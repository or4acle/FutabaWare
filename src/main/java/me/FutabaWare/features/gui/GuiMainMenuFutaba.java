package me.FutabaWare.features.gui;

import me.FutabaWare.FutabaWare;
import me.FutabaWare.util.RenderUtil;
import me.FutabaWare.manager.UpdateManager; // Import do sistema de Update

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Random;

public class GuiMainMenuFutaba extends GuiScreen {

    private boolean isUpdating = false;

    // ─── Constantes ───────────────────────────────────────
    private static final int   COLOR_GREEN  = 0x39FF14;
    private static final int   COLOR_WHITE  = Color.WHITE.getRGB();
    private static final int   COLOR_YELLOW = Color.YELLOW.getRGB();
    private static final int   COLOR_GRAY   = 0xAAAAAA;
    private static final float TITLE_SCALE  = 3.0f;
    private static final float BG_PARALLAX_X = 32f;
    private static final float BG_PARALLAX_Y = 18f;

    private static final String[] SPLASHES = {
            "Futaba Best Girl",
            "or4acle was here",
            "do it for her.",
            "epic",
            "NOT SKIDDED!!!!",
            "FutabaWare > other clients",
            "No Future",
            "Based",
            "Skidware",
            "Persona Enjoyer",
            "Welcome Back",
            "Minecraft 1.12.2",
            "Certified Anarchy Client",
            "8b8t.net supremacy",
            "Crystal PvP enjoyer",
            "Inaba Best Fox"
    };

    // ─── Campos ───────────────────────────────────────────
    private final ResourceLocation background;
    private final String           splashText;

    private float xOffset;
    private float yOffset;
    private int   x;
    private int   y;

    // Animação do splash (oscila como no vanilla)
    private float splashAnim = 0f;
    private float splashDir  = 1f;

    // ─── Construtor ───────────────────────────────────────
    public GuiMainMenuFutaba() {
        Random rng = new Random();
        this.background  = new ResourceLocation(FutabaWare.MODID,
                "textures/background" + (rng.nextInt(3) + 1) + ".png");
        this.splashText  = SPLASHES[rng.nextInt(SPLASHES.length)];
    }

    // ─── Cycle do splash ──────────────────────────────────
    @Override
    public void updateScreen() {
        super.updateScreen();
        splashAnim += 0.05f * splashDir;
        if (splashAnim >  1f) { splashAnim =  1f; splashDir = -1f; }
        if (splashAnim < -1f) { splashAnim = -1f; splashDir =  1f; }
    }

    // ─── Init ─────────────────────────────────────────────
    @Override
    public void initGui() {
        this.x = this.width / 2;
        this.y = this.height / 2 - 20;

        this.buttonList.clear();
        this.buttonList.add(new TextButton(0, x, y + 20, "Singleplayer"));
        this.buttonList.add(new TextButton(1, x, y + 44, "Multiplayer"));
        this.buttonList.add(new TextButton(2, x, y + 66, "Options"));
        this.buttonList.add(new TextButton(3, x, y + 88, "Exit"));
        this.buttonList.add(new TextButton(4, x, y + 110, "Reload Client"));

        // Se tiver update, cria um botão destacado no final com o ID 5
        if (UpdateManager.needsUpdate) {
            this.buttonList.add(new TextButton(5, this.x, this.y + 132, "NEW UPDATE DROPPED: FUTABAWARE (v" + UpdateManager.latestVersion + ")"));
        }
    }

    // ─── Ações ────────────────────────────────────────────
    @Override
    protected void actionPerformed(GuiButton button) {
        if (isUpdating) return; // Trava os botões se estiver baixando update

        switch (button.id) {
            case 0: mc.displayGuiScreen(new GuiWorldSelection(this)); break;
            case 1: mc.displayGuiScreen(new GuiMultiplayer(this));    break;
            case 2: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
            case 3: mc.shutdown(); break;
            case 4:
                FutabaWare.reload();
                mc.displayGuiScreen(new GuiMainMenuFutaba());
                break;
            case 5: // Ação do botão de Update
                isUpdating = true;
                UpdateManager.downloadAndInstallUpdate();
                break;
        }
    }

    // ─── Render ───────────────────────────────────────────
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.x = this.width / 2;
        this.y = this.height / 2 - 20;

