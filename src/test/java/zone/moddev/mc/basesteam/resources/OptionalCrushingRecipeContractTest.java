package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class OptionalCrushingRecipeContractTest {
    private static final Path RECIPES = Paths.get("src/main/resources/data/basesteam/recipes");
    private static final String[] MACHINES = {"steam_boiler_coal", "steam_boiler_electric",
            "steam_boiler_geothermal", "steam_crusher", "steam_drill", "steam_elevator",
            "steam_furnace", "steam_governor", "steam_tank"};

    @BeforeClass public static void bootstrapNativeRegistries() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private static JsonObject json(String text) {
        return new JsonParser().parse(text).getAsJsonObject();
    }

    private static Map<String, JsonObject> crushingRecipes() throws Exception {
        Map<String, JsonObject> result = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(RECIPES)) {
            for (Path path : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    JsonObject recipe = new JsonParser().parse(reader).getAsJsonObject();
                    if ("basemetals:crushing".equals(recipe.get("type").getAsString()))
                        result.put(path.getFileName().toString(), recipe);
                }
            }
        }
        return result;
    }

    private static boolean baseGuard(JsonElement value) {
        if (value == null || !value.isJsonObject()) return false;
        JsonObject condition = value.getAsJsonObject();
        if (condition.size() != 2 || !condition.has("type") || !condition.has("modid")) return false;
        for (String field : Arrays.asList("type", "modid"))
            if (!condition.get(field).isJsonPrimitive()
                    || !condition.getAsJsonPrimitive(field).isString()) return false;
        return "forge:mod_loaded".equals(condition.get("type").getAsString())
                && "basemetals".equals(condition.get("modid").getAsString());
    }

    @Test public void everyOptionalSerializerIsGuardedBeforeItsApocalypticCondition() throws Exception {
        Map<String, JsonObject> recipes = crushingRecipes();
        assertEquals("Keep the exact nine optional processing variants", 9, recipes.size());
        for (String machine : MACHINES) {
            String name = "crushing_" + machine + ".json";
            JsonObject recipe = recipes.get(name);
            assertNotNull(name, recipe);
            assertEquals(name, 2, recipe.getAsJsonArray("conditions").size());
            assertTrue(name + " must skip the unavailable serializer", baseGuard(
                    recipe.getAsJsonArray("conditions").get(0)));
            JsonObject mode = recipe.getAsJsonArray("conditions").get(1).getAsJsonObject();
            assertEquals(name, 2, mode.size());
            assertEquals("basesteam:recipe_mode", mode.get("type").getAsString());
            assertEquals("APOCALYPTIC", mode.get("mode").getAsString());
        }
    }

    @Test public void malformedOrUnrelatedProviderConditionsCannotSatisfyTheGuard() {
        assertTrue(baseGuard(json("{\"type\":\"forge:mod_loaded\",\"modid\":\"basemetals\"}")));
        for (String condition : Arrays.asList("{}",
                "{\"type\":\"basesteam:recipe_mode\",\"mode\":\"APOCALYPTIC\"}",
                "{\"type\":\"forge:mod_loaded\",\"modid\":\"basesteam\"}",
                "{\"type\":\"forge:mod_loaded\",\"modid\":false}",
                "{\"type\":\"forge:mod_loaded\",\"modid\":[\"basemetals\"]}",
                "{\"type\":\"forge:mod_loaded\",\"modId\":\"basemetals\"}",
                "{\"type\":\"forge:mod_loaded\",\"modid\":\"basemetals\",\"extra\":true}"))
            assertFalse(condition, baseGuard(json(condition)));
    }

    @Test public void nativeForge37SerializerRetainsTheExactProviderIdentity() {
        JsonObject guard = json("{\"type\":\"forge:mod_loaded\",\"modid\":\"basemetals\"}");
        ModLoadedCondition condition = ModLoadedCondition.Serializer.INSTANCE.read(guard);
        assertEquals("forge:mod_loaded", ModLoadedCondition.Serializer.INSTANCE.getID().toString());
        JsonObject encoded = new JsonObject();
        ModLoadedCondition.Serializer.INSTANCE.write(encoded, condition);
        assertEquals("basemetals", encoded.get("modid").getAsString());
        assertThrows(RuntimeException.class, () -> ModLoadedCondition.Serializer.INSTANCE.read(json("{}")));
    }

    @Test public void processingInputsOutputsAndQuantitiesRemainExactlyTheSame() throws Exception {
        Map<String, JsonObject> recipes = crushingRecipes();
        for (String machine : MACHINES) {
            JsonObject recipe = recipes.get("crushing_" + machine + ".json");
            assertEquals("Only the original payload plus conditions is expected", 4, recipe.size());
            JsonObject ingredient = recipe.getAsJsonObject("ingredient");
            assertEquals(1, ingredient.size());
            assertEquals("basesteam:" + machine, ingredient.get("item").getAsString());
            JsonObject output = recipe.getAsJsonObject("result");
            assertEquals(machine.equals("steam_tank") ? 1 : 2, output.size());
            assertEquals(machine.equals("steam_governor") ? "basepower:sprocket"
                    : "basesteam:steam_governor", output.get("item").getAsString());
            int amount = machine.equals("steam_tank") ? 1 : machine.equals("steam_elevator") ? 3 : 2;
            assertEquals(!machine.equals("steam_tank"), output.has("count"));
            assertEquals(amount, output.has("count") ? output.get("count").getAsInt() : 1);
        }
    }
}
