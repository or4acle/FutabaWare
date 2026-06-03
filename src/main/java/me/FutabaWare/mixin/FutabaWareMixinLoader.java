package me.FutabaWare.mixin;

import me.FutabaWare.FutabaWare;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class FutabaWareMixinLoader
        implements IFMLLoadingPlugin {
    private static boolean isObfuscatedEnvironment = false;

    public FutabaWareMixinLoader() {
        FutabaWare.LOGGER.info("\n\nLoading mixins by or4acle");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.FutabaWare.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        FutabaWare.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
    }

    public String[] getASMTransformerClass() {
        return new String[0];
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        isObfuscatedEnvironment = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

