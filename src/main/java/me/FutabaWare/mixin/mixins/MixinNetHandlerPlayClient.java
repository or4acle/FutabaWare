package me.FutabaWare.mixin.mixins;

import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    // Intercepta a constante bizarra que a Mojang usou no Vanilla 1.12.2
    @ModifyConstant(method = "handleEntityProperties", constant = @Constant(doubleValue = 2.2250738585072014E-308D))
    private double fixAttributeCrash(double original) {
        // Substitui por 0.0D para que o valor padrão não seja menor que o mínimo
        return 0.0D;
    }
}