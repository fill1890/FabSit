package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.error.PoseException;
import net.fill1890.fabsit.util.PoseTest;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * /spin command implementation
 * <br>
 * Requires <code>fabsit.commands.spin</code> permission node, granted to all players by default
 * <br>
 * Implementation details taken from <a href="https://github.com/Gecolay/GSit">GSit</a>
 */
public class SpinCommand {
    protected static final Pose POSE = Pose.SPINNING;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
       dispatcher.register(literal("spin")
               .requires(Permissions.require("fabsit.commands.spin", true))
               .executes(SpinCommand::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        ServerPlayerEntity player;

        try {
            player = source.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("You must be a player to run this command!"));
            return -1;
        }

        if(!PoseTest.confirmEnabled(player, POSE)) return -1;

        // toggle sitting if the player was sat down
        if(player.hasVehicle()) {
            player.dismountVehicle();
            player.teleport(player.getX(), player.getY() + 0.6, player.getZ());
            return 1;
        }

        // confirm player can pose right now
        try {
            PoseTest.confirmPosable(player, POSE);
        } catch(PoseException ignored) { return -1; }

        // create a new pose manager for spinning and sit the player down
        // (player is then invisible and an npc spins)
        PoseManagerEntity chair = new PoseManagerEntity(player.getEntityWorld(), player.getPos(), POSE, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;

    }
}
