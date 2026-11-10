# Steam on Minecraft 1.20.6

This version uses Forge 50.2.0, Java 21 and basePower 2.4.13.120061.
The Steam jar uses Forge's official runtime names and is also the development
dependency. Power, Base Metals and OreSpawn remain separate jars.

## Bringing an old world forward

Make a backup and test with a copy first. Replace Steam Advantage and Power
Advantage with baseSteam and basePower. Don't install both generations of the
mods or reopen an upgraded world in an older version of Minecraft.

Machine IDs and saved fields are unchanged. Old drill state and inventories
continue through the shared world migration. The musket's wear is now a
Minecraft item component, while its loaded state stays in its custom data.
An existing `basesteam-common.toml` still takes precedence over older settings.

## Checks

The Steam tests cover saved and moving drills, musket wear and loading, and
native recipe, advancement and loot registration. The packaged server smoke
uses a fresh world, with a separate run available without Base Metals. It
doesn't replace basePower's world-upgrade tests or an in-game visual check.

See the [README](../README.md) for the commands and exact dependency pins.
