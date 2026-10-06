package cc.avie.esad.gui;

import cc.avie.esad.compat.ScreenCompat;
import cc.avie.esad.config.EsadConfig;
import com.mojang.blaze3d.platform.InputConstants;
import me.avie29.tabbylib.TabbyLibConfig;
import me.avie29.tabbylib.api.option.Option;
import me.avie29.tabbylib.client.gui.OptionHost;
import me.avie29.tabbylib.client.gui.OptionListWidget;
import me.avie29.tabbylib.client.gui.Panel;
import me.avie29.tabbylib.client.gui.entry.OptionControls;
import me.avie29.tabbylib.client.gui.widget.TabbyButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The settings window of EssentialAdditions: a row of feature cards at the top, each with a switch that turns
 * the feature on or off. Clicking a card shows its settings below, drawn with the TabbyLib option rows.
 * Changes are pending until Save or Done, like in the TabbyLib screen.
 */
public class EsadConfigScreen extends Screen implements OptionHost {
	private static final int TOP = 24;
	private static final int FOOTER = 30;
	private static final int MAX_WIDTH = 640;
	private static final int PADDING = 6;
	private static final int CARD_HEIGHT = 50;
	private static final int CARD_MIN_WIDTH = 78;
	private static final int CARD_GAP = 4;
	/** More rows of cards scroll, so the settings below keep enough room. */
	private static final int MAX_CARD_ROWS = 2;
	private static final int SCROLLBAR_WIDTH = 4;
	private static final int SWITCH_WIDTH = 20;
	private static final int SWITCH_HEIGHT = 10;

	private final @Nullable Screen parent;
	private final List<Feature> features;
	private int selected;

	private @Nullable OptionListWidget optionList;
	private OptionControls.@Nullable KeyBindControl capturingKey;
	private String visibilitySignature = "";
	private double restoreScroll;
	private @Nullable Component statusMessage;
	private long statusUntil;

	// Layout, computed in init()
	private int windowX;
	private int windowWidth;
	private int windowBottom;
	private int cardWidth;
	private int columns;
	private int cardAreaTop;
	private int cardAreaHeight;
	private int cardScroll;
	private int maxCardScroll;
	private int headerY;
	private int listTop;

	public EsadConfigScreen(@Nullable Screen parent) {
		this(parent, null);
	}

	/** Opens the window with the card of the given category selected, e.g. "dayCounter". */
	public EsadConfigScreen(@Nullable Screen parent, @Nullable String category) {
		super(Component.translatable("esad.screen.title"));
		this.parent = parent;
		this.features = EsadConfig.features();
		for (int i = 0; i < this.features.size(); i++) {
			if (this.features.get(i).category().getKey().equals(category)) {
				this.selected = i;
			}
		}
	}

