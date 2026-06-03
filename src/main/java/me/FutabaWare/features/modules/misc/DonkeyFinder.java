package me.FutabaWare.features.modules.misc;

import me.FutabaWare.features.command.Command;
import me.FutabaWare.features.modules.Module;
import me.FutabaWare.features.setting.Setting;
import me.FutabaWare.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.init.SoundEvents;

import java.util.HashSet;
import java.util.Set;

public
class DonkeyFinder
        extends Module {
    private final Set<Entity> donkey = new HashSet< Entity >( );
    public Setting< Boolean > Chat = this.register ( new Setting < Boolean > ( "Chat" , true ) );
    public Setting < Boolean > Sound = this.register ( new Setting < Boolean > ( "Sound" , true ) );

    public DonkeyFinder( ) {
        super ( "DonkeyFinder+" , "Finding donkey speedrun" , Module.Category.MISC , true , false , false );
    }

    @Override
    public
    void onEnable ( ) {
        this.donkey.clear ( );
    }

    @Override
    public void onUpdate ( ) {
        for (Entity entity : DonkeyFinder.mc.world.getLoadedEntityList ( )) {
            if ( ! ( entity instanceof EntityDonkey) || this.donkey.contains ( entity ) ) continue;
            if ( this.Chat.getValue ( ) ) {
                Command.sendMessage (TextUtil.GREEN + "Donkey: " + entity.getPosition ( ).getX ( ) + "x, " + entity.getPosition ( ).getY ( ) + "y, " + entity.getPosition ( ).getZ ( ) + "z." );
            }
            this.donkey.add ( entity );
            if ( ! this.Sound.getValue ( ) ) continue;
            DonkeyFinder.mc.player.playSound ( SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE , 1.0f , 1.0f );
        }
    }
}