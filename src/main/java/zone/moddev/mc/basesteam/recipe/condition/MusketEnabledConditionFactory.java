package zone.moddev.mc.basesteam.recipe.condition;

import com.google.gson.JsonObject;
import zone.moddev.mc.basesteam.SteamAdvantage;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class MusketEnabledConditionFactory implements IConditionFactory {
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		return () -> SteamAdvantage.MUSKET_ENABLE;
	}
}
