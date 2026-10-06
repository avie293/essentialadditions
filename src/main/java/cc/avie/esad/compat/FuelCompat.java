package cc.avie.esad.compat;

import cc.avie.esad.EssentialAdditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Version specific: before 26.3 the burn time comes from Level.fuelValues().
 * <p>
 * Since 26.3 the burn time is a {@code cooking_fuel} component that usually points to a
 * {@code context_int_provider} like {@code minecraft:cooking/time_coal}. That registry is not sent to
 * the client, so the files are read from the game and mod jars instead and evaluated for a normal
 * furnace. Changes made by data packs on a server are therefore not shown.
 */
public final class FuelCompat {
	private static final int UNKNOWN = 0;
	private static final Map<Identifier, Integer> CACHE = new ConcurrentHashMap<>();

	private FuelCompat() {
	}

	/** Burn time of a furnace fuel in ticks in a normal furnace, 0 when it is no fuel or unknown. */
	public static int burnTime(ItemStack stack) {
		CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null) {
			return UNKNOWN;
		}
		ResolvableInt burnTime = fuel.burnTime();
		if (burnTime instanceof ResolvableInt.Constant constant) {
			return constant.value();
		}
		if (burnTime instanceof ResolvableInt.Reference reference) {
			return Math.max(UNKNOWN, provider(reference.key().identifier(), 0));
		}
		return UNKNOWN;
	}

	private static int provider(Identifier id, int depth) {
		Integer cached = CACHE.get(id);
		if (cached != null) {
			return cached;
		}
		int value = depth > 8 ? -1 : evaluate(load(id), depth + 1);
		CACHE.put(id, value);
		return value;
	}

	private static JsonElement load(Identifier id) {
		String path = "data/" + id.getNamespace() + "/context_int_provider/" + id.getPath() + ".json";
		try (InputStream stream = FuelCompat.class.getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				return null;
			}
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (Exception e) {
			EssentialAdditions.LOGGER.debug("Could not read fuel value {}", id, e);
			return null;
		}
	}

	/** Evaluates the provider shapes vanilla uses for fuels, -1 for anything else. */
	private static int evaluate(JsonElement json, int depth) {
		if (json == null) {
			return -1;
		}
		if (json.isJsonPrimitive()) {
			if (json.getAsJsonPrimitive().isNumber()) {
				return json.getAsInt();
			}
			Identifier reference = Identifier.tryParse(json.getAsString());
			return reference == null ? -1 : provider(reference, depth);
		}
		if (!json.isJsonObject()) {
			return -1;
		}
		JsonObject object = json.getAsJsonObject();
		String type = object.has("type") ? object.get("type").getAsString() : "";
		return switch (type) {
			case "minecraft:constant" -> evaluate(object.get("value"), depth);
			// The condition is the fast cooking of blast furnaces and smokers, a normal furnace is shown
			case "minecraft:conditional" -> evaluate(object.get("on_false"), depth);
			case "minecraft:div" -> {
				int left = evaluate(object.get("left"), depth);
				int right = evaluate(object.get("right"), depth);
				yield left < 0 || right <= 0 ? -1 : left / right;
			}
			default -> -1;
		};
	}
}
