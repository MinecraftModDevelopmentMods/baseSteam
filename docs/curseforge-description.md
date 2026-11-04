# baseSteam

**A Base Metals expansion continuing DrCyano's Steam Advantage, with backwards-compatible upgrades for existing Minecraft 1.10.2 worlds.**

Put your workshop under steam. baseSteam adds boilers, brass pipework and steam-powered machinery to [basePower](https://www.curseforge.com/minecraft/mc-mods/basepower), continuing [DrCyano's original Steam Advantage](https://www.curseforge.com/minecraft/mc-mods/steam-advantage) with maintained code, bug fixes and support for existing saves.

This is the continuation under a new name, not an extra mod to install alongside Steam Advantage.

## What Does It Add?

- **Boilers:** Make steam with solid fuel, geothermal heat or liquid fuel. An electric boiler can also turn an electrical supply into steam.
- **Steam distribution:** Connect machines with brass pipes, store surplus in steam tanks and control connections with redstone switches. Tanks are reserves, not pressure boosters; pipes do not lose pressure just because the run is long.
- **Rock crusher:** Automate Base Metals' crushing recipes, from ore processing to making starch from potatoes.
- **Blast furnace:** Smelt three input stacks in parallel, with steam-assisted heating. It still needs its own solid fuel.
- **Mining drill:** Bore in any of six directions, collect the output inside the machine and advance along steam track. Connect storage or a conveyor at the rear to take the output away.
- **Pumps and elevators:** Collect world fluids through an extending pump shaft, or build a steam-powered lift.
- **Steam distillation:** Refine crude oil without a solid-fuel still. Both basePower and Mineralogy crude oils work in the appropriate distillation and oil-boiler machinery.
- **The works musket:** An optional black-powder firearm with cartridges, configurable damage and reload time, and its original enchantments.

## Getting Started

Gather iron, brass and steel, then make a few steam governors. A coal-fired boiler, some brass pipe and a rock crusher make a useful first installation. Supply the boiler with water and furnace fuel, connect the steam line, then give the crusher a small batch to process.

Keep water pipes and steam pipes separate: basePower's fluid system brings water to the boiler, while brass steam pipes carry its output to machines. A geothermal boiler beside lava is an alternative heat source, but it still needs water.

Recipes follow basePower's **Normal**, **Tech Progression** or **Apocalyptic** mode. The [Boiler-House Handbook](https://github.com/MinecraftModDevelopmentMods/baseSteam/wiki) covers recipes, layouts, gauges, storage and fault finding.

## Requirements

- **Minecraft 1.10.2 with Forge 12.18.3.2511 or a compatible later 1.10.2 Forge build.**
- **Java 8.**
- **[basePower](https://www.curseforge.com/minecraft/mc-mods/basepower) 2.4.4.110021 or newer for Minecraft 1.10.2.**
- **[Base Metals](https://www.curseforge.com/minecraft/mc-mods/base-metals).**
- **[MMD OreSpawn](https://www.curseforge.com/minecraft/mc-mods/mmd-orespawn), required by basePower.**

The current 1.10.2 builds were tested together with Base Metals `2.5.1.110021` and OreSpawn `4.1.2.110021`, including existing-world upgrades and full restarts.

[baseElectric](https://github.com/MinecraftModDevelopmentMods/baseElectric) is an optional companion. It adds electrical generation, batteries, wiring and machines, and can supply the electric boiler. It is not needed for the coal, geothermal or oil-fired installations.

## Moving An Existing Steam Advantage World

baseSteam supports upgrading worlds made with the original released Cyano Minecraft 1.10.2 Steam Advantage. Existing blocks, items and saved machine state are migrated to the new identities without needing to rebuild your steam works, including inventories, stored steam, fluid, heat, drill direction and unfinished work.

1. Back up the complete world and its configuration before changing any mods.
2. Remove Power Advantage and Steam Advantage, then install matching basePower and baseSteam builds. Do not keep the old and new jars together.
3. If the world also uses Electric Advantage, replace it with matching baseElectric at the same time. Do not mix the old and new families.
4. Open a copy first, inspect your machines and contents, then close Minecraft completely and reopen it to check the saved result.

The old Steam configuration is carried forward when no new baseSteam configuration exists; the original file is retained. Existing musket enchantments are preserved. Keep your complete backup: rolling back means restoring it, not putting old jars into an already converted world.

This compatibility applies to released 1.10.2 worlds. It is not a promise that every old third-party add-on works unchanged; integrations which look for the old mod IDs may need an update. Read the [upgrade guide](https://github.com/MinecraftModDevelopmentMods/baseSteam/wiki/Installation-and-Upgrading) before converting a valued save.

## Credits, Modpacks And Development

Steam Advantage was created by **DrCyano (Cyanobacterium)**. baseSteam continues that work as part of **Minecraft Mod Development**, alongside Base Metals, basePower and baseElectric.

Modpack inclusion is welcome. The project remains open source under **GPL-3.0**.

[Source code](https://github.com/MinecraftModDevelopmentMods/baseSteam) | [Player wiki](https://github.com/MinecraftModDevelopmentMods/baseSteam/wiki) | [Report an issue](https://github.com/MinecraftModDevelopmentMods/baseSteam/issues) | [Original Steam Advantage](https://www.curseforge.com/minecraft/mc-mods/steam-advantage)
