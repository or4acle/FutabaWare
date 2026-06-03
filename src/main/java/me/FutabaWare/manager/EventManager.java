package me.FutabaWare.manager;

import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.FutabaWare.FutabaWare;
import me.FutabaWare.event.events.*;
import me.FutabaWare.features.modules.client.KillFeed;
import me.FutabaWare.features.modules.combat.AutoCrystal;
import me.FutabaWare.features.modules.render.DeathEffects;
import me.FutabaWare.features.modules.troll.ChinaText;
import me.FutabaWare.util.Timer;
import me.FutabaWare.features.Feature;
import me.FutabaWare.features.command.Command;
import me.FutabaWare.features.modules.client.HUD;
import me.FutabaWare.features.modules.misc.PopCounter;
import me.FutabaWare.util.TimerUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraft.client.gui.GuiMainMenu;

import me.FutabaWare.features.gui.GuiMainMenuFutaba;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager extends Feature {
    private final Timer logoutTimer = new Timer();
    private final TimerUtil chorusTimer;

    public EventManager() {
        this.chorusTimer = new TimerUtil();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onUnload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    private final AtomicBoolean tickOngoing = new AtomicBoolean(false);

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!fullNullCheck() && (event.getEntity().getEntityWorld()).isRemote && event.getEntityLiving().equals(mc.player)) {
            FutabaWare.inventoryManager.update();
            FutabaWare.moduleManager.onUpdate();
            if ((HUD.getInstance()).renderingMode.getValue() == HUD.RenderingMode.Length) {
                FutabaWare.moduleManager.sortModules(true);
            } else {
                FutabaWare.moduleManager.sortModulesABC();
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {

        if(event.getGui() instanceof GuiMainMenu) {

            event.setGui(
                    new GuiMainMenuFutaba()
            );

        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.logoutTimer.reset();
        FutabaWare.moduleManager.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        FutabaWare.moduleManager.onLogout();
    }

    public boolean ticksOngoing() {
        return this.tickOngoing.get();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (fullNullCheck())
            return;
        FutabaWare.moduleManager.onTick();
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null){
                if (player.getHealth() > 0.0F) {
                    continue;
                }
                MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
                if (DeathEffects.getInstance().isEnabled()){
                    DeathEffects.getInstance().onDeath(player);
                }
                if (!PopCounter.getInstance().isEnabled()){
                    continue;
                }
                PopCounter.getInstance().onDeath(player);
            }

        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck())
            return;
        if (event.getStage() == 0) {
            FutabaWare.speedManager.updateValues();
            FutabaWare.rotationManager.updateRotations();
            FutabaWare.positionManager.updatePosition();
        }
        if (event.getStage() == 1) {
            FutabaWare.rotationManager.restoreRotations();
            FutabaWare.positionManager.restorePosition();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() != 0)
            return;
        FutabaWare.serverManager.onPacketReceived();
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) packet.getEntity(mc.world);
                MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
                if (PopCounter.getInstance().isEnabled()) {
                    PopCounter.getInstance().onTotemPop(player);
                }
                if (ChinaText.getInstance().isEnabled()) {
                    ChinaText.getInstance().onTotemPop(player);
                }
            }
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && this.logoutTimer.passedS(1.0D)) {
            SPacketPlayerListItem packet = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet.getAction()))
                return;
            packet.getEntries().stream().filter(Objects::nonNull).filter(data -> (!Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null))
                    .forEach(data -> {
                        String name;
                        EntityPlayer entity;
                        UUID id = data.getProfile().getId();
                        switch (packet.getAction()) {
                            case ADD_PLAYER:
                                name = data.getProfile().getName();
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                                break;
                            case REMOVE_PLAYER:
                                entity = mc.world.getPlayerEntityByUUID(id);
                                if (entity != null) {
                                    String logoutName = entity.getName();
                                    MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                                    break;
                                }
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                                break;
                        }
                    });
        }
        if (event.getPacket() instanceof net.minecraft.network.play.server.SPacketTimeUpdate) {
            FutabaWare.serverManager.update();
        }

        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect)event.getPacket()).getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) {
            if (!this.chorusTimer.passedMs(100L)) {
                MinecraftForge.EVENT_BUS.post(((Event)new ChorusEvent(((SPacketSoundEffect)event.getPacket()).getX(), ((SPacketSoundEffect)event.getPacket()).getY(), ((SPacketSoundEffect)event.getPacket()).getZ())));
            }
            this.chorusTimer.reset();
        }

    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled())
            return;
        mc.profiler.startSection("FutabaWare");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);
        Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        FutabaWare.moduleManager.onRender3D(render3dEvent);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        mc.profiler.endSection();
    }

    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
            FutabaWare.textManager.updateResolution();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            ScaledResolution resolution = new ScaledResolution(mc);
            Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            FutabaWare.moduleManager.onRender2D(render2DEvent);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState())
            FutabaWare.moduleManager.onKeyPressed(Keyboard.getEventKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
                if (event.getMessage().length() > 1) {
                    FutabaWare.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(ChatFormatting.RED + "An error occurred while running this command. Check the log!");
            }
        }
    }
}
