package net.fill1890.fabsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.entity.ChairEntity;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SitCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
                CommandManager.literal("sit")
                        .requires(Permissions.require("fabsit.sit", true))
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

        ChairEntity chair = new ChairEntity(player.getEntityWorld(), player.getPos(), yaw);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;
    }
}
