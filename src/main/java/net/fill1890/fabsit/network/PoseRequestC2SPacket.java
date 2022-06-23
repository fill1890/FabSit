package net.fill1890.fabsit.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fill1890.fabsit.entity.Pose;
import net.minecraft.network.PacketByteBuf;

public class PoseRequestC2SPacket {
    private final Pose pose;

    public PoseRequestC2SPacket(Pose pose) { this.pose = pose; }

    public PoseRequestC2SPacket(PacketByteBuf buf) { this.pose = buf.readEnumConstant(Pose.class); }

    public PacketByteBuf buf() {
        return PacketByteBufs.create().writeEnumConstant(this.pose);
    }

    public Pose getPose() { return pose; }
}
