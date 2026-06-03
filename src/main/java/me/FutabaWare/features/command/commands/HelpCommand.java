package me.FutabaWare.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.FutabaWare.FutabaWare;
import me.FutabaWare.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("Commands: ");
        for (Command command : FutabaWare.commandManager.getCommands()) {
            HelpCommand.sendMessage(ChatFormatting.GRAY + FutabaWare.commandManager.getPrefix() + command.getName());
        }
    }
}

