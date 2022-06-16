package net.fill1890.fabsit.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fill1890.fabsit.entity.LayingEntity;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.PoseManagerEntity;
import net.fill1890.fabsit.entity.PosingEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.fill1890.fabsit.mixin.LivingEntityAccessor.getSLEEPING_POSITION;

public class LayCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
       dispatcher.register(literal("lay")
               .requires(Permissions.require("fabsit.lay", true))
               .executes(LayCommand::run));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

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

        PoseManagerEntity chair = new PoseManagerEntity(player.getEntityWorld(), player.getPos(), yaw, Pose.LAYING, player);
        player.getEntityWorld().spawnEntity(chair);
        player.startRiding(chair, true);

        return 1;

    }
}
