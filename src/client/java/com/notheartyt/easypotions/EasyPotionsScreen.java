package com.notheartyt.easypotions;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EasyPotionsScreen extends Screen {

    private String target = "";
    private String effectId = "";

    private String duration = "60";
    private String level = "1";

    private boolean infinite = false;

    private int page = 0;

    private TextFieldWidget durationField;
    private TextFieldWidget levelField;
    private TextFieldWidget search;

    private static final List<String> PRIORITY = List.of(
        "speed", "slowness", "strength",
        "instant_health", "instant_damage",
        "jump_boost", "regeneration", "resistance",
        "fire_resistance", "water_breathing",
        "night_vision", "invisibility", "haste",
        "mining_fatigue", "poison", "weakness",
        "slow_falling", "health_boost",
        "absorption", "luck"
    );

    public EasyPotionsScreen() {
        super(Text.literal("Easy Potions"));
    }

    private EasyPotionsScreen(String target, int page) {
        super(Text.literal("Easy Potions"));
        this.target = target;
        this.page = page;
    }

    @Override
    protected void init() {
        clearChildren();

        int center = width / 2;

        switch (page) {
            case 0 -> createPlayerPage(center);
            case 1 -> createManagementPage(center);
            case 2 -> createEffectPage(center);
            case 3 -> createApplyPage(center);
        }
    }

    // PLAYER SELECTION

    private void createPlayerPage(int center) {

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("EVERYONE"),
                button -> openManagement("@a")
            ).dimensions(center - 100, 65, 200, 25).build()
        );

        if (client == null || client.world == null) return;

        int y = 100;

        for (PlayerEntity player : client.world.getPlayers()) {

            String username = player.getGameProfile().getName();

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(username),
                    button -> openManagement(username)
                ).dimensions(center - 100, y, 200, 22).build()
            );

            y += 26;

            if (y > height - 40) break;
        }
    }

    private void openManagement(String selected) {
        if (client != null) {
            client.setScreen(new EasyPotionsScreen(selected, 1));
        }
    }

    // PLAYER MANAGEMENT

    private void createManagementPage(int center) {

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("BACK"),
                button -> client.setScreen(new EasyPotionsScreen())
            ).dimensions(10, 10, 80, 22).build()
        );

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("ADD EFFECT"),
                button -> client.setScreen(
                    new EasyPotionsScreen(target, 2)
                )
            ).dimensions(center - 100, 55, 200, 25).build()
        );

        // NEW FEATURE: CLEAR ALL EFFECTS

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("CLEAR ALL EFFECTS"),
                button -> clearAllEffects()
            ).dimensions(center - 100, 85, 200, 25)
                .build()
        );

        PlayerEntity selectedPlayer = null;

        if (!target.equals("@a") && client.world != null) {
            for (PlayerEntity player : client.world.getPlayers()) {
                if (player.getGameProfile().getName().equals(target)) {
                    selectedPlayer = player;
                    break;
                }
            }
        }

        if (selectedPlayer == null) return;

        int y = 125;

        for (StatusEffectInstance instance :
            selectedPlayer.getActiveStatusEffects()) {

            StatusEffect effect = instance.getEffectType().value();

            Identifier id = Registries.STATUS_EFFECT.getId(effect);

            if (id == null) continue;

            String name = effect.getName().getString();

            int amplifier = instance.getAmplifier() + 1;
            int seconds = instance.getDuration() / 20;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(
                        name + " | Level " + amplifier
                        + " | " + seconds + "s"
                    ),
                    button -> {
                        effectId = id.toString();
                        client.setScreen(
                            new EasyPotionsScreen(target, 3)
                        );
                    }
                ).dimensions(center - 140, y, 280, 22).build()
            );

            y += 26;

            if (y > height - 40) break;
        }
    }

    // CLEAR ALL EFFECTS

    private void clearAllEffects() {

        if (client == null || client.player == null) return;

        String command = "effect clear " + target;

        client.player.networkHandler.sendChatCommand(command);

        client.player.sendMessage(
            Text.literal(
                "Clear All Effects command sent for " + target
            ),
            false
        );
    }

    // EFFECT LIST

    private void createEffectPage(int center) {

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("BACK"),
                button -> client.setScreen(
                    new EasyPotionsScreen(target, 1)
                )
            ).dimensions(10, 10, 80, 22).build()
        );

        search = new TextFieldWidget(
            textRenderer,
            center - 120,
            45,
            240,
            22,
            Text.literal("Search effects")
        );

        search.setMaxLength(50);
        addDrawableChild(search);

        List<Identifier> effects = new ArrayList<>(
            Registries.STATUS_EFFECT.getIds()
        );

        effects.sort(
            Comparator.comparingInt(id -> {
                int index = PRIORITY.indexOf(id.getPath());
                return index < 0 ? 1000 : index;
            }).thenComparing(Identifier::getPath)
        );

        int y = 80;
        int shown = 0;

        for (Identifier id : effects) {

            String name = id.getPath().replace('_', ' ');

            String query = search.getText().toLowerCase();

            if (!query.isEmpty()
                && !name.contains(query)
                && !id.toString().contains(query)) {
                continue;
            }

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(name.toUpperCase()),
                    button -> {
                        effectId = id.toString();
                        client.setScreen(
                            new EasyPotionsScreen(target, 3)
                        );
                    }
                ).dimensions(center - 110, y, 220, 22).build()
            );

            y += 25;
            shown++;

            if (shown >= 8) break;
        }
    }

    // APPLY EFFECT

    private void createApplyPage(int center) {

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("BACK"),
                button -> client.setScreen(
                    new EasyPotionsScreen(target, 2)
                )
            ).dimensions(10, 10, 80, 22).build()
        );

        durationField = new TextFieldWidget(
            textRenderer, center - 80, 100,
            160, 22, Text.literal("Duration")
        );

        durationField.setText(duration);
        durationField.setMaxLength(9);
        addDrawableChild(durationField);

        levelField = new TextFieldWidget(
            textRenderer, center - 80, 145,
            160, 22, Text.literal("Level")
        );

        levelField.setText(level);
        levelField.setMaxLength(3);
        addDrawableChild(levelField);

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Infinite: " + infinite),
                button -> {
                    infinite = !infinite;
                    duration = durationField.getText();
                    level = levelField.getText();
                    clearAndInit();
                }
            ).dimensions(center - 80, 180, 160, 22).build()
        );

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("APPLY EFFECT"),
                button -> applyEffect()
            ).dimensions(center - 80, 220, 160, 28).build()
        );
    }

    private void applyEffect() {

        try {
            duration = durationField.getText();
            level = levelField.getText();

            int amplifier = Integer.parseInt(level);
            int seconds = Integer.parseInt(duration);

            if (amplifier < 1 || amplifier > 255) {
                throw new IllegalArgumentException();
            }

            if (!infinite && seconds < 1) {
                throw new IllegalArgumentException();
            }

            String time = infinite ? "infinite" : duration;

            String command =
                "effect give " + target
                + " " + effectId
                + " " + time
                + " " + (amplifier - 1)
                + " true";

            client.player.networkHandler.sendChatCommand(command);

            client.setScreen(
                new EasyPotionsScreen(target, 1)
            );

        } catch (Exception exception) {

            client.player.sendMessage(
                Text.literal("Invalid level or duration!"),
                false
            );
        }
    }

    @Override
    public void render(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta
    ) {

        renderBackground(context, mouseX, mouseY, delta);

        context.fill(
            width / 2 - 165,
            25,
            width / 2 + 165,
            height - 20,
            0xE820202D
        );

        context.drawCenteredTextWithShadow(
            textRenderer,
            "EASY POTIONS",
            width / 2,
            35,
            0x55FFFF
        );

        if (page == 1) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                "Managing: " + target,
                width / 2,
                115,
                0xFFFFFF
            );
        }

        if (page == 3) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                "Effect: " + effectId,
                width / 2,
                70,
                0xFFFFFF
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }
    }