        this.xOffset = -1f * ((mouseX - this.width  / 2f) / (this.width  / BG_PARALLAX_X));
        this.yOffset = -1f * ((mouseY - this.height / 2f) / (this.height / BG_PARALLAX_Y));

        // Background com parallax
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        this.mc.getTextureManager().bindTexture(this.background);
        drawCompleteImage(-16f + xOffset, -9f + yOffset, this.width + 32, this.height + 18);

        // Overlay escuro suave para legibilidade
        drawRect(0, 0, this.width, this.height, 0x55000000);

        // ── Título ──
        GlStateManager.pushMatrix();
        GlStateManager.scale(TITLE_SCALE, TITLE_SCALE, TITLE_SCALE);
        float titleW = FutabaWare.textManager.getStringWidth("FutabaWare");
        FutabaWare.textManager.drawStringWithShadow(
                "FutabaWare",
                (this.width / TITLE_SCALE) / 2f - titleW / 2f,
                12,
                COLOR_GREEN
        );
        GlStateManager.popMatrix();

        // ── Versão ──
        String version = "v" + FutabaWare.MODVER;
        FutabaWare.textManager.drawStringWithShadow(
                version,
                this.width / 2f - FutabaWare.textManager.getStringWidth(version) / 2f,
                72,
                COLOR_WHITE
        );

        // ── Splash com leve oscilação ──
        float splashScale = 1.0f + splashAnim * 0.03f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2f, 84, 0);
        GlStateManager.scale(splashScale, splashScale, splashScale);
        FutabaWare.textManager.drawStringWithShadow(
                splashText,
                -FutabaWare.textManager.getStringWidth(splashText) / 2f,
                0,
                COLOR_YELLOW
        );
        GlStateManager.popMatrix();

        // ── Crédito no canto ──
        String credit = "by or4acle";
        FutabaWare.textManager.drawStringWithShadow(
                credit,
                4,
                this.height - FutabaWare.textManager.getFontHeight() - 4,
                COLOR_GRAY
        );

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Aviso visual enquanto baixa a atualização
        if (isUpdating) {
            String updateText = "Downloading update. This instance will shutdown soon.";
            FutabaWare.textManager.drawStringWithShadow(
                    updateText,
                    this.width / 2f - FutabaWare.textManager.getStringWidth(updateText) / 2f,
                    this.y + 155,
                    Color.RED.getRGB()
            );
        }
    }

    // ─── Helper de imagem ────────────────────────────────
    public static void drawCompleteImage(float posX, float posY, float width, float height) {
        GL11.glPushMatrix();
        GL11.glTranslatef(posX, posY, 0f);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(0f,     0f,      0f);
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f(0f,     height,  0f);
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f(width,  height,  0f);
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f(width,  0f,      0f);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    // ─── Botão de texto ───────────────────────────────────
    private static class TextButton extends GuiButton {

        TextButton(int id, int x, int y, String text) {
            super(id, x, y,
                    FutabaWare.textManager.getStringWidth(text),
                    FutabaWare.textManager.getFontHeight(),
                    text);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) return;

            float halfW = FutabaWare.textManager.getStringWidth(this.displayString) / 2f;

            this.hovered = (float) mouseX >= this.x - halfW
                    && mouseY >= this.y
                    && mouseX <  this.x + this.width
                    && mouseY <  this.y + this.height;

            // Fica vermelho se for o botão de Update
            int color = this.hovered ? 0xFFFFFF : (this.id == 5 ? Color.RED.getRGB() : 0x00FF00);

            FutabaWare.textManager.drawStringWithShadow(
                    this.displayString,
                    this.x - halfW,
                    this.y,
                    color
            );

            if (this.hovered) {
                RenderUtil.drawLine(
                        this.x - 1 - halfW,
                        this.y + 2 + FutabaWare.textManager.getFontHeight(),
                        this.x     + halfW + 1f,
                        this.y + 2 + FutabaWare.textManager.getFontHeight(),
                        1f,
                        this.id == 5 ? Color.RED.getRGB() : 0x39FF14
                );
            }
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (!this.enabled || !this.visible) return false;
            float halfW = FutabaWare.textManager.getStringWidth(this.displayString) / 2f;
            return (float) mouseX >= this.x - halfW
                    && mouseY >= this.y
                    && mouseX <  this.x + this.width
                    && mouseY <  this.y + this.height;
        }
    }
}