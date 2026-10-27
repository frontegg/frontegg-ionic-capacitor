import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import ts from 'typescript';

const androidPlugin = readFileSync('android/src/main/java/com/frontegg/ionic/FronteggNativePlugin.java', 'utf8');
const iosPlugin = readFileSync('ios/Plugin/FronteggNativePlugin.swift', 'utf8');
const definitions = ts.createSourceFile(
  'definitions.ts',
  readFileSync('src/definitions.ts', 'utf8'),
  ts.ScriptTarget.Latest,
  true,
);

// The Android SDK's directLoginAction(activity, type, data) takes no ephemeral-session flag.
const androidIgnoredArguments = { directLoginAction: ['ephemeralSession'] };
// Kept for apps that called the undeclared method directly; openAdminPortal is the declared one.
const androidUndeclaredMethods = ['showAdminPortal'];

function balancedFrom(source, start, open, close) {
  const openIndex = source.indexOf(open, start);
  let depth = 0;
  for (let index = openIndex; index < source.length; index++) {
    if (source[index] === open) depth++;
    if (source[index] === close && --depth === 0) return source.slice(openIndex, index + 1);
  }
  throw new Error(`Unbalanced ${open} after offset ${start}`);
}

function matches(pattern, text) {
  return new Set([...text.matchAll(pattern)].map((match) => match[1]));
}

function functionBodies(source, declarationPattern) {
  return new Map(
    [...source.matchAll(declarationPattern)].map((match) => [
      match[1],
      balancedFrom(source, match.index + match[0].length - 1, '{', '}'),
    ]),
  );
}

function withCalledHelpers(body, helpers) {
  const called = [...matches(/\b(\w+)\(/g, body)].filter((name) => helpers.has(name));
  return [body, ...called.map((name) => helpers.get(name))].join('\n');
}

function nativePlugin(source, { methodPattern, helperPattern, argumentPattern, resultKeyPattern }) {
  const helpers = functionBodies(source, helperPattern);
  return new Map(
    [...functionBodies(source, methodPattern)].map(([name, body]) => [
      name,
      {
        argumentKeys: matches(argumentPattern, body),
        resultKeys: matches(resultKeyPattern, withCalledHelpers(body, helpers)),
      },
    ]),
  );
}

const android = nativePlugin(androidPlugin, {
  methodPattern: /@PluginMethod\s+public void (\w+)\(PluginCall call\)\s*\{/g,
  helperPattern: /(?:private|public|protected)\s+[\w<>[\]]+\s+(\w+)\([^)]*\)\s*\{/g,
  argumentPattern: /call\.get\w+\("(\w+)"/g,
  resultKeyPattern: /put\("(\w+)"/g,
});

const ios = nativePlugin(iosPlugin, {
  methodPattern: /@objc func (\w+)\(_ call: CAPPluginCall\)\s*\{/g,
  helperPattern: /func (\w+)\([^)]*\)[^{]*\{/g,
  argumentPattern: /call\.(?:get\w+\("|options\[")(\w+)"/g,
  resultKeyPattern: /"(\w+)"\s*:/g,
});

const iosRegisteredMethods = matches(/CAPPluginMethod\(name: "(\w+)"/g, iosPlugin);

const interfaces = new Map(
  definitions.statements.filter(ts.isInterfaceDeclaration).map((node) => [node.name.text, node]),
);

function objectKeys(typeNode) {
  const members = ts.isTypeLiteralNode(typeNode)
    ? typeNode.members
    : ts.isTypeReferenceNode(typeNode) && interfaces.has(typeNode.typeName.getText())
      ? interfaces.get(typeNode.typeName.getText()).members
      : null;
  if (!members) return null;
  const properties = members.filter(ts.isPropertySignature);
  return {
    all: new Set(properties.map((property) => property.name.getText())),
    required: new Set(
      properties.filter((property) => !property.questionToken).map((property) => property.name.getText()),
    ),
  };
}

const declaredMethods = new Map(
  interfaces
    .get('FronteggNativePlugin')
    .members.filter(ts.isMethodSignature)
    .filter((method) => method.name.getText() !== 'addListener')
    .map((method) => {
      const payload = method.parameters[0]?.type;
      const resolved = method.type.typeArguments?.[0];
      return [
        method.name.getText(),
        {
          argumentKeys: payload ? objectKeys(payload).all : new Set(),
          resultKeys: resolved ? objectKeys(resolved) : null,
        },
      ];
    }),
);

const platforms = [
  {
    name: 'Android',
    methods: android,
    ignoredArguments: androidIgnoredArguments,
    undeclared: androidUndeclaredMethods,
  },
  { name: 'iOS', methods: ios, ignoredArguments: {}, undeclared: [] },
];

test('iOS registers every method it implements', () => {
  assert.deepEqual(iosRegisteredMethods, new Set(ios.keys()));
});

for (const platform of platforms) {
  test(`${platform.name} implements exactly the declared methods`, () => {
    const implemented = new Set(platform.methods.keys());
    platform.undeclared.forEach((name) => implemented.delete(name));
    assert.deepEqual(implemented, new Set(declaredMethods.keys()));
  });

  test(`${platform.name} reads exactly the declared arguments`, () => {
    const mismatches = [...declaredMethods]
      .filter(([name]) => platform.methods.has(name))
      .map(([name, declared]) => {
        const expected = [...declared.argumentKeys].filter((key) => !platform.ignoredArguments[name]?.includes(key));
        const read = [...platform.methods.get(name).argumentKeys];
        return { name, expected: expected.sort(), read: read.sort() };
      })
      .filter(({ expected, read }) => expected.join() !== read.join())
      .map(({ name, expected, read }) => `${name}: declared {${expected}}, native reads {${read}}`);
    assert.deepEqual(mismatches, []);
  });

  test(`${platform.name} resolves every required field of the declared results`, () => {
    const mismatches = [...declaredMethods]
      .filter(([name, declared]) => declared.resultKeys && platform.methods.has(name))
      .map(([name, declared]) => ({
        name,
        missing: [...declared.resultKeys.required].filter((key) => !platform.methods.get(name).resultKeys.has(key)),
      }))
      .filter(({ missing }) => missing.length > 0)
      .map(({ name, missing }) => `${name}: missing {${missing}}`);
    assert.deepEqual(mismatches, []);
  });
}
