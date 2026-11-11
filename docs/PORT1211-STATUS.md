# Steam on Minecraft 1.21.1

Version 2.2.15.121011 targets Forge 52.1.0 and Java 21.
The build pins released basePower 2.4.14.121011 at `8d9937b3c0c9`, with
public file IDs and checked jar hashes for all three dependencies.

Steam keeps its mod ID, packages, registry paths, configuration and machine
balance. Datapack registry folders use the native singular names, without
changing logical recipe or advancement IDs. All recipe modes and ingredient
guards remain in place.

The four musket enchantments now load from data and resolve real registry
holders. Their IDs, costs, levels and effects are unchanged. Migration tests
cover their legacy names alongside wear and the loaded byte. Machine state,
drill packets, server-side menus and the whole fluid gauges remain covered
by the Steam tests. The pinned local suite passes 228 tests without skips,
plus four native GameTests, archive audits and the Eclipse classpath gate.
Fresh packaged dedicated servers also start and stop cleanly with and
without Base Metals.

The coordinated suite also passed old-world migration, connected machine
jobs, saved-state checks, chunk unloading and two 30-minute endurance runs.
The client visual and recipe-unlock checks were accepted before publication.
Keep a backup and test upgrades on a copy; these checks cannot cover every modpack.
