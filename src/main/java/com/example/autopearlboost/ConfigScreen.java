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
        int centerX = this.width / 2;
        int y = 60;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Enabled: " + (Config.enabled ? "ON" : "OFF")
                        ),
                        button -> {
                            Config.enabled = !Config.enabled;
                            button.setMessage(
                                    Component.literal(
                                            "Enabled: " + (Config.enabled ? "ON" : "OFF")
                                    )
                            );
                            Config.save();
                        }
                ).bounds(centerX - 100, y, 200, 20).build()
        );

        y += 30;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal(
                                "Auto Use: " + (Config.autoUse ? "ON" : "OFF")
                        ),
                        button -> {
                            Config.autoUse = !Config.autoUse;
                            button.setMessage(
                                    Component.literal(
                                            "Auto Use: " + (Config.autoUse ? "ON" : "OFF")
                                    )
                            );
                            Config.save();
                        }
                ).bounds(centerX - 100, y, 200, 20).build()
        );

        y += 30;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Close"),
                        button -> Minecraft.getInstance().gui.setScreen(parent)
                ).bounds(centerX - 100, y, 200, 20).build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(
                this.font,
                this.title,
                this.width / 2,
                25,
                0xFFFFFFFF,
                true
        );
    }
}
