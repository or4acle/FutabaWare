package me.FutabaWare.features.modules.schem;

import me.FutabaWare.features.modules.Module;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;

public class Printer extends Module {
    public Printer() {
        super("Printer", "Printer for schematica", Category.SAL, true, false, false);
    }

    @Override
    public void onEnable(){
        SchematicPrinter.INSTANCE.setPrinting(this.isEnabled());
    }

    @Override
    public void onDisable(){SchematicPrinter.INSTANCE.setPrinting(false);}
}
