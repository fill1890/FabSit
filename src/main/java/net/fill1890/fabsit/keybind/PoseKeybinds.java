package net.fill1890.fabsit.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fill1890.fabsit.FabSit;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.network.PoseRequestC2SPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;

public abstract class PoseKeybinds {
    private static final String KEY = "key.fabsit.";
    private static final String CATEGORY = "key.fabsit.category";

    private static final KeyBinding sitKey = emptyKey("sit");
    private static final KeyBinding layKey = emptyKey("lay");
    private static final KeyBinding spinKey = emptyKey("spin");

    private static KeyBinding emptyKey(String base) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(KEY + base, InputUtil.Type.KEYSYM,InputUtil.UNKNOWN_KEY.getCode(), CATEGORY)
        );
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(sitKey.wasPressed()) {
                ClientPlayNetworking.send(FabSit.REQUEST_CHANNEL, new PoseRequestC2SPacket(Pose.SITTING).buf());
            }

            while(layKey.wasPressed()) {
                ClientPlayNetworking.send(FabSit.REQUEST_CHANNEL, new PoseRequestC2SPacket(Pose.LAYING).buf());
            }

            while(spinKey.wasPressed()) {
                ClientPlayNetworking.send(FabSit.REQUEST_CHANNEL, new PoseRequestC2SPacket(Pose.SPINNING).buf());
            }
        });
    }
}
