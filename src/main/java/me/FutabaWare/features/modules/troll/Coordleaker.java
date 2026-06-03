package me.FutabaWare.features.modules.troll;

import me.FutabaWare.features.modules.Module;
import net.minecraft.network.play.client.CPacketChatMessage;


public class Coordleaker extends Module {
    public Coordleaker() {
        super("CoordExploit", "troll 69", Module.Category.TROLL, true, false, false);

    }

    @Override
    public
    void onUpdate(){
        if (fullNullCheck()) return;
        mc.player.connection.sendPacket(new CPacketChatMessage("My coords are: " + Math.floor(mc.player.posX) + ", " + Math.floor(mc.player.posY) + ", " + Math.floor(mc.player.posZ) + "! Please, come and kill me!!"));

    }
}
