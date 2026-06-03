package me.FutabaWare.features.modules.misc;

import me.FutabaWare.FutabaWare;
import me.FutabaWare.event.events.PacketEvent;
import me.FutabaWare.features.modules.Module;
import me.FutabaWare.features.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatModifier
        extends Module {
    private static ChatModifier INSTANCE = new ChatModifier();
    public Setting<Boolean> clean = this.register(new Setting<Boolean>("NoChatBackground", Boolean.valueOf(false), "Cleans your chat"));
    public Setting<Boolean> infinite = this.register(new Setting<Boolean>("InfiniteChat", Boolean.valueOf(false), "Makes your chat infinite."));
    public boolean check;

    public ChatModifier() {
        super("BetterChat", "Modifies your chat", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    public static ChatModifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatModifier();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
            this.check = !s.startsWith(FutabaWare.commandManager.getPrefix());
        }
    }
}

