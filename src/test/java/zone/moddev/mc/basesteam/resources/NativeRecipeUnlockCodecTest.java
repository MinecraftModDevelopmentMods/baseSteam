package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

/** Exact criterion/reference codec proof; production recipe bodies and availability have separate native gates. */
public class NativeRecipeUnlockCodecTest {
    private static final Path ROOT = Path.of("src/main/resources/data/basesteam/advancement/recipes");
    private static List<Path> files;
    private static RegistryOps<JsonElement> ops;
    private static Map<String, ResourceKey<Recipe<?>>> keys;
    private static Map<ResourceKey<Recipe<?>>, Recipe<?>> values;
    private static ResourceKey<Recipe<?>> unrelated;

    @BeforeClass public static void bootstrap() throws Exception {
        NativeTestBootstrap.registerCraftingSerializers();
        try (var paths = Files.list(ROOT)) { files = paths.filter(p -> p.toString().endsWith(".json")).sorted().toList(); }
        assertEquals(24, files.size());
        var world = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        var nativeRecipes = net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(world)
                .lookupOrThrow(Registries.RECIPE).listElements().limit(files.size() + 1L).toList();
        assertEquals(files.size() + 1, nativeRecipes.size());
        keys = new LinkedHashMap<>();
        values = new LinkedHashMap<>();
        for (int i = 0; i < files.size(); i++) {
            var id = "basesteam:" + files.get(i).getFileName().toString().replace(".json", "");
            var key = ResourceKey.create(Registries.RECIPE, Identifier.parse(id));
            keys.put(id, key);
            // Real distinct vanilla recipes supply values; the tested contract here is exact generated reference identity.
            values.put(key, nativeRecipes.get(i).value());
        }
        unrelated = ResourceKey.create(Registries.RECIPE, Identifier.parse("basesteam_test:unrelated"));
        values.put(unrelated, nativeRecipes.getLast().value());
        var lookup = new RegistrySetBuilder().add(Registries.RECIPE,
                context -> values.forEach(context::register)).build(world);
        ops = lookup.createSerializationContext(JsonOps.INSTANCE);
    }

    @Test public void everyGeneratedCriterionDecodesAndMatchesOnlyItsExactNativeRecipeHolder() throws Exception {
        for (Path file : files) {
            String id = "basesteam:" + file.getFileName().toString().replace(".json", "");
            JsonObject condition = condition(file);
            var parsed = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(ops, condition).getOrThrow();
            assertEquals(1, parsed.recipes().size());
            assertEquals(keys.get(id), parsed.recipes().get(0).unwrapKey().orElseThrow());
            assertTrue(parsed.matches(new RecipeHolder<>(keys.get(id), values.get(keys.get(id)))));
            assertFalse(parsed.matches(new RecipeHolder<>(unrelated, values.get(unrelated))));
            JsonObject canonical = new JsonObject();
            canonical.addProperty("recipes", id);
            assertEquals("Native singleton HolderSets encode as one reference, not an array", canonical,
                    RecipeUnlockedTrigger.TriggerInstance.CODEC.encodeStart(ops, parsed).getOrThrow());
            var roundTrip = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(ops, canonical).getOrThrow();
            assertEquals(1, roundTrip.recipes().size());
            assertEquals(keys.get(id), roundTrip.recipes().get(0).unwrapKey().orElseThrow());
        }
    }

    @Test public void nativeCodecRejectsOldSingularFieldAndMissingRecipeReferencesWithoutDroppingTheCriterion() throws Exception {
        JsonObject valid = condition(files.getFirst());
        JsonObject old = valid.deepCopy();
        old.add("recipe", old.remove("recipes").getAsJsonArray().get(0));
        JsonObject missing = valid.deepCopy();
        var ids = new com.google.gson.JsonArray();
        ids.add("missing_mod:unregistered_recipe");
        missing.add("recipes", ids);
        for (JsonObject invalid : List.of(old, missing, new JsonObject())) {
            var rejected = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(ops, invalid);
            assertTrue(rejected.error().isPresent());
            assertTrue(rejected.result().isEmpty());
        }
    }

    private static JsonObject condition(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject()
                .getAsJsonObject("criteria").getAsJsonObject("has_recipe").getAsJsonObject("conditions");
    }
}
