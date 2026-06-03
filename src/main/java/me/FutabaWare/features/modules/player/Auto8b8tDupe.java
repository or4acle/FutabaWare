package me.FutabaWare.features.modules.player;

import me.FutabaWare.features.command.Command;
import me.FutabaWare.features.modules.Module;
import me.FutabaWare.features.modules.movement.GUIMove;
import me.FutabaWare.features.setting.Setting;
import me.FutabaWare.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.settings.KeyBinding;

public class Auto8b8tDupe extends Module {

    // ─── Settings ─────────────────────────────────────────
    private final Setting<Integer> waitDelay     = register(new Setting<>("WaitDelay",     2500, 500,  8000));
    private final Setting<Integer> clickDelay    = register(new Setting<>("ClickDelay",    200,  50,   1000));
    private final Setting<Integer> driveDistance = register(new Setting<>("Distance",      135,  128,  200));
    private final Setting<Boolean> autoGUIMove   = register(new Setting<>("AutoGUIMove",   true));

    // ─── Estado ───────────────────────────────────────────
    public enum State {
        SEARCHING,
        WAITING_GUI_1,
        DEPOSITING,
        REOPENING,
        MOUNTING,
        DRIVING,
        WAITING_FAKE_GUI,
        EXTRACTING,
        DONE
    }

    private State state = State.SEARCHING;

    private AbstractChestHorse targetEntity;
    private EntityMinecartEmpty targetCart;
    private Vec3d               startPos;

    private final Timer actionTimer  = new Timer();
    private final Timer globalTimer  = new Timer();
    private static final long GLOBAL_TIMEOUT_MS = 120_000L;

    // ─── Construtor ───────────────────────────────────────
    public Auto8b8tDupe() {
        super("Auto8b8tDupe", "Automates the 8b8t donkey dupe.", Category.MISC, true, false, false);
    }

    // ─── Enable / Disable ─────────────────────────────────
    @Override
    public void onEnable() {
        state        = State.SEARCHING;
        targetEntity = null;
        targetCart   = null;
        startPos     = null;
        actionTimer.reset();
        globalTimer.reset();

        if (autoGUIMove.getValue()) enableGUIMove();
    }

    @Override
    public void onDisable() {
        if (autoGUIMove.getValue()) disableGUIMove();
        releaseMovement();
    }

    // ─── Loop principal ───────────────────────────────────
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        // Timeout global
        if (globalTimer.passedMs(GLOBAL_TIMEOUT_MS)) {
            Command.sendMessage("[8b8tDupe] Timeout — aborting.");
            this.disable();
            return;
        }

        switch (state) {

            // ── 1. Procura entidade com baú e carrinho ──
            case SEARCHING: {
                targetEntity = null;
                targetCart   = null;

                for (Entity e : mc.world.loadedEntityList) {
                    if (e instanceof AbstractChestHorse
                            && ((AbstractChestHorse) e).hasChest()
                            && mc.player.getDistance(e) < 5f) {
                        targetEntity = (AbstractChestHorse) e;
                    }
                    if (e instanceof EntityMinecartEmpty
                            && mc.player.getDistance(e) < 5f) {
                        targetCart = (EntityMinecartEmpty) e;
                    }
                }

                if (targetEntity != null && targetCart != null && hasShulkers()) {
                    Command.sendMessage("[8b8tDupe] Found entities. Opening inventory...");
                    mc.player.connection.sendPacket(
                            new CPacketUseEntity(targetEntity, EnumHand.MAIN_HAND));
                    actionTimer.reset();
                    state = State.WAITING_GUI_1;
                } else if (targetEntity == null) {
                    Command.sendMessage("[8b8tDupe] No chest entity nearby. Stopping.");
                    this.disable();
                } else if (!hasShulkers()) {
                    Command.sendMessage("[8b8tDupe] No shulkers in inventory. Stopping.");
                    this.disable();
                }
                break;
            }

            // ── 2. Aguarda a GUI abrir ──
            case WAITING_GUI_1: {
                if (!actionTimer.passedMs(waitDelay.getValue())) break;

                if (mc.player.openContainer instanceof ContainerChest) {
                    Command.sendMessage("[8b8tDupe] GUI open. Depositing shulkers...");
                    state = State.DEPOSITING;
                    actionTimer.reset();
                } else if (actionTimer.passedMs(waitDelay.getValue() + 3000)) {
                    Command.sendMessage("[8b8tDupe] GUI didn't open. Aborting.");
                    this.disable();
                }
                break;
            }

            // ── 3. Deposita shulkers ──
            case DEPOSITING: {
                if (!actionTimer.passedMs(clickDelay.getValue())) break;

                boolean movedAny = false;
                for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (stack.getItem() instanceof ItemShulkerBox) {
                        mc.playerController.windowClick(
                                mc.player.openContainer.windowId,
                                getInventorySlot(i),
                                0,
                                ClickType.QUICK_MOVE,
                                mc.player);
                        Command.sendMessage("[8b8tDupe] Depositing... "
                                + countShulkers() + " shulker(s) remaining.");
                        movedAny = true;
                        actionTimer.reset();
                        break;
                    }
                }

                if (!movedAny) {
                    mc.player.closeScreen();
                    Command.sendMessage("[8b8tDupe] All deposited. Re-opening...");
                    actionTimer.reset();
                    state = State.REOPENING;
                }
                break;
            }

