package zone.moddev.mc.basesteam.resources;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.Items;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecipeUnlockContractTest {
    private static final String MOD = "basesteam";
    private static final Path DATA = Paths.get("src/main/resources/data", MOD);
    private static final Set<String> IGNORED = new LinkedHashSet<>(java.util.Arrays.asList(
            "item:minecraft:stick", "item:minecraft:string", "item:minecraft:feather", "tag:forge:rods/wooden"));

    @BeforeClass public static void bootstrapNativeRegistries() {
        NativeTestBootstrap.start();
    }

    @Test public void everyCraftingRecipeHasExactlyOneUnlockAndProcessingHasNone() throws IOException {
        Set<String> expected = new LinkedHashSet<>();
        for (Path file : files(DATA.resolve("recipe"))) {
            if (crafting(read(file))) expected.add(file.getFileName().toString());
        }
        Set<String> actual = files(DATA.resolve("advancement/recipes")).stream()
                .map(file -> file.getFileName().toString()).collect(Collectors.toSet());
        assertFalse("No crafting recipes found", expected.isEmpty());
        assertEquals("Missing or orphaned recipe unlocks", expected, actual);
    }

    @Test public void discoveriesAreSingleIngredientAlternativesNotAnEntireCraftingGrid() throws IOException {
        for (Path file : files(DATA.resolve("recipe"))) {
            JsonObject recipe = read(file);
            if (!crafting(recipe)) continue;
            JsonObject unlock = read(DATA.resolve("advancement/recipes").resolve(file.getFileName()));
            assertEquals("minecraft:recipes/root", unlock.get("parent").getAsString());
            assertFalse("Recipe unlocks should not create visible advancement tabs", unlock.has("display"));
            JsonObject criteria = unlock.getAsJsonObject("criteria");
            Set<String> actual = new LinkedHashSet<>();
            for (Map.Entry<String, JsonElement> entry : criteria.entrySet()) {
                if (entry.getKey().equals("has_recipe")) continue;
                JsonObject criterion = entry.getValue().getAsJsonObject();
                assertEquals(file.toString(), "minecraft:inventory_changed", criterion.get("trigger").getAsString());
                JsonArray items = criterion.getAsJsonObject("conditions").getAsJsonArray("items");
                assertEquals("Finding ONE ingredient must be enough: " + file, 1, items.size());
                JsonObject predicate = items.get(0).getAsJsonObject();
                assertEquals("Only an item or tag predicate is expected", 1, predicate.size());
                actual.add(predicateKey(predicate));
            }
            List<JsonElement> choices = new ArrayList<>();
            if (recipe.has("key")) {
                for (Map.Entry<String, JsonElement> entry : recipe.getAsJsonObject("key").entrySet())
                    collect(entry.getValue(), choices);
            } else collect(recipe.has("ingredients") ? recipe.get("ingredients") : recipe.get("ingredient"), choices);
            Set<String> expected = choices.stream().map(RecipeUnlockContractTest::ingredientKey)
                    .filter(key -> !IGNORED.contains(key)).collect(Collectors.toCollection(LinkedHashSet::new));
            if (expected.isEmpty()) for (JsonElement choice : choices) expected.add(ingredientKey(choice));
            assertEquals("Trigger must correspond to a relevant recipe ingredient: " + file, expected, actual);
            JsonArray requirements = unlock.getAsJsonArray("requirements");
            assertEquals("All criteria must form one OR group", 1, requirements.size());
            Set<String> alternatives = new LinkedHashSet<>();
            for (JsonElement key : requirements.get(0).getAsJsonArray()) alternatives.add(key.getAsString());
            assertEquals(criteria.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()), alternatives);
        }
    }

    @Test public void rewardAndRecipeCommandCriterionReferToTheExactVariant() throws IOException {
        for (Path file : files(DATA.resolve("advancement/recipes"))) {
            String id = MOD + ":" + file.getFileName().toString().replace(".json", "");
            JsonObject unlock = read(file);
            JsonArray recipes = unlock.getAsJsonObject("rewards").getAsJsonArray("recipes");
            assertEquals(1, recipes.size());
            assertEquals(id, recipes.get(0).getAsString());
            JsonObject criterion = unlock.getAsJsonObject("criteria").getAsJsonObject("has_recipe");
            assertEquals("minecraft:recipe_unlocked", criterion.get("trigger").getAsString());
            assertEquals(id, criterion.getAsJsonObject("conditions").get("recipe").getAsString());
            assertFalse("Forge recipe conditions are not valid advancement conditions", unlock.has("conditions"));
        }
    }

    @Test public void recipeBookGroupsIdentifyTheOutputNotABroadMachineCategory() throws IOException {
        for (Path file : files(DATA.resolve("recipe"))) {
            JsonObject recipe = read(file);
            if (!crafting(recipe)) continue;
            JsonObject result = recipe.getAsJsonObject("result");
            assertFalse("Legacy recipe result item field", result.has("item"));
            String output = result.get("id").getAsString();
            assertTrue("Missing output-specific group: " + file, recipe.has("group"));
            assertEquals("Different items must not share a recipe-book cell: " + file,
                    output, recipe.get("group").getAsString());
        }
    }

    @Test public void nativeSingletonPredicatesKeepRecipeIngredientSemantics() {
        JsonElement ingredient = new com.google.gson.JsonPrimitive("minecraft:string");
        JsonObject predicate = new JsonParser().parse("{\"items\":[\"minecraft:string\"]}").getAsJsonObject();
        assertEquals("item:minecraft:string", predicateKey(predicate));
        assertEquals(ingredientKey(ingredient), predicateKey(predicate));
        JsonElement tag = new com.google.gson.JsonPrimitive("#minecraft:planks");
        JsonObject nativeTag = new JsonParser().parse("{\"items\":\"#minecraft:planks\"}").getAsJsonObject();
        assertEquals("tag:minecraft:planks", predicateKey(nativeTag));
        assertEquals(ingredientKey(tag), predicateKey(nativeTag));
        assertThrows("Legacy item predicates must not compare equal to native predicates",
                AssertionError.class, () -> predicateKey(new JsonParser().parse("{\"item\":\"minecraft:string\"}").getAsJsonObject()));
    }

    @Test public void malformedOrBroadItemPredicatesAreRejected() {
        for (String json : new String[]{"{}", "{\"items\":[]}",
                "{\"items\":[\"minecraft:string\",\"minecraft:stone\"]}",
                "{\"items\":\"minecraft:string\"}", "{\"items\":[1]}", "{\"items\":[null]}",
                "{\"tag\":\"minecraft:planks\"}", "{\"items\":\"#\"}",
                "{\"items\":[\"minecraft:string\"],\"item\":\"minecraft:stone\"}",
                "{\"items\":[\"minecraft:string\"],\"tag\":\"minecraft:planks\"}"}) {
            JsonObject predicate = new JsonParser().parse(json).getAsJsonObject();
            assertThrows(json, AssertionError.class, () -> predicateKey(predicate));
        }
    }

    @Test public void nativeMusketUnlockPredicateRejectsUnrelatedInventory() throws IOException {
        JsonObject unlock = read(DATA.resolve("advancement/recipes/musket_right_normal.json"));
        JsonObject predicate = unlock.getAsJsonObject("criteria").getAsJsonObject("has_ingredient_1")
                .getAsJsonObject("conditions").getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals("item:minecraft:flint_and_steel", predicateKey(predicate));
        ItemPredicate nativePredicate = decode(predicate);
        var accepted = nativePredicate.items().orElseThrow();
        assertTrue(accepted.contains(Items.FLINT_AND_STEEL.builtInRegistryHolder()));
        assertFalse("String alone must not unlock a musket", accepted.contains(Items.STRING.builtInRegistryHolder()));
        assertFalse("Unrelated inventory must not act as an ANY predicate",
                accepted.contains(Items.STONE.builtInRegistryHolder()));
    }

    @Test public void nativeParserNegativeControlExposesIgnoredLegacyItemField() {
        JsonObject legacy = new JsonParser().parse("{\"item\":\"minecraft:string\"}").getAsJsonObject();
        ItemPredicate unrestricted = decode(legacy);
        assertTrue("The old field is ignored by the native parser", unrestricted.items().isEmpty());
        assertThrows(AssertionError.class, () -> predicateKey(legacy));
        JsonObject singleton = new JsonParser().parse("{\"items\":[\"minecraft:string\"]}").getAsJsonObject();
        ItemPredicate restricted = decode(singleton);
        var accepted = restricted.items().orElseThrow();
        assertTrue(accepted.contains(Items.STRING.builtInRegistryHolder()));
        assertFalse("Native singleton must not become ANY", accepted.contains(Items.STONE.builtInRegistryHolder()));
    }

    private static ItemPredicate decode(JsonObject json) {
        return ItemPredicate.CODEC.parse(NativeTestBootstrap.registries().createSerializationContext(JsonOps.INSTANCE), json)
                .getOrThrow();
    }

    private static boolean crafting(JsonObject recipe) {
        String type = recipe.get("type").getAsString();
        return type.equals("minecraft:crafting_shaped") || type.equals("minecraft:crafting_shapeless")
                || type.equals("minecraft:smelting");
    }

    private static String predicateKey(JsonObject predicate) {
        assertEquals("Only one native item or tag predicate is expected", 1, predicate.size());
        assertFalse("Native advancement predicates must not use the ignored legacy item field", predicate.has("item"));
        assertFalse("Native tag predicates must also use the items field", predicate.has("tag"));
        JsonElement selection = predicate.get("items");
        if (selection != null && selection.isJsonPrimitive() && selection.getAsJsonPrimitive().isString()) {
            String tag = selection.getAsString();
            assertTrue("Native tag predicate must use a #namespace:path", tag.matches("#[a-z0-9_.-]+:[a-z0-9/_.-]+"));
            return "tag:" + tag.substring(1);
        }
        assertTrue("Native item predicate requires an items array", predicate.has("items") && predicate.get("items").isJsonArray());
        JsonArray items = predicate.getAsJsonArray("items");
        assertEquals("Discovery must identify exactly one recipe ingredient", 1, items.size());
        JsonElement item = items.get(0);
        assertTrue("Item predicate must contain a string ID", item.isJsonPrimitive() && item.getAsJsonPrimitive().isString());
        return "item:" + item.getAsString();
    }

    private static String ingredientKey(JsonElement ingredient) {
        assertTrue("Native recipe ingredient must contain a string ID",
                ingredient.isJsonPrimitive() && ingredient.getAsJsonPrimitive().isString());
        String value = ingredient.getAsString();
        return value.startsWith("#") ? "tag:" + value.substring(1) : "item:" + value;
    }

    private static void collect(JsonElement element, List<JsonElement> result) {
        if (element.isJsonArray()) for (JsonElement child : element.getAsJsonArray()) collect(child, result);
        else result.add(element);
    }

    private static JsonObject read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private static List<Path> files(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return java.util.Collections.emptyList();
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(path -> path.toString().endsWith(".json")).sorted().collect(Collectors.toList());
        }
    }
}
