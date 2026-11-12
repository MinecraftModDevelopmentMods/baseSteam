'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const read = name => fs.readFileSync(path.join(root, name), 'utf8');
const mod = 'basesteam';
const java = 'src/main/java/zone/moddev/mc/basesteam';
const resources = 'src/main/resources';
const assets = `${resources}/assets/${mod}`;
const data = `${resources}/data/${mod}`;
function files(dir) {
  return fs.readdirSync(path.join(root, dir), { withFileTypes: true }).flatMap(entry => {
    const name = `${dir}/${entry.name}`;
    return entry.isDirectory() ? files(name) : [name];
  });
}
const json = new Map();
for (const name of files(resources).filter(name => /\.(json|mcmeta)$/.test(name))) {
  assert(!/\/data\/[^/]+\/(?:recipes|advancements|loot_tables|structures)\//.test(name), name);
  assert(!/\/data\/[^/]+\/tags\/(?:items|blocks)\//.test(name), name);
  const text = read(name);
  assert(!/steamadvantage|poweradvantage/.test(name + text), name);
  json.set(name, JSON.parse(text));
}
assert.deepEqual(json.get(`${resources}/pack.mcmeta`).pack, {
  min_format: [101, 1], max_format: [101, 1], description: 'baseSteam'
});
const metadata = read(`${resources}/META-INF/mods.toml`);
assert.match(metadata, /^showAsResourcePack=false$/m);
assert.match(metadata, /modId="basesteam"/);
assert.match(metadata, /modId="basepower"\r?\nmandatory=true\r?\nversionRange="\[\$\{poweradvantage_version\},3\.0\.0\)"/);
assert.match(metadata, /modId="basemetals"\r?\nmandatory=true/);
assert.doesNotMatch(metadata, /dependencies\.steamadvantage|modId="(?:steamadvantage|poweradvantage)"/);

function verifyLocalAsset(id, directory, extension, name) {
  if (!id.startsWith(`${mod}:`)) return;
  const target = `${assets}/${directory}/${id.slice(mod.length + 1)}${extension}`;
  assert(fs.existsSync(path.join(root, target)), `${name} references missing ${target}`);
}
function verifyModelReferences(value, name) {
  if (Array.isArray(value)) value.forEach(child => verifyModelReferences(child, name));
  else if (value && typeof value === 'object') {
    for (const [key, child] of Object.entries(value)) {
      if (typeof child === 'string' && (key === 'model' || key === 'parent')) {
        verifyLocalAsset(child, 'models', '.json', name);
      } else if (key === 'textures' && child && !Array.isArray(child)) {
        for (const texture of Object.values(child)) verifyLocalAsset(texture, 'textures', '.png', name);
      } else verifyModelReferences(child, name);
    }
  }
}
for (const [name, content] of json) {
  if (name.startsWith(assets)) verifyModelReferences(content, name);
}
for (const sourceRoot of ['src/main/java', 'src/test/java', 'src/gametest/java']) {
  for (const name of files(sourceRoot).filter(name => name.endsWith('.java'))) {
    const declared = /^package\s+([\w.]+);/m.exec(read(name))?.[1];
    const expected = path.posix.dirname(name.slice(sourceRoot.length + 1)).replaceAll('/', '.');
    assert.equal(declared, expected, `${name}: source root must match the package (Eclipse/Buildship)`);
  }
}
const source = files('src/main/java').filter(name => name.endsWith('.java'));
for (const name of source) {
  const text = read(name);
  assert(!/net\.minecraft\.(?:block|tileentity|entity|item|inventory)\./.test(text), name);
  assert(!/net\.minecraft\.world\.level\.material\.Material;|\.getMaterial\(\)/.test(text), name);
  assert(!/setPosition\(|setLevelAndPosition\(|ITickableTileEntity|\[native code\]/.test(text), name);
  assert(!/getDeclaredField\("(?:level|world|worldPosition)"\)/.test(text), name);
  assert(!/zone\.moddev\.mc\.(?:steamadvantage|poweradvantage)\b/.test(text), name);
}
const machines = `${java}/machines`;
for (const name of files(machines).filter(name => name.endsWith('Block.java'))) {
  assert(/newBlockEntity\(net\.minecraft\.core\.BlockPos pos, BlockState state\)/.test(read(name)), name);
  assert(/return new \w+TileEntity\(pos, state\)/.test(read(name)), name);
}
for (const file of ['Blocks', 'Items']) {
  const text = read(`${java}/init/${file}.java`);
  assert(text.includes('import zone.moddev.mc.basepower.init.ItemGroups;'));
  assert(text.includes('ItemGroups.show(item)'));
  assert(!text.includes('.tab('));
}
assert.match(read(`${java}/init/Blocks.java`),
  /if \(!"pump_pipe_steam"\.equals\(id\) && !"drillbit"\.equals\(id\)\) ItemGroups\.show\(item\)/);
for (const id of ['steam_machine', 'musket']) {
  assert.deepEqual(json.get(`${data}/damage_type/${id}.json`),
    { message_id: id, scaling: 'never', exhaustion: 0.1 });
  assert.equal(typeof json.get(`${assets}/lang/en_us.json`)[`death.attack.${id}`], 'string');
}
const migration = read(`${java}/migration/SteamLegacyMigrations.java`);
assert.match(migration, /LEGACY_MODID = "steamadvantage"/);
assert.match(migration, /registerNamespaceAlias\(LEGACY_MODID, SteamAdvantage.MODID\)/);
assert.match(migration, /registerTileEntityAliases\(id\(path\)\)/);
const entities = read(`${java}/init/Entities.java`);
assert(entities.includes('BlockEntityType.BlockEntitySupplier<T> factory'));
const ids = [...entities.matchAll(/register\(event, "([^"]+)"/g)].map(match => match[1]);
assert.equal(ids.length, 12);
const pickaxe = json.get(`${resources}/data/minecraft/tags/block/mineable/pickaxe.json`);
assert.equal(pickaxe.replace, false);
assert.equal(new Set(pickaxe.values).size, 17);
for (const id of ids) assert(pickaxe.values.includes(`${mod}:${id}`), id);
for (const id of pickaxe.values.map(value => value.split(':')[1]).filter(id => !['drillbit', 'platform', 'pump_pipe_steam'].includes(id))) {
  const loot = json.get(`${data}/loot_table/blocks/${id}.json`);
  assert.equal(loot.pools[0].entries[0].name, `${mod}:${id}`);
}
const tests = files('src/test/java').filter(name => name.endsWith('.java'));
const testCount = tests.reduce((sum, name) => sum + (read(name).match(/@Test\b/g) || []).length, 0);
for (const id of [...pickaxe.values.map(value => value.split(':')[1]),
  'steam_governor', 'steam_drill_bit', 'blackpowder_cartridge', 'musket']) {
  assert(json.has(`${assets}/items/${id}.json`), `Missing native item definition: ${id}`);
}
for (const name of source) {
  assert(!/ResourceLocation|net\.minecraftforge\.eventbus\.api\.(?:IEventBus|SubscribeEvent)/.test(read(name)), name);
}
console.log(`BASESTEAM_NATIVE2612_SOURCE_PASS json=${json.size} java=${source.length} factories=${ids.length} harvest=${pickaxe.values.length} testMethods=${testCount}`);
console.log('Source checks only. Compilation, executed tests, packaged runtime and manual acceptance are separate.');
require('./verify-enchantment-data');
require('./verify-villager-trades');
