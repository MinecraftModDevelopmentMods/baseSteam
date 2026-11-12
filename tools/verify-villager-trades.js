'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..', 'src/main/resources/data');
const uniform = (min, max) => ({ type: 'minecraft:uniform', min, max });
const sale = (item, cost, modifiers = []) => ({
  wants: { id: 'minecraft:emerald', count: cost },
  gives: { id: `basesteam:${item}`, count: 1 },
  max_uses: 7, reputation_discount: 0, xp: 1,
  given_item_modifiers: modifiers
});
const count = (min, max) => [{ function: 'minecraft:set_count', count: uniform(min, max) }];
const expectedTrades = {
  steam_governor: sale('steam_governor', 1, count(1, 4)),
  steam_pipe: sale('steam_pipe', 1, count(4, 8)),
  blackpowder_cartridge: sale('blackpowder_cartridge', 1, count(5, 7)),
  musket: sale('musket', uniform(10, 15)),
  enchanted_musket: sale('musket', uniform(15, 25), [{
    function: 'minecraft:enchant_with_levels', levels: uniform(5, 19),
    options: '#minecraft:non_treasure', include_additional_cost_component: true
  }])
};
const expectedTags = {
  'armorer/level_1': ['steam_governor'],
  'toolsmith/level_1': ['steam_governor', 'blackpowder_cartridge', 'musket'],
  'weaponsmith/level_1': ['steam_governor', 'blackpowder_cartridge', 'musket'],
  'librarian/level_1': ['steam_governor', 'steam_pipe'],
  'cleric/level_3': ['enchanted_musket']
};
function validate(trades, tags) {
  assert.deepEqual(trades, expectedTrades, 'Keep every legacy quantity, inclusive price, uses, XP and enchantment surcharge');
  assert.deepEqual(tags, Object.fromEntries(Object.entries(expectedTags).map(([key, values]) =>
    [key, { replace: false, values: values.map(value => `basesteam:${value}`) }])));
}
function jsonFiles(directory, prefix = '') {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const name = prefix + entry.name;
    return entry.isDirectory() ? jsonFiles(path.join(directory, entry.name), name + '/') :
      entry.name.endsWith('.json') ? [[name.slice(0, -5), JSON.parse(fs.readFileSync(path.join(directory, entry.name), 'utf8'))]] : [];
  });
}
const trades = Object.fromEntries(jsonFiles(path.join(root, 'basesteam/villager_trade')));
const tags = Object.fromEntries(jsonFiles(path.join(root, 'minecraft/tags/villager_trade')));
validate(trades, tags);
const controls = [
  data => { data.trades.steam_governor.given_item_modifiers[0].count.max = 5; },
  data => { data.trades.musket.wants.count.min = 9; },
  data => { data.trades.steam_pipe.max_uses = 8; },
  data => { data.trades.steam_governor.xp = 2; },
  data => { data.trades.musket.reputation_discount = 0.05; },
  data => { data.trades.enchanted_musket.given_item_modifiers[0].include_additional_cost_component = false; },
  data => { data.trades.enchanted_musket.given_item_modifiers[0].levels.max = 20; },
  data => { data.tags['cleric/level_1'] = data.tags['cleric/level_3']; delete data.tags['cleric/level_3']; },
  data => { data.tags['librarian/level_1'].replace = true; },
  data => { data.trades.steam_governor.unrecognized = true; }
];
for (const mutate of controls) {
  const data = structuredClone({ trades, tags });
  mutate(data);
  assert.throws(() => validate(data.trades, data.tags), assert.AssertionError);
}
console.log(`BASESTEAM_NATIVE2612_TRADE_DATA_PASS definitions=5 placements=10 negativeControls=${controls.length}`);
console.log('Exact data contract only; native codec and generated-offer runtime checks remain separate.');
