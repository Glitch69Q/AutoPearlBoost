package com.example.autopearlboost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Auto Pearl Boost"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 4;

        addRenderableWidget(Button.builder(toggle("Auto Pearl Boost", Config.enabled), b -> {
            Config.enabled = !Config.enabled;
            b.setMessage(toggle("Auto Pearl Boost", Config.enabled));
            Config.save();
        }).bounds(cx - 100, y, 200, 20).build());

        addRenderableWidget(Button.builder(toggle("Automatic Wind Charge", Config.autoUse), b -> {
            Config.autoUse = !Config.autoUse;
            b.setMessage(toggle("Automatic Wind Charge", Config.autoUse));
            Config.save();
        }).bounds(cx - 100, y + 28, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Prediction strength: " + fmt(Config.predictionStrength)), b -> {
            Config.predictionStrength += 0.25;
            if (Config.predictionStrength > 2.0) Config.predictionStrength = 0.25;
            b.setMessage(Component.literal("Prediction strength: " + fmt(Config.predictionStrength)));
            Config.save();
        }).bounds(cx - 100, y + 56, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Max distance: " + fmt(Config.maxDistance)), b -> {
            Config.maxDistance += 4.0;
            if (Config.maxDistance > 64.0) Config.maxDistance = 8.0;
            b.setMessage(Component.literal("Max distance: " + fmt(Config.maxDistance)));
            Config.save();
        }).bounds(cx - 100, y + 84, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            Config.save();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(cx - 100, y + 122, 200, 20).build());
    }

    private static Component toggle(String name, boolean enabled) {
        return Component.literal(name + ": " + (enabled ? "ON" : "OFF"));
    }

    private static String fmt(double d) {
        return String.format(java.util.Locale.ROOT, "%.2f", d);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, 0xCC101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 25, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
