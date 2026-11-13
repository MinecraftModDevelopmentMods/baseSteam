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
import java.util.Optional;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

/** Actual criterion/tag codecs; production recipe bodies and mode restore retain separate server gates. */
public class NativeRecipeUnlockCodecTest {
    private static final Path ROOT = Path.of("src/main/resources/data/basesteam/advancement/recipes");
    private static List<Path> files;
    private static final Path TAGS = Path.of("src/main/resources/data/basesteam/tags/recipe/discovery");
    private static RegistryOps<JsonElement> active;
    private static RegistryOps<JsonElement> inactive;
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
        active = lookup(world, true);
        inactive = lookup(world, false);
    }

    private static RegistryOps<JsonElement> lookup(HolderLookup.Provider world, boolean present) throws Exception {
        var registry = new MappedRegistry<Recipe<?>>(Registries.RECIPE, com.mojang.serialization.Lifecycle.stable());
        values.forEach((key, value) -> {
            if (present || key.equals(unrelated)) registry.register(key, value, RegistrationInfo.BUILT_IN);
        });
        registry.freeze();
        Map<Identifier, List<TagLoader.EntryWithSource>> definitions = new LinkedHashMap<>();
        for (Path file : files) {
            var tag = TagFile.CODEC.parse(JsonOps.INSTANCE,
                    JsonParser.parseString(Files.readString(TAGS.resolve(file.getFileName())))).getOrThrow();
            definitions.put(tagKey(file).location(), tag.entries().stream()
                    .map(entry -> new TagLoader.EntryWithSource(entry, "actual-generated-discovery-tag")).toList());
        }
        var otherTag = Identifier.parse("basesteam_test:unrelated_discovery");
        definitions.put(otherTag, List.of(new TagLoader.EntryWithSource(TagEntry.element(unrelated.identifier()), "negative-control")));
        var loader = new TagLoader<Holder<Recipe<?>>>((id, required) -> registry.get(id), "tags/recipe");
        Map<TagKey<Recipe<?>>, List<Holder<Recipe<?>>>> tags = new LinkedHashMap<>();
        loader.build(definitions).forEach((id, holders) -> tags.put(TagKey.create(Registries.RECIPE, id), holders));
        assertEquals(25, tags.size());
        registry.prepareTagReload(new TagLoader.LoadResult<>(Registries.RECIPE, tags)).apply();
        return HolderLookup.Provider.create(java.util.stream.Stream.concat(
                world.listRegistries().filter(lookup -> !lookup.key().equals(Registries.RECIPE)),
                java.util.stream.Stream.of(registry))).createSerializationContext(JsonOps.INSTANCE);
    }

    @Test public void everyGeneratedCriterionKeepsItsNamedTagWithAnActiveRecipeOrNoRecipeHolder() throws Exception {
        for (Path file : files) {
            String id = "basesteam:" + file.getFileName().toString().replace(".json", "");
            JsonObject condition = condition(file);
            var parsed = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(active, condition).getOrThrow();
            assertEquals(Optional.of(tagKey(file)), parsed.recipes().unwrapKey());
            assertEquals(1, parsed.recipes().size());
            assertEquals(keys.get(id), parsed.recipes().get(0).unwrapKey().orElseThrow());
            assertTrue(parsed.matches(new RecipeHolder<>(keys.get(id), values.get(keys.get(id)))));
            assertFalse(parsed.matches(new RecipeHolder<>(unrelated, values.get(unrelated))));
            assertEquals(condition, RecipeUnlockedTrigger.TriggerInstance.CODEC.encodeStart(active, parsed).getOrThrow());
            var empty = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(inactive, condition).getOrThrow();
            assertEquals(Optional.of(tagKey(file)), empty.recipes().unwrapKey());
            assertEquals(0, empty.recipes().size());
            assertFalse(empty.matches(new RecipeHolder<>(keys.get(id), values.get(keys.get(id)))));
            assertFalse(empty.matches(new RecipeHolder<>(unrelated, values.get(unrelated))));
            assertEquals("An inactive recipe must retain the criterion and exact tag identity", condition,
                    RecipeUnlockedTrigger.TriggerInstance.CODEC.encodeStart(inactive, empty).getOrThrow());
            assertTrue(inactive.getter(Registries.RECIPE).orElseThrow().get(keys.get(id)).isEmpty());
        }
    }

    @Test public void nativeCodecRejectsOldSingularFieldAndMissingRecipeReferencesWithoutDroppingTheCriterion() throws Exception {
        JsonObject valid = condition(files.getFirst());
        JsonObject old = valid.deepCopy();
        old.add("recipe", old.remove("recipes"));
        JsonObject missing = valid.deepCopy();
        var ids = new com.google.gson.JsonArray();
        ids.add("missing_mod:unregistered_recipe");
        missing.add("recipes", ids);
        JsonObject missingTag = valid.deepCopy();
        missingTag.addProperty("recipes", "#missing_mod:unregistered_discovery_tag");
        for (JsonObject invalid : List.of(old, missing, missingTag, new JsonObject())) {
            var rejected = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(active, invalid);
            assertTrue(rejected.error().isPresent());
            assertTrue(rejected.result().isEmpty());
        }
    }

    @Test public void unrelatedNamedRecipeTagsCannotMatchTheGeneratedRecipeIdentity() throws Exception {
        for (Path file : files) {
            String id = "basesteam:" + file.getFileName().toString().replace(".json", "");
            JsonObject other = condition(file).deepCopy();
            other.addProperty("recipes", "#basesteam_test:unrelated_discovery");
            var parsed = RecipeUnlockedTrigger.TriggerInstance.CODEC.parse(active, other).getOrThrow();
            assertFalse(parsed.matches(new RecipeHolder<>(keys.get(id), values.get(keys.get(id)))));
            assertTrue(parsed.matches(new RecipeHolder<>(unrelated, values.get(unrelated))));
            assertEquals(other, RecipeUnlockedTrigger.TriggerInstance.CODEC.encodeStart(active, parsed).getOrThrow());
        }
    }

    @Test public void actualTagEntryCodecRetainsOptionalIdentityAndRejectsRequiredMissingElements() throws Exception {
        var missing = new TagEntry.Lookup<String>() {
            @Override public String element(Identifier id, boolean required) { return null; }
            @Override public java.util.Collection<String> tag(Identifier id) { return null; }
        };
        for (Path file : files) {
            String id = "basesteam:" + file.getFileName().toString().replace(".json", "");
            var source = JsonParser.parseString(Files.readString(TAGS.resolve(file.getFileName()))).getAsJsonObject();
            var entry = source.getAsJsonArray("values").get(0).getAsJsonObject();
            assertEquals(id, entry.get("id").getAsString());
            assertFalse(entry.get("required").getAsBoolean());
            var decoded = TagEntry.CODEC.parse(JsonOps.INSTANCE, entry).getOrThrow();
            assertEquals(entry, TagEntry.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow());
            var output = new java.util.ArrayList<String>();
            assertTrue(decoded.build(missing, output::add));
            assertTrue(output.isEmpty());
            var required = entry.deepCopy();
            required.addProperty("required", true);
            assertFalse(TagEntry.CODEC.parse(JsonOps.INSTANCE, required).getOrThrow().build(missing, output::add));
            assertTrue(output.isEmpty());
        }
    }

    private static TagKey<Recipe<?>> tagKey(Path file) {
        return TagKey.create(Registries.RECIPE, Identifier.parse("basesteam:discovery/"
                + file.getFileName().toString().replace(".json", "")));
    }

    private static JsonObject condition(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject()
                .getAsJsonObject("criteria").getAsJsonObject("has_recipe").getAsJsonObject("conditions");
    }
}
