package me.FutabaWare.util;

import club.minnced.discord.rpc.*;

import me.FutabaWare.FutabaWare;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;

public class DiscordPresence {

    private static final String APPLICATION_ID =
            "1511490410432954458";

    private static DiscordRPC rpc;

    private static DiscordRichPresence presence;

    private static Thread callbackThread;

    public static void start() {

        try {

            rpc = DiscordRPC.INSTANCE;

            presence = new DiscordRichPresence();

            DiscordEventHandlers handlers =
                    new DiscordEventHandlers();

            handlers.disconnected =
                    (errorCode, message) ->
                            FutabaWare.LOGGER.warn(
                                    "Discord RPC disconnected: "
                                            + errorCode
                                            + " "
                                            + message
                            );

            rpc.Discord_Initialize(
                    APPLICATION_ID,
                    handlers,
                    true,
                    ""
            );

            presence.startTimestamp =
                    System.currentTimeMillis() / 1000L;

            presence.largeImageKey =
                    "futabaware";

            presence.largeImageText =
                    "FutabaWare v"
                            + FutabaWare.MODVER;

            rpc.Discord_UpdatePresence(
                    presence
            );

            callbackThread =
                    new Thread(
                            DiscordPresence::loop,
                            "FutabaWare-RPC"
                    );

            callbackThread.start();

            FutabaWare.LOGGER.info(
                    "Discord RPC loaded."
            );

        } catch (Throwable t) {

            FutabaWare.LOGGER.error(
                    "Failed to initialize Discord RPC.",
                    t
            );
        }

    }

    private static void loop() {

        while (!Thread.currentThread().isInterrupted()) {

            try {

                rpc.Discord_RunCallbacks();

                updatePresence();

                Thread.sleep(5000L);

            } catch (Throwable ignored) {

            }

        }

    }

    private static void updatePresence() {

        Minecraft mc =
                Minecraft.getMinecraft();

        String details;
        String state;

        if (mc.currentScreen instanceof GuiMainMenu) {

            details =
                    "In Main Menu";

            state =
                    "Choosing modules";

        }

        else if (mc.isIntegratedServerRunning()) {

            details =
                    "Singleplayer";

            state =
                    "Offline world";

        }

        else {

            ServerData server =
                    mc.getCurrentServerData();

            if (server != null) {

                details =
                        "Multiplayer";

                state =
                        server.serverIP;

            }

            else {

                details =
                        "Loading";

                state =
                        "Starting Minecraft";

            }

        }

        presence.details =
                details;

        presence.state =
                state;

        rpc.Discord_UpdatePresence(
                presence
        );

    }

    public static void stop() {

        try {

            if (callbackThread != null)
                callbackThread.interrupt();

            if (rpc != null)
                rpc.Discord_Shutdown();

        } catch (Throwable ignored) {

        }

    }

}