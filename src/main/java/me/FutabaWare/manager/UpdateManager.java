package me.FutabaWare.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.FutabaWare.FutabaWare;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateManager {

    // Substitua pelo seu usuário e nome do repositório
    private static final String API_URL = "https://api.github.com/repos/or4acle/FutabaWare/releases/latest";

    public static String latestVersion = "";
    public static String downloadUrl = "";
    public static boolean needsUpdate = false;

    public static void checkForUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // O GitHub exige um User-Agent para acessar a API
                conn.setRequestProperty("User-Agent", "FutabaWare-Updater");

                if (conn.getResponseCode() == 200) {
                    JsonObject json = new JsonParser().parse(new InputStreamReader(conn.getInputStream())).getAsJsonObject();

                    // Pega a versão baseada na Tag do GitHub (ex: "2.7")
                    latestVersion = json.get("tag_name").getAsString();

                    // Compara a versão do GitHub com a versão atual do FutabaWare.java
                    if (!latestVersion.equalsIgnoreCase(FutabaWare.MODVER)) {
                        JsonArray assets = json.getAsJsonArray("assets");
                        if (assets.size() > 0) {
                            JsonObject asset = assets.get(0).getAsJsonObject();
                            downloadUrl = asset.get("browser_download_url").getAsString();
                            needsUpdate = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void downloadAndInstallUpdate() {
        new Thread(() -> {
            try {
                // 1. Pega o arquivo .jar atual que está rodando
                File currentJar = new File(FutabaWare.class.getProtectionDomain().getCodeSource().getLocation().toURI());

                // Se estiver rodando no IntelliJ (ambiente de dev), aborta para não quebrar o projeto
                if (currentJar.isDirectory()) return;

                File modsDir = currentJar.getParentFile();
                File tempUpdateFile = new File(modsDir, "FutabaWare-Update.jar");

                // 2. Baixa o novo .jar do GitHub
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "FutabaWare-Updater");
                InputStream in = conn.getInputStream();
                Files.copy(in, tempUpdateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                in.close();

                // 3. Cria um script .bat para fazer a substituição enquanto o jogo fecha
                File batFile = new File(Minecraft.getMinecraft().gameDir, "update_futabaware.bat");
                String batContent = "@echo off\n" +
                        "timeout /t 3 /nobreak > NUL\n" + // Espera 3 segundos pro Minecraft fechar totalmente
                        "del /f /q \"" + currentJar.getAbsolutePath() + "\"\n" + // Deleta a versão antiga
                        "ren \"" + tempUpdateFile.getAbsolutePath() + "\" \"FutabaWare-" + latestVersion + ".jar\"\n" + // Renomeia a nova
                        "del \"%~f0\"\n"; // O script se auto-deleta no final

                FileOutputStream fos = new FileOutputStream(batFile);
                fos.write(batContent.getBytes());
                fos.close();

                // 4. Executa o script invisível e desliga o Minecraft
                Runtime.getRuntime().exec("cmd /c start /min \"\" \"" + batFile.getAbsolutePath() + "\"");
                Minecraft.getMinecraft().shutdown();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}