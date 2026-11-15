package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import static org.junit.Assert.*;

public class SharedCrushingRecipeContractTest {
    private static final List<String> MACHINES = List.of("steam_boiler_coal", "steam_boiler_electric",
            "steam_boiler_geothermal", "steam_crusher", "steam_drill", "steam_elevator",
            "steam_furnace", "steam_governor", "steam_tank");

    @Test public void preservesAllNineIdsInputsOutputsAndAmountsWithNativeConditions() throws Exception {
        Path directory = Path.of("src/main/resources/data/basesteam/recipe");
        try (var files = Files.list(directory)) {
            assertEquals(9, files.filter(p -> p.getFileName().toString().startsWith("crushing_")).count());
        }
        for (String machine : MACHINES) {
            JsonObject recipe = JsonParser.parseString(Files.readString(directory.resolve("crushing_" + machine + ".json"))).getAsJsonObject();
            assertEquals(4, recipe.size());
            assertEquals("mmdlib:crushing", recipe.get("type").getAsString());
            assertEquals("basesteam:" + machine, recipe.getAsJsonObject("ingredient").get("item").getAsString());
            JsonObject output = recipe.getAsJsonObject("result");
            assertEquals(machine.equals("steam_governor") ? "basepower:sprocket" : "basesteam:steam_governor", output.get("id").getAsString());
            assertEquals(machine.equals("steam_tank") ? 1 : machine.equals("steam_elevator") ? 3 : 2,
                    output.has("count") ? output.get("count").getAsInt() : 1);
            var conditions = recipe.getAsJsonArray("neoforge:conditions");
            assertEquals(1, conditions.size());
            assertEquals("basesteam:recipe_mode", conditions.get(0).getAsJsonObject().get("type").getAsString());
            assertEquals("APOCALYPTIC", conditions.get(0).getAsJsonObject().get("mode").getAsString());
        }
    }

    @Test public void bothLookupOverloadsDelegateWithoutRuntimeFallbacksOrOptionalRecipeTypes() throws Exception {
        ClassNode node = new ClassNode();
        try (var stream = getClass().getResourceAsStream("/zone/moddev/mc/basesteam/compat/BaseMetalsCompat.class")) {
            new ClassReader(stream).accept(node, 0);
        }
        int overloads = 0;
        for (var method : node.methods) {
            if (!method.name.equals("getCrusherRecipeOutput")) continue;
            overloads++;
            int calls = 0;
            for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) {
                assertEquals("zone/moddev/mc/basepower/compat/CrushingRecipes", call.owner);
                assertEquals("find", call.name);
                calls++;
            }
            assertEquals(1, calls);
        }
        assertEquals(2, overloads);
    }
}
