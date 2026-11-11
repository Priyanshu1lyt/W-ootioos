package com.notheartyt.easypotions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class EasyPotionsClient implements ClientModInitializer {

    public static KeyBinding OPEN_GUI;

    @Override
    public void onInitializeClient() {

        KeyBinding.Category category =
                KeyBinding.Category.create(
                        Identifier.of("easy_potions", "main")
                );

        OPEN_GUI = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.easy_potions.open",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        category
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (OPEN_GUI.wasPressed()) {

                if (client.player != null) {
                    client.setScreen(new EasyPotionsScreen());
                }

            }

        });
    }
}
