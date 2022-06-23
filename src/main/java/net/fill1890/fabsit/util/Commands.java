package net.fill1890.fabsit.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fill1890.fabsit.command.LayCommand;
import net.fill1890.fabsit.command.ReloadConfigCommand;
import net.fill1890.fabsit.command.SitCommand;
import net.fill1890.fabsit.command.SpinCommand;

public abstract class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(SitCommand::register);
        CommandRegistrationCallback.EVENT.register(LayCommand::register);
        CommandRegistrationCallback.EVENT.register(SpinCommand::register);
        CommandRegistrationCallback.EVENT.register(ReloadConfigCommand::register);
    }
}
