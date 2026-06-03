package me.FutabaWare.features.modules.troll;

import me.FutabaWare.event.events.PacketEvent;
import me.FutabaWare.features.modules.Module;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoChinaText extends Module {
    public NoChinaText() { super("NoChinaText", "No more china text lag shit", Category.TROLL, true, false, false);}

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChat) {
            String text = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText();
            if (text.contains("\u0B01") || text.contains("\u0201") || text.contains("\u2701")) {
                event.setCanceled(true);
            }
        }
    }
}