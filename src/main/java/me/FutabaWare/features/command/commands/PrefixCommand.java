package me.FutabaWare.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.FutabaWare.FutabaWare;
import me.FutabaWare.features.command.Command;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(ChatFormatting.GREEN + "Current prefix is " + FutabaWare.commandManager.getPrefix());
            return;
        }
        FutabaWare.commandManager.setPrefix(commands[0]);
        Command.sendMessage("Prefix changed to " + ChatFormatting.GRAY + commands[0]);
    }
}

