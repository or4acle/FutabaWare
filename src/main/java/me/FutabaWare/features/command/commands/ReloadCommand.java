package me.FutabaWare.features.command.commands;

import me.FutabaWare.FutabaWare;
import me.FutabaWare.features.command.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        FutabaWare.reload();
    }
}

