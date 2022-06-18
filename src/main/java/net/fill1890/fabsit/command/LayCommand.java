package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * /lay command implementation
 * <br>
 * Requires <code>fabsit.lay</code> permission node, granted to all players by default
 * <br>
 * Implementation details taken from <a href="https://github.com/Gecolay/GSit">GSit</a>
 */
public class LayCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
       dispatcher.register(literal("lay")
               .requires(Permissions.require("fabsit.commands.lay", true))
               .executes(LayCommand::run));
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

        if(player.hasVehicle()) {
            player.dismountVehicle();
            player.teleport(player.getX(), player.getY() + 0.6, player.getZ());
            return 1;
        }

        BlockState standingBlock = player.getEntityWorld().getBlockState(new BlockPos(player.getPos()).down());
        if(
                player.isFallFlying()
                || player.isSleeping()
                || player.isSwimming()
                || player.isSpectator()
                || standingBlock.isAir()
        ) { return -1; }

        float yaw = player.getYaw();
        yaw = (yaw % 360 + 360) % 360;

        // create a new pose manager for laying and sit the player down
        // (player is then invisible and an npc lays down)
        PoseManagerEntity chair = new PoseManagerEntity(player.getEntityWorld(), player.getPos(), yaw, Pose.LAYING, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;

    }
}
