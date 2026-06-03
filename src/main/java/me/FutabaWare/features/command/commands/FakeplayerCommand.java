package me.FutabaWare.features.command.commands;

import me.FutabaWare.features.command.Command;
import me.FutabaWare.features.modules.player.FakePlayer;

public class FakeplayerCommand
        extends Command {
    public FakeplayerCommand() {
        super("fakeplayer");
    }

    @Override
    public void execute(String[] commands) {
        FakePlayer.getInstance().toggle();
    }
}