	/**
	 * Opens the window, e.g. from a command. Commands run while the chat is still open, so the screen is
	 * set on the next tick.
	 */
	public static void open(@Nullable String category) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.execute(() -> ScreenCompat.setScreen(new EsadConfigScreen(ScreenCompat.currentScreen(), category)));
	}

	// ---------------------------------------------------------------- layout

	@Override
	protected void init() {
		this.windowWidth = Math.min(this.width - 16, MAX_WIDTH);
		this.windowX = (this.width - this.windowWidth) / 2;
		this.windowBottom = this.height - FOOTER;
		int inner = this.windowWidth - PADDING * 2 - SCROLLBAR_WIDTH - 2;
		int count = Math.max(1, this.features.size());
		this.columns = Math.max(1, Math.min(count, (inner + CARD_GAP) / (CARD_MIN_WIDTH + CARD_GAP)));
		this.cardWidth = (inner - CARD_GAP * (this.columns - 1)) / this.columns;
		int rows = (count + this.columns - 1) / this.columns;
		int visibleRows = Math.min(rows, MAX_CARD_ROWS);
		this.cardAreaTop = TOP + PADDING;
		this.cardAreaHeight = visibleRows * (CARD_HEIGHT + CARD_GAP) - CARD_GAP;
		this.maxCardScroll = Math.max(0, rows * (CARD_HEIGHT + CARD_GAP) - CARD_GAP - this.cardAreaHeight);
		this.scrollToCard(this.selected);
		this.headerY = this.cardAreaTop + this.cardAreaHeight + 8;
		this.listTop = this.headerY + 26;

		this.optionList = new OptionListWidget(this.minecraft, this, this.windowX + 2, this.listTop,
			this.windowWidth - 4, Math.max(10, this.windowBottom - this.listTop - 3));
		this.addRenderableWidget(this.optionList);

		int buttonWidth = Math.min(100, (this.width - 16 - 12) / 4);
		int x = (this.width - (buttonWidth * 4 + 12)) / 2;
		int y = this.height - FOOTER + 5;
		TabbyButton reset = new TabbyButton(buttonWidth, 20, Component.translatable("esad.screen.reset"), this::resetSelected);
		reset.setTooltip(Tooltip.create(Component.translatable("esad.screen.reset.tooltip")));
		TabbyButton discard = new TabbyButton(buttonWidth, 20, Component.translatable("tabbylib.button.discard"), this::discard);
		TabbyButton save = new TabbyButton(buttonWidth, 20, Component.translatable("tabbylib.button.save"), this::save);
		TabbyButton done = new TabbyButton(buttonWidth, 20, Component.translatable("tabbylib.button.done"), () -> {
			this.save();
			this.closeScreen();
		});
		for (TabbyButton button : List.of(reset, discard, save, done)) {
			button.setPosition(x, y);
			x += buttonWidth + 4;
			this.addRenderableWidget(button);
		}

		this.rebuildOptions();
		this.optionList.setScrollAmount(this.restoreScroll);
		this.restoreScroll = 0;
	}

	private int cardX(int index) {
		return this.windowX + PADDING + index % this.columns * (this.cardWidth + CARD_GAP);
	}

	private int cardY(int index) {
		return this.cardAreaTop + index / this.columns * (CARD_HEIGHT + CARD_GAP) - this.cardScroll;
	}

	private int switchX(int index) {
		return this.cardX(index) + (this.cardWidth - SWITCH_WIDTH) / 2;
	}

	private int switchY(int index) {
		return this.cardY(index) + CARD_HEIGHT - SWITCH_HEIGHT - 3;
	}

	/** Scrolls the card area so the card is fully visible. */
	private void scrollToCard(int index) {
		int top = index / this.columns * (CARD_HEIGHT + CARD_GAP);
		if (top < this.cardScroll) {
			this.cardScroll = top;
		} else if (top + CARD_HEIGHT > this.cardScroll + this.cardAreaHeight) {
			this.cardScroll = top + CARD_HEIGHT - this.cardAreaHeight;
		}
		this.cardScroll = Math.max(0, Math.min(this.maxCardScroll, this.cardScroll));
	}

	private boolean isInCardArea(double mouseX, double mouseY) {
		return mouseX >= this.windowX + PADDING && mouseX < this.windowX + this.windowWidth - PADDING
			&& mouseY >= this.cardAreaTop && mouseY < this.cardAreaTop + this.cardAreaHeight;
	}

	private @Nullable Feature selectedFeature() {
		return this.features.isEmpty() ? null : this.features.get(this.selected);
	}

	// ---------------------------------------------------------------- options

	@Override
	public Screen asScreen() {
		return this;
	}

	@Override
	public void rebuildOptions() {
		if (this.optionList == null) {
			return;
		}
		double scroll = this.optionList.scrollAmount();
		this.optionList.clear();
		this.visibilitySignature = this.computeVisibilitySignature();
		Feature feature = this.selectedFeature();
		if (feature != null) {
			this.optionList.addEntries(feature.category().getEntries(), false);
		}
		this.optionList.setScrollAmount(scroll);
	}

	private void select(int index) {
		if (this.selected != index) {
			this.selected = index;
			this.scrollToCard(index);
			this.rebuildOptions();
			if (this.optionList != null) {
				this.optionList.setScrollAmount(0);
			}
		}
	}

	/** Which options are visible right now. When it changes (visibleWhen) the list is rebuilt. */
	private String computeVisibilitySignature() {
		StringBuilder builder = new StringBuilder();
		for (Option<?> option : EsadConfig.CONFIG.getOptions()) {
			builder.append(option.isVisible() ? '1' : '0');
		}
		return builder.toString();
	}

	@Override
	public void tick() {
		if (!this.computeVisibilitySignature().equals(this.visibilitySignature)) {
			this.rebuildOptions();
		}
	}

	@Override
	public void openSubScreen(Screen screen) {
		this.restoreScroll = this.optionList != null ? this.optionList.scrollAmount() : 0;
		ScreenCompat.setScreen(screen);
	}

	// ---------------------------------------------------------------- saving

	private void resetSelected() {
		Feature feature = this.selectedFeature();
		if (feature != null) {
			feature.toggle().resetPending();
			feature.category().getOptions().forEach(Option::resetPending);
			this.refreshAfterChange();
		}
	}

	private void discard() {
		EsadConfig.CONFIG.discard();
		this.refreshAfterChange();
		this.showStatus(Component.translatable("tabbylib.status.discarded"));
	}

	private void save() {
		if (!EsadConfig.CONFIG.hasChanges()) {
			return;
		}
		boolean restart = EsadConfig.CONFIG.commit();
		this.refreshAfterChange();
		this.showStatus(restart
			? Component.translatable("tabbylib.status.restart").withStyle(ChatFormatting.RED)
			: Component.translatable("tabbylib.status.saved").withStyle(ChatFormatting.GREEN));
	}

	private void refreshAfterChange() {
		this.restoreScroll = this.optionList != null ? this.optionList.scrollAmount() : 0;
		this.rebuildWidgets();
	}

	private void showStatus(Component message) {
		this.statusMessage = message;
		this.statusUntil = Util.getMillis() + 3000;
	}

	@Override
	public void onClose() {
		if (EsadConfig.CONFIG.hasChanges() && TabbyLibConfig.CONFIRM_UNSAVED.get()) {
			ScreenCompat.setScreen(new ConfirmScreen(save -> {
				if (save) {
					this.save();
				} else {
					EsadConfig.CONFIG.discard();
				}
				this.closeScreen();
			}, Component.translatable("tabbylib.unsaved.title"), Component.translatable("tabbylib.unsaved.message"),
				Component.translatable("tabbylib.button.save"), Component.translatable("tabbylib.button.discard")));
			return;
		}
		EsadConfig.CONFIG.discard();
		this.closeScreen();
	}

	private void closeScreen() {
		ScreenCompat.setScreen(this.parent);
	}

	// ---------------------------------------------------------------- input

	@Override
	public void startKeyCapture(OptionControls.KeyBindControl control) {
		OptionControls.KeyBindControl previous = this.capturingKey;
		this.capturingKey = control;
		if (previous != null && previous != control) {
			previous.refresh();
		}
	}

	@Override
	public boolean isCapturing(OptionControls.KeyBindControl control) {
		return this.capturingKey == control;
	}

	private void finishKeyCapture(InputConstants.Key key) {
		OptionControls.KeyBindControl control = this.capturingKey;
		this.capturingKey = null;
		if (control != null) {
			control.setKey(key);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.capturingKey != null) {
			this.finishKeyCapture(event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event));
			return true;
		}
		// Ctrl + S saves, like in the TabbyLib screen
		if (event.input() == InputConstants.KEY_S && event.hasControlDown()) {
			this.save();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (this.capturingKey != null) {
			this.finishKeyCapture(InputConstants.Type.MOUSE.getOrCreate(event.button()));
			return true;
		}
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			for (int i = 0; i < this.features.size(); i++) {
				if (this.isOverSwitch(i, event.x(), event.y())) {
					Feature feature = this.features.get(i);
					feature.toggle().setPending(!feature.toggle().getPending());
					AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
					return true;
				}
				if (this.isOverCard(i, event.x(), event.y())) {
					this.select(i);
					AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean isOverCard(int index, double mouseX, double mouseY) {
		int x = this.cardX(index);
		int y = this.cardY(index);
		return this.isInCardArea(mouseX, mouseY)
			&& mouseX >= x && mouseX < x + this.cardWidth && mouseY >= y && mouseY < y + CARD_HEIGHT;
	}

	/** The switch reacts a bit outside of its frame, it is small. */
	private boolean isOverSwitch(int index, double mouseX, double mouseY) {
		int x = this.switchX(index);
		int y = this.switchY(index);
		return this.isInCardArea(mouseX, mouseY) && mouseX >= x - 3 && mouseX < x + SWITCH_WIDTH + 3 && mouseY >= y - 3 && mouseY < y + SWITCH_HEIGHT + 3;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.maxCardScroll > 0 && this.isInCardArea(mouseX, mouseY)) {
			this.cardScroll = (int) Math.max(0, Math.min(this.maxCardScroll, this.cardScroll - scrollY * 20));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	// ---------------------------------------------------------------- rendering

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Panel.draw(graphics, this.windowX, TOP, this.windowWidth, this.windowBottom - TOP);
		graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

		graphics.enableScissor(this.windowX + PADDING, this.cardAreaTop, this.windowX + this.windowWidth - PADDING,
			this.cardAreaTop + this.cardAreaHeight);
		for (int i = 0; i < this.features.size(); i++) {
			int y = this.cardY(i);
			if (y + CARD_HEIGHT > this.cardAreaTop && y < this.cardAreaTop + this.cardAreaHeight) {
				this.drawCard(graphics, i, mouseX, mouseY);
			}
		}
		graphics.disableScissor();
		if (this.maxCardScroll > 0) {
			int barX = this.windowX + this.windowWidth - PADDING - SCROLLBAR_WIDTH;
			int contentHeight = this.cardAreaHeight + this.maxCardScroll;
			int thumbHeight = Math.max(10, this.cardAreaHeight * this.cardAreaHeight / contentHeight);
			int thumbY = this.cardAreaTop + (this.cardAreaHeight - thumbHeight) * this.cardScroll / this.maxCardScroll;
			graphics.fill(barX, this.cardAreaTop, barX + SCROLLBAR_WIDTH, this.cardAreaTop + this.cardAreaHeight, 0x40000000);
			graphics.fill(barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFA0A0A0);
		}

		Feature feature = this.selectedFeature();
		if (feature != null) {
			int x = this.windowX + PADDING + 1;
			int width = this.windowWidth - PADDING * 2 - 2;
			graphics.text(this.font, OptionListWidget.clip(this.font, feature.name().copy().withStyle(ChatFormatting.BOLD), width),
				x, this.headerY, TabbyLibConfig.accentColor(), true);
			graphics.text(this.font, OptionListWidget.clip(this.font, feature.description(), width),
				x, this.headerY + 12, 0xFFA0A0A0, false);
			graphics.fill(this.windowX + 4, this.listTop - 2, this.windowX + this.windowWidth - 4, this.listTop - 1, 0x40FFFFFF);
		}

		super.extractRenderState(graphics, mouseX, mouseY, a);

		if (this.statusMessage != null) {
			if (Util.getMillis() < this.statusUntil) {
				int textWidth = this.font.width(this.statusMessage);
				graphics.text(this.font, this.statusMessage, this.windowX + this.windowWidth - textWidth, 8, 0xFFFFFFFF, true);
			} else {
				this.statusMessage = null;
			}
		}
	}

	private void drawCard(GuiGraphicsExtractor graphics, int index, int mouseX, int mouseY) {
		Feature feature = this.features.get(index);
		Font font = this.font;
		int x = this.cardX(index);
		int y = this.cardY(index);
		int w = this.cardWidth;
		int h = CARD_HEIGHT;
		int accent = TabbyLibConfig.accentColor();
		boolean selected = index == this.selected;
		boolean enabled = feature.toggle().getPending();
		boolean hovered = this.isOverCard(index, mouseX, mouseY);

		Panel.draw(graphics, x, y, w, h);
		if (selected) {
			graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, (accent & 0x00FFFFFF) | 0x30000000);
			graphics.fill(x, y, x + w, y + 1, accent);
			graphics.fill(x, y + h - 1, x + w, y + h, accent);
			graphics.fill(x, y + 1, x + 1, y + h - 1, accent);
			graphics.fill(x + w - 1, y + 1, x + w, y + h - 1, accent);
		} else if (hovered) {
			graphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0x20FFFFFF);
		}

		// Item icon at 1.5 times the size
		int iconX = x + w / 2 - 12;
		int iconY = y + 3;
		feature.icon().draw(graphics, iconX, iconY, 24);
		if (!enabled) {
			graphics.fill(iconX, iconY, iconX + 24, iconY + 24, 0x90202020);
		}

		Component name = feature.name();
		int nameColor = enabled ? 0xFFFFFFFF : 0xFF909090;
		graphics.centeredText(font, OptionListWidget.clip(font, name, w - 6), x + w / 2, y + 28, nameColor);

		this.drawSwitch(graphics, this.switchX(index), this.switchY(index), enabled, accent, this.isOverSwitch(index, mouseX, mouseY));

		if (hovered && !this.isOverSwitch(index, mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(font, font.split(feature.description(), 200), mouseX, mouseY);
		} else if (this.isOverSwitch(index, mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(font, font.split(Component.translatable(enabled ? "esad.screen.switch.on" : "esad.screen.switch.off"), 200),
				mouseX, mouseY);
		}
	}

	/** Pixel style switch: accent colored track with the knob on the right when on. */
	private void drawSwitch(GuiGraphicsExtractor graphics, int x, int y, boolean on, int accent, boolean hovered) {
		int track = on ? accent : 0xFF3C3C3C;
		graphics.fill(x, y, x + SWITCH_WIDTH, y + SWITCH_HEIGHT, 0xFF000000);
		graphics.fill(x + 1, y + 1, x + SWITCH_WIDTH - 1, y + SWITCH_HEIGHT - 1, track);
		int knobSize = SWITCH_HEIGHT - 2;
		int knobX = on ? x + SWITCH_WIDTH - 1 - knobSize : x + 1;
		int knob = hovered ? 0xFFFFFFFF : on ? 0xFFF0F0F0 : 0xFFB0B0B0;
		graphics.fill(knobX, y + 1, knobX + knobSize, y + 1 + knobSize, knob);
		// Small shadow at the bottom of the knob for a bit of depth
		graphics.fill(knobX, y + knobSize, knobX + knobSize, y + 1 + knobSize, 0x40000000);
	}
}
