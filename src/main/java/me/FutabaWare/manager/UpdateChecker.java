package me.FutabaWare.manager;

import me.FutabaWare.FutabaWare;
import me.FutabaWare.features.command.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    // URL do arquivo version.txt no seu repositório
    // Exemplo: https://raw.githubusercontent.com/SeuUser/FutabaWare/main/version.txt
    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/or4acle/FutabaWare/main/version.txt";

    private static final String RELEASES_URL =
            "https://github.com/or4acle/FutabaWare/releases/latest";

    public static void check() {
        // Roda em thread separada para não travar o jogo
        new Thread(() -> {
            try {
                String latestVersion = fetchLatestVersion();
                String currentVersion = FutabaWare.MODVER;

                if (latestVersion == null) {
                    FutabaWare.LOGGER.warn("[UpdateChecker] Could not fetch latest version.");
                    return;
                }

                if (!latestVersion.trim().equals(currentVersion.trim())) {
                    // Notifica no chat quando o jogador logar
                    FutabaWare.LOGGER.info("[UpdateChecker] Update available: "
                            + currentVersion + " -> " + latestVersion);

                    // Guarda pra notificar no onLogin
                    UpdateChecker.pendingMessage =
                            "§a[FutabaWare] §eUpdate available! §7"
                                    + currentVersion + " §e-> §a" + latestVersion
                                    + " §7| " + RELEASES_URL;
                } else {
                    FutabaWare.LOGGER.info("[UpdateChecker] Up to date (" + currentVersion + ").");
                }

            } catch (Exception e) {
                FutabaWare.LOGGER.warn("[UpdateChecker] Check failed: " + e.getMessage());
            }
        }, "FutabaWare-UpdateChecker").start();
    }

    private static String fetchLatestVersion() throws Exception {
        HttpURLConnection conn = (HttpURLConnection)
                new URL(VERSION_URL).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        conn.setRequestProperty("User-Agent", "FutabaWare/" + FutabaWare.MODVER);

        if (conn.getResponseCode() != 200) return null;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return br.readLine();
        }
    }

    // Mensagem pendente para mostrar quando o jogador logar
    public static String pendingMessage = null;
}