            // ── 4. Fecha e reabre ──
            case REOPENING: {
                if (!actionTimer.passedMs(500)) break;

                mc.player.connection.sendPacket(
                        new CPacketUseEntity(targetEntity, EnumHand.MAIN_HAND));
                actionTimer.reset();
                state = State.MOUNTING;
                break;
            }

            // ── 5. Monta no carrinho ──
            case MOUNTING: {
                if (!actionTimer.passedMs(500)) break;  // aguarda confirmação

                mc.player.connection.sendPacket(
                        new CPacketUseEntity(targetCart, EnumHand.MAIN_HAND));
                startPos = mc.player.getPositionVector();  // posição REAL de partida
                Command.sendMessage("[8b8tDupe] Mounted! Travel "
                        + driveDistance.getValue() + " blocks in the minecart.");
                state = State.DRIVING;
                break;
            }

            // ── 6. Viaja até a distância segura ──
            case DRIVING: {
                // Verifica se ainda está montado
                if (mc.player.getRidingEntity() == null) {
                    Command.sendMessage("[8b8tDupe] Dismounted unexpectedly. Aborting.");
                    this.disable();
                    break;
                }

                double dist = mc.player.getPositionVector().distanceTo(startPos);
                if (dist >= driveDistance.getValue()) {
                    Command.sendMessage("[8b8tDupe] Distance reached! Waiting for fake GUI...");
                    actionTimer.reset();
                    state = State.WAITING_FAKE_GUI;
                }
                break;
            }

            // ── 7. Aguarda a GUI fake aparecer ──
            case WAITING_FAKE_GUI: {
                if (mc.player.openContainer instanceof ContainerChest) {
                    Command.sendMessage("[8b8tDupe] Fake GUI detected. Extracting items...");
                    state = State.EXTRACTING;
                    actionTimer.reset();
                } else if (actionTimer.passedMs(8000)) {
                    Command.sendMessage("[8b8tDupe] Fake GUI didn't appear. Aborting.");
                    this.disable();
                }
                break;
            }

            // ── 8. Extrai itens da GUI fake ──
            case EXTRACTING: {
                if (!(mc.player.openContainer instanceof ContainerChest)) {
                    Command.sendMessage("[8b8tDupe] Lost GUI during extraction. Aborting.");
                    this.disable();
                    break;
                }
                if (!actionTimer.passedMs(clickDelay.getValue())) break;

                ContainerChest chest = (ContainerChest) mc.player.openContainer;
                boolean extractedAny = false;

                for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
                    if (!chest.getLowerChestInventory().getStackInSlot(i).isEmpty()) {
                        mc.playerController.windowClick(
                                chest.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                        extractedAny = true;
                        actionTimer.reset();
                        break;
                    }
                }

                if (!extractedAny) {
                    mc.player.closeScreen();
                    mc.player.dismountRidingEntity();
                    Command.sendMessage("[8b8tDupe] Dupe complete!");
                    state = State.DONE;
                }
                break;
            }

            // ── 9. Finaliza ──
            case DONE: {
                this.disable();
                break;
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────
    private boolean hasShulkers() {
        return countShulkers() > 0;
    }

    private int countShulkers() {
        int count = 0;
        for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemShulkerBox) count++;
        }
        return count;
    }

    private int getInventorySlot(int playerSlot) {
        if (!(mc.player.openContainer instanceof ContainerChest)) return playerSlot;
        int chestSize = ((ContainerChest) mc.player.openContainer)
                .getLowerChestInventory().getSizeInventory();
        return playerSlot < 9
                ? chestSize + 27 + playerSlot   // hotbar
                : chestSize + (playerSlot - 9);  // inventário principal
    }

    private void enableGUIMove() {
        GUIMove gm = GUIMove.getInstance();
        if (gm != null && !gm.isOn()) gm.enable();
    }

    private void disableGUIMove() {
        GUIMove gm = GUIMove.getInstance();
        if (gm != null && gm.isOn()) gm.disable();
    }

    private void releaseMovement() {
        if (mc.gameSettings == null) return;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(),    false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(),    false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(),   false);
    }
}