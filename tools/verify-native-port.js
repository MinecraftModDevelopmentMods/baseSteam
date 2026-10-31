'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const read = name => fs.readFileSync(path.join(root, name), 'utf8');
function files(dir) {
  return fs.readdirSync(path.join(root, dir), { withFileTypes: true }).flatMap(entry => {
    const name = `${dir}/${entry.name}`;
    return entry.isDirectory() ? files(name) : [name];
  });
}
let json = 0;
for (const name of files('src/main/resources').filter(name => /\.(json|mcmeta)$/.test(name))) {
  JSON.parse(read(name));
  json++;
}
assert.equal(JSON.parse(read('src/main/resources/pack.mcmeta')).pack.pack_format, 7);
const source = files('src/main/java').filter(name => name.endsWith('.java'));
for (const name of source) {
  const text = read(name);
  assert(!/net\.minecraft\.(?:block|tileentity|entity|item|inventory)\./.test(text), name);
  assert(!/setPosition\(|setLevelAndPosition\(|ITickableTileEntity|\[native code\]/.test(text), name);
  assert(!/getDeclaredField\("(?:level|world|worldPosition)"\)/.test(text), name);
}
const machines = 'src/main/java/zone/moddev/mc/steamadvantage/machines';
for (const name of files(machines).filter(name => name.endsWith('Block.java'))) {
  assert(/newBlockEntity\(net\.minecraft\.core\.BlockPos pos, BlockState state\)/.test(read(name)), name);
  assert(/return new \w+TileEntity\(pos, state\)/.test(read(name)), name);
}
const entities = read('src/main/java/zone/moddev/mc/steamadvantage/init/Entities.java');
assert(entities.includes('BlockEntityType.BlockEntitySupplier<T> factory'));
const ids = [...entities.matchAll(/register\(event, "([^"]+)"/g)].map(match => match[1]);
assert.equal(ids.length, 12);
const pickaxe = JSON.parse(read('src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json'));
assert.equal(pickaxe.replace, false);
assert.equal(new Set(pickaxe.values).size, 17);
for (const id of ids) assert(pickaxe.values.includes(`steamadvantage:${id}`), id);
for (const id of pickaxe.values.map(value => value.split(':')[1]).filter(id => !['drillbit', 'platform', 'pump_pipe_steam'].includes(id))) {
  const loot = JSON.parse(read(`src/main/resources/data/steamadvantage/loot_tables/blocks/${id}.json`));
  assert.equal(loot.pools[0].entries[0].name, `steamadvantage:${id}`);
}
const tests = files('src/test/java').filter(name => name.endsWith('.java'));
const testCount = tests.reduce((sum, name) => sum + (read(name).match(/@Test\b/g) || []).length, 0);
console.log(`STEAM_NATIVE117_SOURCE_PASS json=${json} java=${source.length} factories=${ids.length} harvest=${pickaxe.values.length} testMethods=${testCount}`);
console.log('Source checks only. Compilation, executed tests, packaged runtime and manual acceptance are separate.');
