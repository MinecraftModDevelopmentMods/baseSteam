'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const read = name => fs.readFileSync(path.join(root, name), 'utf8');
const data = 'src/main/resources/data/basesteam/loot_table';
const blocks = fs.readdirSync(path.join(root, data, 'blocks')).filter(name => name.endsWith('.json'));
assert.equal(blocks.length, 14);
for (const name of blocks) {
  const id = name.slice(0, -5);
  const table = JSON.parse(read(`${data}/blocks/${name}`));
  assert.deepEqual(table, {type: 'minecraft:block', pools: [{rolls: 1,
    entries: [{type: 'minecraft:item', name: `basesteam:${id}`}],
    condition: {type: 'minecraft:survives_explosion'}}]});
}
const uniform = (min, max) => ({type: 'minecraft:uniform', min, max});
const count = (min, max) => [{type: 'minecraft:set_count', count: uniform(min, max)}];
const expected = [
  {type: 'minecraft:empty', weight: 135},
  {type: 'minecraft:item', name: 'basesteam:steam_governor', weight: 50, modifier: count(1, 6)},
  {type: 'minecraft:item', name: 'basesteam:steam_pipe', weight: 25, modifier: count(3, 6)},
  {type: 'minecraft:item', name: 'basesteam:blackpowder_cartridge', weight: 50, modifier: count(5, 15)},
  {type: 'minecraft:item', name: 'basesteam:musket', weight: 18},
  {type: 'minecraft:item', name: 'basesteam:musket', weight: 2,
    modifier: [{type: 'minecraft:enchant_with_levels', levels: 10}]}
];
const chest = JSON.parse(read(`${data}/chests/advantage_parts.json`));
assert.equal(chest.type, 'minecraft:chest');
assert.equal(chest.pools.length, 1);
assert.equal(chest.pools[0].rolls, 1);
assert.deepEqual(chest.pools[0].entries, expected);
const source = read('src/main/java/zone/moddev/mc/basesteam/init/TreasureChests.java');
const initializer = source.slice(source.indexOf('private static final String LOOT_POOL ='));
const generated = JSON.parse([...initializer.matchAll(/"(?:\\.|[^"\\])*"/g)]
  .map(match => JSON.parse(match[0])).join(''));
const normalize = value => {
  if (Array.isArray(value)) return value.map(normalize);
  if (!value || typeof value !== 'object') return value;
  return Object.fromEntries(Object.entries(value).filter(([key]) => key !== 'entryName').map(([key, child]) =>
    [key, key === 'type' && typeof child === 'string' && !child.includes(':') ? `minecraft:${child}` : normalize(child)]));
};
assert.deepEqual(normalize(generated.pools[0].entries), expected);
assert.equal(generated.pools[0].rolls, 1);
for (const mutate of [
  pool => { pool.functions = pool.modifier; delete pool.modifier; },
  pool => { pool.modifier[0].function = pool.modifier[0].type; delete pool.modifier[0].type; },
  pool => { delete pool.modifier[0].count.type; }
]) {
  const control = structuredClone(expected[1]);
  mutate(control);
  assert.throws(() => assert.deepEqual(control, expected[1]), assert.AssertionError);
}
console.log('BASESTEAM_NATIVE263_LOOT_DATA_PASS blocks=14 chestEntries=6 generatedTemplate=1 negativeControls=3');
