package me.FutabaWare.features.command.commands;

import me.FutabaWare.FutabaWare;
import me.FutabaWare.features.command.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        FutabaWare.unload(true);
    }
}

