package cc.avie.esad.feature.tooltips;

import cc.avie.esad.compat.FuelCompat;
import cc.avie.esad.config.EsadConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Extra tooltip lines: durability, food values, burn time, repair cost, enchantment descriptions,
 * mod name, item id and a map preview. Every info can be shown always, only with Shift or never.
 */
public final class AdvancedTooltips {
	/** Ticks a furnace needs to smelt one item. */
	private static final int SMELT_TIME = 200;

	private AdvancedTooltips() {
	}

	public static void register() {
		ItemTooltipCallback.EVENT.register(AdvancedTooltips::addLines);
		ClientTooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof MapTooltip map) {
				return new ClientMapTooltip(map);
			}
			return data instanceof ContainerTooltip container ? new ClientContainerTooltip(container) : null;
		});
		EnderChestCache.register();
	}

	private static void addLines(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
		if (!EsadConfig.TOOLTIPS_ENABLED.get() || stack.isEmpty()) {
			return;
		}
		boolean hidden = false;

		hidden |= addEnchantmentDescriptions(stack, lines);

		TooltipMode durability = EsadConfig.TOOLTIP_DURABILITY.get();
		// Vanilla already shows the durability with advanced tooltips (F3 + H)
		if (stack.isDamageableItem() && !(flag.isAdvanced() && stack.isDamaged())
			&& (stack.isDamaged() || EsadConfig.TOOLTIP_DURABILITY_WHEN_FULL.get())) {
			if (durability.isVisible()) {
				addDurability(stack, lines);
			}
			hidden |= durability.isHiddenByShift();
		}

		TooltipMode food = EsadConfig.TOOLTIP_FOOD.get();
		FoodProperties properties = stack.get(DataComponents.FOOD);
		if (properties != null) {
			if (food.isVisible()) {
				lines.add(Component.translatable("tooltip.esad.food", properties.nutrition(), format(properties.saturation()))
					.withStyle(ChatFormatting.GRAY));
			}
			hidden |= food.isHiddenByShift();
		}

		TooltipMode fuel = EsadConfig.TOOLTIP_FUEL.get();
		if (fuel != TooltipMode.OFF) {
			int burnTime = FuelCompat.burnTime(stack);
			if (burnTime > 0) {
				if (fuel.isVisible()) {
					lines.add(Component.translatable("tooltip.esad.fuel", format(burnTime / 20.0), format(burnTime / (double) SMELT_TIME))
						.withStyle(ChatFormatting.GRAY));
				}
				hidden |= fuel.isHiddenByShift();
			}
		}

		TooltipMode repair = EsadConfig.TOOLTIP_REPAIR_COST.get();
		int repairCost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
		if (repairCost > 0) {
			if (repair.isVisible()) {
				lines.add(Component.translatable("tooltip.esad.repair_cost", repairCost).withStyle(ChatFormatting.GRAY));
			}
			hidden |= repair.isHiddenByShift();
		}

		hidden |= addToolInfo(stack, lines);

		TooltipMode enderChest = EsadConfig.TOOLTIP_ENDER_CHEST.get();
		if (stack.is(Items.ENDER_CHEST)) {
			if (enderChest.isVisible() && EnderChestCache.contents() == null) {
				lines.add(Component.translatable("tooltip.esad.ender_chest.unknown").withStyle(ChatFormatting.DARK_GRAY));
			}
			hidden |= enderChest.isHiddenByShift();
		}
		if (stack.has(DataComponents.CONTAINER)) {
			hidden |= EsadConfig.TOOLTIP_CONTAINER.get().isHiddenByShift();
		}

		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());

		TooltipMode modName = EsadConfig.TOOLTIP_MOD_NAME.get();
		// Mod Menu adds the mod name itself
		if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
			if (modName.isVisible()) {
				lines.add(Component.literal(modName(id.getNamespace())).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
			}
			hidden |= modName.isHiddenByShift();
		}

		TooltipMode itemId = EsadConfig.TOOLTIP_ITEM_ID.get();
		// Vanilla already shows the id with advanced tooltips (F3 + H)
		if (!flag.isAdvanced()) {
			if (itemId.isVisible()) {
				lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
			}
			hidden |= itemId.isHiddenByShift();
		}

		if (stack.has(DataComponents.MAP_ID)) {
			hidden |= EsadConfig.TOOLTIP_MAP.get().isHiddenByShift();
		}

		if (hidden && EsadConfig.TOOLTIP_SHIFT_HINT.get()) {
			lines.add(Component.translatable("tooltip.esad.shift_hint").withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	/**
	 * Adds a short description below every enchantment line.
	 *
	 * @return true when descriptions exist but are hidden until Shift is pressed
	 */
	private static boolean addEnchantmentDescriptions(ItemStack stack, List<Component> lines) {
		TooltipMode mode = EsadConfig.TOOLTIP_ENCHANTMENTS.get();
		ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (enchantments.isEmpty()) {
			enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		}
		if (mode == TooltipMode.OFF || enchantments.isEmpty()) {
			return false;
		}
		boolean found = false;
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
			String key = "enchantment." + entry.getKey().getRegisteredName().replace(':', '.') + ".esad_desc";
			if (!Language.getInstance().has(key)) {
				continue;
			}
			found = true;
			if (!mode.isVisible()) {
				continue;
			}
			String name = Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
			Component description = Component.literal("  ").append(Component.translatable(key)).withStyle(ChatFormatting.DARK_GRAY);
			int index = indexOf(lines, name);
			if (index >= 0) {
				lines.add(index + 1, description);
			}
		}
		return found && mode.isHiddenByShift();
	}

	/**
	 * Mining speed and mining level of tools.
	 *
	 * @return true when the info exists but is hidden until Shift is pressed
	 */
	private static boolean addToolInfo(ItemStack stack, List<Component> lines) {
		TooltipMode mode = EsadConfig.TOOLTIP_TOOL.get();
		Tool tool = stack.get(DataComponents.TOOL);
		if (mode == TooltipMode.OFF || tool == null) {
			return false;
		}
		float speed = tool.defaultMiningSpeed();
		String tier = null;
		for (Tool.Rule rule : tool.rules()) {
			if (rule.speed().isPresent()) {
				speed = Math.max(speed, rule.speed().get());
			}
			// Tiers deny the blocks they can not mine: "incorrect_for_iron_tool" means iron level
			if (rule.correctForDrops().isPresent() && !rule.correctForDrops().get()) {
				tier = rule.blocks().unwrapKey()
					.map(key -> key.location().getPath())
					.filter(path -> path.startsWith("incorrect_for_") && path.endsWith("_tool"))
					.map(path -> path.substring("incorrect_for_".length(), path.length() - "_tool".length()))
					.orElse(tier);
			}
		}
		if (speed <= 1 && tier == null) {
			// Shears, swords and similar items only have rules for a few blocks
			return false;
		}
		if (mode.isVisible()) {
			int efficiency = efficiencyLevel(stack);
			if (efficiency > 0) {
				lines.add(Component.translatable("tooltip.esad.tool.speed_efficiency", format(speed),
					format(speed + efficiency * efficiency + 1)).withStyle(ChatFormatting.GRAY));
			} else {
				lines.add(Component.translatable("tooltip.esad.tool.speed", format(speed)).withStyle(ChatFormatting.GRAY));
			}
			if (tier != null) {
				String key = "tooltip.esad.tool.tier." + tier;
				Component name = Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(tier);
				lines.add(Component.translatable("tooltip.esad.tool.tier", name).withStyle(ChatFormatting.GRAY));
			}
		}
		return mode.isHiddenByShift();
	}

	private static int efficiencyLevel(ItemStack stack) {
		ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
			if (entry.getKey().is(Enchantments.EFFICIENCY)) {
				return entry.getIntValue();
			}
		}
		return 0;
	}

	private static int indexOf(List<Component> lines, String text) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).getString().equals(text)) {
				return i;
			}
		}
		return -1;
	}

	/** Durability in the look of the Durability Tooltip mod: numbers, a bar or a text. */
	private static void addDurability(ItemStack stack, List<Component> lines) {
		int max = stack.getMaxDamage();
		int durability = max - stack.getDamageValue();
		boolean label = EsadConfig.TOOLTIP_DURABILITY_LABEL.get();
		DurabilityColor colorStyle = EsadConfig.TOOLTIP_DURABILITY_COLOR.get();
		ChatFormatting color = colorStyle.color(durability, max);

		switch (EsadConfig.TOOLTIP_DURABILITY_STYLE.get()) {
			case NUMBERS -> {
				Component maxText = Component.literal(Integer.toString(max))
					.withStyle(colorStyle == DurabilityColor.VARYING ? ChatFormatting.GRAY : color);
				Component numbers = durability == max ? maxText : Component.translatable("tooltip.esad.durability.numbers",
					Component.literal(Integer.toString(durability)).withStyle(color), maxText);
				lines.add(withLabel(numbers, label));
			}
			case BAR -> {
				if (label) {
					lines.add(Component.translatable("tooltip.esad.durability.label").withStyle(ChatFormatting.GRAY));
				}
				int full = max > 0 ? Math.round(10f * durability / max) : 10;
				Component bar = Component.literal("█".repeat(full) + "▒".repeat(10 - full)).withStyle(color);
				lines.add(Component.translatable("tooltip.esad.durability.bar", bar).withStyle(ChatFormatting.GRAY));
			}
			case TEXT -> {
				String key = durability == max ? "pristine"
					: durability >= 0.4f * max ? "damaged"
					: durability >= 0.1f * max ? "severely_damaged"
					: durability > 0 ? "nearly_broken"
					: "broken";
				lines.add(withLabel(Component.translatable("tooltip.esad.durability." + key).withStyle(color), label));
			}
		}
	}

	private static Component withLabel(Component value, boolean label) {
		return label ? Component.translatable("tooltip.esad.durability.with_label", value).withStyle(ChatFormatting.GRAY)
			: value.copy().withStyle(ChatFormatting.GRAY);
	}

	private static String modName(String namespace) {
		if (namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
			return "Minecraft";
		}
		return FabricLoader.getInstance().getModContainer(namespace)
			.map(ModContainer::getMetadata)
			.map(metadata -> metadata.getName())
			.orElse(namespace);
	}

	private static String format(double value) {
		return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
	}

	// ---------------------------------------------------------------- map preview

	/** Called for every tooltip image request: map preview, container content and ender chest content. */
	public static Optional<TooltipComponent> tooltipImage(ItemStack stack, Optional<TooltipComponent> original) {
		if (original.isPresent() || !EsadConfig.TOOLTIPS_ENABLED.get()) {
			return original;
		}
		MapId mapId = stack.get(DataComponents.MAP_ID);
		if (mapId != null && EsadConfig.TOOLTIP_MAP.get().isVisible()) {
			return Optional.of(new MapTooltip(mapId));
		}
		if (showsContainerPreview(stack)) {
			return Optional.of(containerTooltip(stack));
		}
		if (stack.is(Items.ENDER_CHEST) && EsadConfig.TOOLTIP_ENDER_CHEST.get().isVisible() && EnderChestCache.contents() != null) {
			return Optional.of(new ContainerTooltip(EnderChestCache.contents(), EsadConfig.TOOLTIP_CONTAINER_COLOR.get() ? 0xFF1F3A3A : 0));
		}
		return original;
	}

	private static boolean showsContainerPreview(ItemStack stack) {
		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		return contents != null && contents.nonEmptyItems().iterator().hasNext() && EsadConfig.TOOLTIP_CONTAINER.get().isVisible();
	}

	/** Whether the vanilla list of the container content ("Diamond x3, ...") is replaced by the preview. */
	public static boolean hidesContainerList(DataComponentGetter components) {
		ItemContainerContents contents = components.get(DataComponents.CONTAINER);
		return EsadConfig.TOOLTIPS_ENABLED.get() && EsadConfig.TOOLTIP_CONTAINER_HIDE_LIST.get() && contents != null
			&& contents.nonEmptyItems().iterator().hasNext() && EsadConfig.TOOLTIP_CONTAINER.get().isVisible();
	}

	private static ContainerTooltip containerTooltip(ItemStack stack) {
		ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
		contents.copyInto(items);
		int last = 0;
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isEmpty()) {
				last = i;
			}
		}
		int color = 0;
		boolean shulker = stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
		if (shulker && EsadConfig.TOOLTIP_CONTAINER_COLOR.get()) {
			DyeColor dye = ((ShulkerBoxBlock) ((BlockItem) stack.getItem()).getBlock()).getColor();
			color = dye != null ? dye.getTextureDiffuseColor() : 0xFF8E648E;
		}
		// Shulker boxes always show their 27 slots, other containers the rows that are used
		int slots = shulker ? 27 : Math.max(9, (last / 9 + 1) * 9);
		return new ContainerTooltip(new ArrayList<>(items.subList(0, slots)), color);
	}
}
