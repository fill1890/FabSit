package net.fill1890.fabsit;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fill1890.fabsit.command.GenericSitBasedCommand;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.event.UseStairCallback;
import net.fill1890.fabsit.network.PoseRequestC2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabSitServer implements DedicatedServerModInitializer {
    private static int tickDebounce = 0;

    @Override
    public void onInitializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(FabSit.LOADED_CHANNEL, FabSitServer::handleCheckResponse);
        ServerPlayNetworking.registerGlobalReceiver(FabSit.REQUEST_CHANNEL, FabSitServer::handlePoseRequest);

        // on player joins, ping them with a fabsit check packet to see if they have the mod loaded
        ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler networkHandler, PacketSender sender, MinecraftServer server)
                -> sender.sendPacket(FabSit.LOADED_CHANNEL, PacketByteBufs.empty()));

        // TODO: per player debounce?
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            if (tickDebounce > 0) {
                tickDebounce--;
            }
        });

        UseBlockCallback.EVENT.register(UseStairCallback::interact);
    }

    // if the client has the mod loaded, add them to the supported player registry
    private static void handleCheckResponse(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler networkHandler, PacketByteBuf packetByteBuf, PacketSender sender) {
        server.execute(() -> ConfigManager.loadedPlayers.add(player));
    }

    // attempt to pose when requested
    private static void handlePoseRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        if(tickDebounce == 0) {
            GenericSitBasedCommand.run(player, new PoseRequestC2SPacket(buf).getPose());
            tickDebounce = 10; // wait at least half a second before registering another keypress
        }
    }
}
