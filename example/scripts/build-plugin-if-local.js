/**
 * When the example app uses @frontegg/ionic-capacitor via "file:..", the plugin
 * must be built (dist/) so the module resolves. This script runs after
 * npm install: if the parent directory is the plugin, install its deps and build.
 */
const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

const exampleDir = __dirname.replace(/[/\\]scripts$/, '');
const parentDir = path.join(exampleDir, '..');

let pkg;
try {
  pkg = JSON.parse(fs.readFileSync(path.join(parentDir, 'package.json'), 'utf8'));
} catch {
  process.exit(0);
}

if (pkg.name !== '@frontegg/ionic-capacitor') {
  process.exit(0);
}

console.log('[@frontegg/ionic-capacitor example] Building linked plugin...');
try {
  execSync('npm install', { cwd: parentDir, stdio: 'inherit' });
  execSync('npm run build', { cwd: parentDir, stdio: 'inherit' });
  console.log('[@frontegg/ionic-capacitor example] Plugin build done.');
} catch (err) {
  console.warn('[@frontegg/ionic-capacitor example] Plugin build failed. Run from repo root: npm install && npm run build');
}
