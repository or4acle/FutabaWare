package me.FutabaWare.features.modules.client;

import club.minnced.discord.rpc.*;
import me.FutabaWare.features.modules.Module;
import net.minecraft.client.multiplayer.ServerData;

public class DiscordRPCModule extends Module {

    private static DiscordRPCModule INSTANCE;

    private DiscordRPC rpc;
    private DiscordRichPresence presence;

    private long startTimestamp;

    public DiscordRPCModule() {
        super(
                "DiscordRPC",
                "Shows Discord Rich Presence",
                Category.CLIENT,
                false,
                false,
                false
        );

        INSTANCE = this;
    }

    public static DiscordRPCModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {

        startTimestamp = System.currentTimeMillis() / 1000L;

        rpc = DiscordRPC.INSTANCE;

        DiscordEventHandlers handlers = new DiscordEventHandlers();

        rpc.Discord_Initialize(
                "SEU_APPLICATION_ID",
                handlers,
                true,
                ""
        );

        presence = new DiscordRichPresence();

        presence.startTimestamp = startTimestamp;

        presence.largeImageKey = "logo";
        presence.largeImageText = "FutabaWare";

        updatePresence();

        rpc.Discord_UpdatePresence(presence);

        Thread thread = new Thread(() -> {

            while (isOn()) {

                try {

                    updatePresence();

                    rpc.Discord_RunCallbacks();
                    rpc.Discord_UpdatePresence(presence);

                    Thread.sleep(5000);

                } catch (Exception ignored) {
                }
            }

        }, "Discord-RPC");

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onDisable() {

        if (rpc != null) {
            rpc.Discord_Shutdown();
        }
    }

    private void updatePresence() {

        if (mc.player == null) {

            presence.details = "Main Menu";
            presence.state = "Idle";

            return;
        }

        String server = "Singleplayer";

        if (mc.getCurrentServerData() != null) {
            server = mc.getCurrentServerData().serverIP;
        }

        int players = 0;

        try {

            ServerData data = mc.getCurrentServerData();

            if (data != null
                    && data.populationInfo != null) {

                String population = data.populationInfo;

                population = population.replaceAll("§.", "");

                String[] split = population.split("/");

                if (split.length >= 1) {
                    players = Integer.parseInt(split[0].trim());
                }
            }

        } catch (Exception ignored) {
        }

        int ping = 0;

        try {
            ping = me.FutabaWare.FutabaWare.serverManager.getPing();
        } catch (Exception ignored) {
        }

        float tps = 20.0f;

        try {
            tps = me.FutabaWare.FutabaWare.serverManager.getTPS();
        } catch (Exception ignored) {
        }

        presence.details =
                "Playing on " + server;

        presence.state =
                players + " players | "
                        + ping + "ms | "
                        + String.format("%.1f", tps)
                        + " TPS";
    }
}