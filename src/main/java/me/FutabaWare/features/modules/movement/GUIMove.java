package me.FutabaWare.features.modules.movement;

import me.FutabaWare.features.modules.Module;
import me.FutabaWare.features.setting.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class GUIMove extends Module {

    private static GUIMove INSTANCE;

    public Setting<Boolean> jump =
            this.register(new Setting<>("Jump", true));

    public Setting<Boolean> sneak =
            this.register(new Setting<>("Sneak", false));

    public Setting<Boolean> sprint =
            this.register(new Setting<>("Sprint", true));

    public GUIMove() {
        super(
                "GUIMove",
                "Move while GUIs are open",
                Category.MOVEMENT,
                false,
                false,
                false
        );

        INSTANCE = this;
    }

    public static GUIMove getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GUIMove();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (mc.currentScreen == null) {
            return;
        }

        if (mc.currentScreen instanceof GuiChat) {
            return;
        }

        KeyBinding[] keys = {
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight
        };

        for (KeyBinding bind : keys) {
            KeyBinding.setKeyBindState(
                    bind.getKeyCode(),
                    Keyboard.isKeyDown(bind.getKeyCode())
            );
        }

        if (jump.getValue()) {
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindJump.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())
            );
        }

        if (sneak.getValue()) {
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindSneak.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
            );
        }

        if (sprint.getValue()) {
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindSprint.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())
            );
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }

        KeyBinding.unPressAllKeys();
    }
}