# Steam on Minecraft 1.21.1

Local candidate 2.2.15.121011 targets Forge 52.1.0 and Java 21.
The build pins the coordinated local basePower commit `72fd326b9a9c` and
its exact native jar. Public CI still requires merged sources and verified
public files. Nothing has been published.

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

Back up old worlds and use disposable copies with basePower's coordinated
migration rig. These Steam checks do not establish combined world-upgrade,
multiplayer or manual visual acceptance.
