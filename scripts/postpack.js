const fs = require('fs');
const path = require('path');
const exec = require('child_process').execSync;

const rootDir = path.join(__dirname, '../');
fs.readdirSync(rootDir).forEach(file => {
  if (file.startsWith('frontegg-ionic-capacitor-') && file.endsWith('.tgz')) {
    console.log(file);
    if (fs.existsSync(path.join(rootDir, 'frontegg-ionic-capacitor.tgz'))) {
      fs.unlinkSync(path.join(rootDir, 'frontegg-ionic-capacitor.tgz'));
    }
    fs.renameSync(
      path.join(rootDir, file),
      path.join(rootDir, 'frontegg-ionic-capacitor.tgz'),
    );
  }
});

exec('cd example && npm install ../frontegg-ionic-capacitor.tgz', {
  stdio: 'inherit',
});
