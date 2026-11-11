'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const read = file => JSON.parse(fs.readFileSync(path.join(root, 'src/main/resources', file), 'utf8'));
const specs = {
  high_explosive: [5, 5, 7, 7, 17, 7, 2],
  powderless: [1, 1, 20, 0, 50, 0, 8],
  rapid_reload: [2, 1, 15, 0, 50, 0, 4],
  recoil: [10, 5, 2, 5, 12, 5, 1]
};
for (const [id, [weight, maxLevel, min, minStep, max, maxStep, anvil]] of Object.entries(specs)) {
  assert.deepEqual(read(`data/basesteam/enchantment/${id}.json`), {
    description: {translate: `enchantment.${id}`},
    supported_items: '#basesteam:muskets', weight, max_level: maxLevel,
    min_cost: {base: min, per_level_above_first: minStep},
    max_cost: {base: max, per_level_above_first: maxStep},
    anvil_cost: anvil, slots: ['mainhand'], effects: {}
  }, id);
}
for (const tag of ['non_treasure', 'tooltip_order']) {
  const data = read(`data/minecraft/tags/enchantment/${tag}.json`);
  assert.equal(data.replace, false);
  assert.deepEqual([...data.values].sort(), Object.keys(specs).map(id => `basesteam:${id}`).sort());
}
assert.deepEqual(read('data/basesteam/tags/item/muskets.json'), {replace: false, values: ['basesteam:musket']});
const loot = read('data/basesteam/loot_table/chests/advantage_parts.json');
const enchanted = loot.pools[0].entries.find(entry => entry.functions?.some(f => f.function === 'minecraft:enchant_with_levels'));
assert.equal(enchanted.name, 'basesteam:musket');
assert.equal(enchanted.weight, 2);
// No options means all registry enchantments, retaining the old treasure=true loot policy.
assert.deepEqual(enchanted.functions, [{function: 'minecraft:enchant_with_levels', levels: 10}]);
console.log('BASESTEAM_ENCHANTMENT_DATA_PASS definitions=4 distributionTags=2');
