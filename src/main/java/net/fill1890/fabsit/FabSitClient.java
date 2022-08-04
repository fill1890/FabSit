package net.fill1890.fabsit;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fill1890.fabsit.keybind.PoseKeybinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FabSitClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // handler for if the server pings us with a loaded check
        ClientLoginNetworking.registerGlobalReceiver(FabSit.LOADED_CHANNEL, FabSitClient::checkPacketReply);

        // renderer is required for all registered entities, but we don't want to render them
        // so pass in an empty renderer
        EntityRendererRegistry.register(FabSit.CHAIR_ENTITY_TYPE, EmptyRenderer::new);
        EntityRendererRegistry.register(FabSit.RAW_CHAIR_ENTITY_TYPE, EmptyRenderer::new);

        // keybinds for posing
        PoseKeybinds.register();
    }

    private static CompletableFuture<PacketByteBuf> checkPacketReply(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listener) {
        // respond to loaded check
        return CompletableFuture.completedFuture(PacketByteBufs.empty());
    }

    // empty renderer - will never render as shouldRender is always false
    private static class EmptyRenderer extends EntityRenderer<Entity> {
        protected EmptyRenderer(EntityRendererFactory.Context ctx) { super(ctx); }

        @Override
        public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) { return false; }

        @Override
        public Identifier getTexture(Entity entity) { return null; }
    }
}
