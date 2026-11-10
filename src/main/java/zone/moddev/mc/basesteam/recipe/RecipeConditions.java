package zone.moddev.mc.basesteam.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import zone.moddev.mc.basepower.PowerAdvantage;
import zone.moddev.mc.basepower.RecipeMode;
import zone.moddev.mc.basesteam.SteamAdvantage;
import java.util.Locale;

/** Registers the Forge data-pack conditions used by Steam recipes. */
public final class RecipeConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.CONDITION_SERIALIZERS, SteamAdvantage.MODID);

    static {
        SERIALIZERS.register("recipe_mode", () -> ModeCondition.CODEC);
        SERIALIZERS.register("musket_enabled", () -> MusketCondition.CODEC);
    }

    private RecipeConditions() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    private record ModeCondition(RecipeMode mode) implements ICondition {
        private static final MapCodec<ModeCondition> CODEC = Codec.STRING
                .comapFlatMap(ModeCondition::parseMode, RecipeMode::name)
                .fieldOf("mode").xmap(ModeCondition::new, ModeCondition::mode);

        private static DataResult<RecipeMode> parseMode(String value) {
            try {
                return DataResult.success(RecipeMode.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return DataResult.error(() -> "Unknown baseSteam recipe mode: " + value);
            }
        }

        @Override public MapCodec<ModeCondition> codec() { return CODEC; }
        @Override public boolean test(IContext context, DynamicOps<?> ops) {
            return PowerAdvantage.recipeMode == mode;
        }
    }

    private record MusketCondition() implements ICondition {
        private static final MapCodec<MusketCondition> CODEC = MapCodec.unit(new MusketCondition());

        @Override public MapCodec<MusketCondition> codec() { return CODEC; }
        @Override public boolean test(IContext context, DynamicOps<?> ops) {
            return SteamAdvantage.MUSKET_ENABLE;
        }
    }
}
