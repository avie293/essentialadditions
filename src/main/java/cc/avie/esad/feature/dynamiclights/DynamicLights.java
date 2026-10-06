package cc.avie.esad.feature.dynamiclights;

import cc.avie.esad.config.EsadConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dynamic lights like LambDynamicLights: entities holding or being a light source light up the blocks
 * around them, without changing the real light of the world.
 * <p>
 * Every few ticks the light sources near the player are collected. Sections around sources that
 * appeared, moved or disappeared are rebuilt, and while they are built the light lookup of the renderer
 * adds {@link #lightAt} on top of the vanilla block light. Section builds run on worker threads, so they
 * only ever read the immutable {@link #sources} snapshot.
 */
public final class DynamicLights {
	/** Distance at which a light source with luminance 15 fades out completely. */
	private static final double MAX_RADIUS = 7.75;
	private static final double MAX_RADIUS_SQR = MAX_RADIUS * MAX_RADIUS;
	/** Packed light of fully lit blocks (e.g. emissive ones), nothing to add there. */
	private static final int FULL_BRIGHT = 0xF000F0;

	/** Items that give light without being a light emitting block. */
	private static final Map<String, Integer> ITEM_LIGHT = Map.of(
		"lava_bucket", 15,
		"blaze_rod", 10,
		"blaze_powder", 8,
		"fire_charge", 10,
		"glowstone_dust", 8,
		"glow_ink_sac", 10,
		"glow_berries", 8,
		"magma_cream", 8,
		"spectral_arrow", 8,
		"nether_star", 8
	);
	/** Items that go out under water. */
	private static final Set<String> WATER_SENSITIVE = Set.of(
		"torch", "soul_torch", "copper_torch", "campfire", "soul_campfire", "lava_bucket", "fire_charge", "blaze_powder"
	);
	/** Entities that glow by themselves. */
	private static final Map<String, Integer> ENTITY_LIGHT = Map.of(
		"blaze", 10,
		"magma_cube", 8,
		"glow_squid", 8,
		"fireball", 14,
		"small_fireball", 14,
		"dragon_fireball", 14,
		"spectral_arrow", 8,
		"tnt", 10
	);

	/** Sources the current section meshes were built with, read by the render worker threads. */
	private static volatile LightSource[] sources = new LightSource[0];
	/** Same sources by entity id, only used on the client thread. */
	private static Map<Integer, LightSource> sourcesById = new HashMap<>();
	private static int ticksSinceUpdate;

	private DynamicLights() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(DynamicLights::tick);
	}

	// ---------------------------------------------------------------- light lookup (any thread)

	/** Dynamic block light (0 to 15) at a position, the strongest of all sources. */
	public static double lightAt(double x, double y, double z) {
		LightSource[] current = sources;
		double light = 0;
		for (LightSource source : current) {
			double dx = source.x() - x;
			double dy = source.y() - y;
			double dz = source.z() - z;
			double distanceSqr = dx * dx + dy * dy + dz * dz;
			if (distanceSqr < MAX_RADIUS_SQR) {
				light = Math.max(light, source.luminance() * (1 - Math.sqrt(distanceSqr) / MAX_RADIUS));
			}
		}
		return light;
	}

	/** Adds the dynamic light at the center of a block to packed light coordinates. */
	public static int applyToBlock(int packedLight, BlockPos pos) {
		if (sources.length == 0 || packedLight == FULL_BRIGHT) {
			return packedLight;
		}
		return withBlockLight(packedLight, lightAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
	}

	/**
	 * Raises the block light of packed light coordinates to the given level, if it is brighter.
	 * The block light is stored as level * 16 in the lowest byte, so fractions give a smooth fade.
	 */
	public static int withBlockLight(int packedLight, double light) {
		if (light <= 0) {
			return packedLight;
		}
		int dynamic = Math.min(240, (int) (light * 16));
		int vanilla = packedLight & 0xFF;
		return dynamic > vanilla ? (packedLight & ~0xFF) | dynamic : packedLight;
	}

	public static boolean hasSources() {
		return sources.length > 0;
	}

	// ---------------------------------------------------------------- updating (client thread)

	private static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		if (level == null || minecraft.player == null || !EsadConfig.DYNAMIC_LIGHTS_ENABLED.get()) {
			clear(level);
			return;
		}
		if (++ticksSinceUpdate < EsadConfig.DYNAMIC_LIGHTS_UPDATE_RATE.get().interval()) {
			return;
		}
		ticksSinceUpdate = 0;

		double range = EsadConfig.DYNAMIC_LIGHTS_RANGE.get();
		Map<Integer, LightSource> next = new HashMap<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity.distanceToSqr(minecraft.player) > range * range) {
				continue;
			}
			int luminance = luminance(minecraft, entity);
			if (luminance <= 0) {
				continue;
			}
			LightSource source = new LightSource(entity.getId(), entity.getX(), entity.getEyeY(), entity.getZ(), luminance);
			LightSource old = sourcesById.get(source.entityId());
			if (old != null && !source.differsFrom(old)) {
				// Keeps the old position, so tiny movements add up until they are worth a rebuild
				next.put(old.entityId(), old);
			} else {
				next.put(source.entityId(), source);
				if (old != null) {
					setDirty(level, old);
				}
				setDirty(level, source);
			}
		}
		for (LightSource old : sourcesById.values()) {
			if (!next.containsKey(old.entityId())) {
				setDirty(level, old);
			}
		}

		sourcesById = next;
		sources = next.values().toArray(new LightSource[0]);
	}

	private static void clear(ClientLevel level) {
		if (sourcesById.isEmpty()) {
			return;
		}
		if (level != null) {
			sourcesById.values().forEach(source -> setDirty(level, source));
		}
		sourcesById = new HashMap<>();
		sources = new LightSource[0];
	}

	/** Rebuilds every section the light of a source reaches, including the block next to it for smooth lighting. */
	private static void setDirty(ClientLevel level, LightSource source) {
		double reach = MAX_RADIUS + 1;
		level.setSectionRangeDirty(
			SectionPos.blockToSectionCoord(Mth.floor(source.x() - reach)),
			SectionPos.blockToSectionCoord(Mth.floor(source.y() - reach)),
			SectionPos.blockToSectionCoord(Mth.floor(source.z() - reach)),
			SectionPos.blockToSectionCoord(Mth.floor(source.x() + reach)),
			SectionPos.blockToSectionCoord(Mth.floor(source.y() + reach)),
			SectionPos.blockToSectionCoord(Mth.floor(source.z() + reach))
		);
	}

	// ---------------------------------------------------------------- luminance

	private static int luminance(Minecraft minecraft, Entity entity) {
		if (entity.isSpectator() || (entity == minecraft.player && !EsadConfig.DYNAMIC_LIGHTS_SELF.get())) {
			return 0;
		}
		int light = 0;
		if (EsadConfig.DYNAMIC_LIGHTS_BURNING.get() && entity.isOnFire()) {
			light = 15;
		}
		if (EsadConfig.DYNAMIC_LIGHTS_ENTITIES.get()) {
			light = Math.max(light, entityLuminance(entity));
		}
		if (EsadConfig.DYNAMIC_LIGHTS_HELD_ITEMS.get() && entity instanceof LivingEntity living) {
			boolean underWater = living.isUnderWater();
			light = Math.max(light, itemLuminance(living.getMainHandItem(), underWater));
			light = Math.max(light, itemLuminance(living.getOffhandItem(), underWater));
		}
		if (EsadConfig.DYNAMIC_LIGHTS_DROPPED_ITEMS.get() && entity instanceof ItemEntity item) {
			light = Math.max(light, itemLuminance(item.getItem(), item.isUnderWater()));
		}
		return Math.min(light, 15);
	}

	private static int entityLuminance(Entity entity) {
		if (entity instanceof Creeper creeper) {
			// Brightens while it is about to explode
			return (int) (creeper.getSwelling(0) * 10);
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return isVanilla(id) ? ENTITY_LIGHT.getOrDefault(id.getPath(), 0) : 0;
	}

	private static int itemLuminance(ItemStack stack, boolean underWater) {
		if (stack.isEmpty()) {
			return 0;
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (isVanilla(id)) {
			if (underWater && EsadConfig.DYNAMIC_LIGHTS_WATER_SENSITIVE.get() && WATER_SENSITIVE.contains(id.getPath())) {
				return 0;
			}
			Integer light = ITEM_LIGHT.get(id.getPath());
			if (light != null) {
				return light;
			}
		}
		if (stack.getItem() instanceof BlockItem blockItem) {
			return blockItem.getBlock().defaultBlockState().getLightEmission();
		}
		return 0;
	}

	private static boolean isVanilla(Identifier id) {
		return id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE);
	}
}
