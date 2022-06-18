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

/**
 * /sit command implementation
 * <br>
 * Requires <code>fabsit.sit</code> permission node; granted to all players by default
 * <br>
 * Inspiration taken from <a href="https://github.com/BradBot1/FabricSit">Fabric Sit</a>
 */
public class SitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
                CommandManager.literal("sit")
                        .requires(Permissions.require("fabsit.commands.sit", true))
                        .executes(SitCommand::run));
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

        // toggle sitting if already sat down
        if(player.hasVehicle()) {
            player.dismountVehicle();
            player.teleport(player.getX(), player.getY() + 0.5, player.getZ());
            return 1;
        }

        // Get the lower block to make sure it isn't air
        BlockState standingBlock = player.getEntityWorld().getBlockState(new BlockPos(player.getPos()).down());
        if(
                player.isFallFlying()
                || player.isSleeping()
                || player.isSwimming()
                || player.isSpectator()
                || standingBlock.isAir()
        ) { return -1; }

        // Set initial rotation of the player so the legs line up
        float yaw = player.getYaw();
        yaw = (yaw % 360 + 360) % 360;

        // Create a new pose manager for sitting and sit the player down
        PoseManagerEntity chair = new PoseManagerEntity(player.getEntityWorld(), player.getPos(), yaw, Pose.SITTING, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;
    }
}
