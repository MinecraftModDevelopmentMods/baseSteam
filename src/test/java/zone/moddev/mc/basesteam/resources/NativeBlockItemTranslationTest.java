package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Lifecycle;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.Test;
import zone.moddev.mc.basepower.init.ItemGroups;
import zone.moddev.mc.basesteam.init.Blocks;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeBlockItemTranslationTest {
    @Test public void actualProductionBlockItemFactoryUsesTranslatedBlockKeys() throws Exception {
        isolated("factory", "BLOCKITEM_FACTORY_17_PASS");
    }

    @Test public void omittedNativeBlockPrefixReproducesTheUntranslatedItemKey() throws Exception {
        isolated("negative", "BLOCKITEM_MISSING_PREFIX_NEGATIVE_PASS");
    }

    private static void isolated(String mode, String marker) throws Exception {
        Path root = Path.of("build/native-blockitem-translations").toAbsolutePath();
        Files.createDirectories(root);
        Path evidence = Files.createTempDirectory(root, mode + "-");
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
        if (!Files.isExecutable(javaExecutable)) javaExecutable = javaExecutable.resolveSibling("java.exe");
        String classpath = Arrays.stream(System.getProperty("basesteam.nativePackClasspath",
                System.getProperty("java.class.path")).split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
                .map(entry -> new java.io.File(entry).getAbsolutePath())
                .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
        var command = new ArrayList<>(java.util.List.of(javaExecutable.toString(), "-XX:ActiveProcessorCount=4", "-Xmx512m",
                "-Dterminal.jline=false", "-Dbasesteam.resourcesRoot=" + Path.of("src/main/resources").toAbsolutePath(),
                "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED"));
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:")).toList());
        command.addAll(java.util.List.of("-cp", classpath, NativeBlockItemTranslationTest.class.getName(), mode));
        Path log = evidence.resolve("native-factory.log");
        ProcessBuilder builder = new ProcessBuilder(command).directory(evidence.toFile())
                .redirectInput(ProcessBuilder.Redirect.PIPE).redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("TERM", "dumb");
        for (String name : java.util.List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS")) builder.environment().remove(name);
        Process process = builder.start();
        if (!process.waitFor(90, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new AssertionError("Native BlockItem proof timed out: " + log);
        }
        String output = Files.readString(log);
        zone.moddev.mc.basesteam.testing.NativePackResourceProof.requireCleanOutput(output, log);
        assertEquals(log + "\n" + output, 0, process.exitValue());
        assertTrue(log + "\n" + output, output.contains(marker));
    }

    public static void main(String[] args) throws Exception {
        zone.moddev.mc.basesteam.testing.NativePackResourceProof.requireCleanJvmEnvironment();
        if (args.length != 1) throw new IllegalArgumentException("Expected factory or negative proof");
        NativeTestBootstrap.start();
        Blocks.init();
        switch (args[0]) {
            case "factory" -> {
                proveFactory();
                System.out.println("BLOCKITEM_FACTORY_17_PASS");
            }
            case "negative" -> {
                proveNegative();
                System.out.println("BLOCKITEM_MISSING_PREFIX_NEGATIVE_PASS");
            }
            default -> throw new IllegalArgumentException("Unknown BlockItem proof: " + args[0]);
        }
    }

    @SuppressWarnings("unchecked")
    private static void proveFactory() throws Exception {
        MappedRegistry<Item> items = (MappedRegistry<Item>)net.minecraft.core.registries.BuiltInRegistries.ITEM;
        var constructor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        constructor.setAccessible(true);
        var visible = ItemGroups.class.getDeclaredField("VISIBLE_ITEMS");
        visible.setAccessible(true);
        Set<Item> visibleItems = (Set<Item>)visible.get(null);
        try {
            Blocks.registerBlockItems(constructor.newInstance(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS, items));
        } finally {
            items.keySet().stream().filter(id -> id.getNamespace().equals("basesteam"))
                    .forEach(id -> visibleItems.remove(items.getValue(id)));
        }
        NativeTestBootstrap.bindRegisteredItemComponents();
        assertEquals(Set.of("steam_pipe", "steam_track", "steam_boiler_coal", "steam_boiler_electric",
                "steam_boiler_geothermal", "steam_tank", "steam_furnace", "steam_crusher", "steam_drill",
                "drillbit", "platform", "steam_elevator", "steam_switch", "pump_pipe_steam", "steam_still",
                "steam_pump", "steam_boiler_oil"),
                items.keySet().stream().filter(id -> id.getNamespace().equals("basesteam"))
                        .map(Identifier::getPath).collect(java.util.stream.Collectors.toSet()));
        JsonObject english = english();
        for (Identifier id : items.keySet()) {
            if (!id.getNamespace().equals("basesteam")) continue;
            String key = "block.basesteam." + id.getPath();
            assertEquals("basesteam", id.getNamespace());
            assertTrue(items.getValue(id) instanceof BlockItem);
            BlockItem item = (BlockItem)items.getValue(id);
            assertEquals(key, item.getBlock().getDescriptionId());
            assertEquals(key, item.getDescriptionId());
            assertTrue(item.getDefaultInstance().getHoverName().getContents() instanceof TranslatableContents);
            assertEquals(key, ((TranslatableContents)item.getDefaultInstance().getHoverName().getContents()).getKey());
            assertTrue("Untranslated native block item " + key, english.has(key));
            assertFalse(english.get(key).getAsString().isBlank());
        }
    }

    private static void proveNegative() throws Exception {
        BlockItem wrong = new BlockItem(Blocks.steam_pipe, new Item.Properties().setId(ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath("basesteam", "steam_pipe"))));
        ForgeRegistries.ITEMS.register(Identifier.fromNamespaceAndPath("basesteam", "steam_pipe"), wrong);
        NativeTestBootstrap.bindRegisteredItemComponents();
        assertEquals("item.basesteam.steam_pipe", wrong.getDescriptionId());
        assertEquals("item.basesteam.steam_pipe",
                ((TranslatableContents)wrong.getDefaultInstance().getHoverName().getContents()).getKey());
        assertFalse(english().has(wrong.getDescriptionId()));
        assertTrue(english().has("block.basesteam.steam_pipe"));
    }

    private static JsonObject english() throws Exception {
        return JsonParser.parseString(Files.readString(
                Path.of(System.getProperty("basesteam.resourcesRoot"), "assets/basesteam/lang/en_us.json"))).getAsJsonObject();
    }
}
