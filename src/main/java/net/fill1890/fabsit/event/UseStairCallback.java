package net.fill1890.fabsit.event;

import net.fill1890.fabsit.command.GenericSitBasedCommand;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.entity.Position;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UseStairCallback {
    public static ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        // canCollide?

        // only allow interaction if enabled
        if(!ConfigManager.getConfig().right_click_sit)
            return ActionResult.PASS;

        // check player isn't spectating, sneaking, currently riding
        if(player.isSpectator())
            return ActionResult.PASS;
        if(player.isSneaking())
            return ActionResult.PASS;
        if(player.hasVehicle())
            return ActionResult.PASS;

        // player needs to click on an up-facing face
        if(hitResult.getSide() != Direction.UP)
            return ActionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // check block is stair or slab
        if(!(block instanceof SlabBlock || block instanceof StairsBlock))
            return ActionResult.PASS;

        // use the block occupation logic since this forces centering
        if(ConfigManager.occupiedBlocks.contains(pos))
            return ActionResult.PASS;

        // player needs to click with an empty hand
        if(!player.getStackInHand(Hand.MAIN_HAND).isEmpty())
            return ActionResult.PASS;

        // bottom slab only
        if(block instanceof SlabBlock && !isBottomSlab(state))
            return ActionResult.PASS;

        // bottom stair only
        if(block instanceof StairsBlock && !isBottomStair(state))
            return ActionResult.PASS;

        // check block above is empty
        if(!world.getBlockState(pos.up()).isAir())
            return ActionResult.PASS;

        // nice looking position
        Vec3d sitPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.4d, pos.getZ() + 0.5d);

        // tweak block position for stairs
        if (block instanceof StairsBlock) {
            sitPos = sitPos.add(switch (state.get(StairsBlock.FACING)) {
                case EAST -> new Vec3d(-0.1, 0, 0);
                case SOUTH -> new Vec3d(0, 0, -0.1);
                case WEST -> new Vec3d(0.1, 0, 0);
                case NORTH -> new Vec3d(0, 0, 0.1);
                default -> throw new IllegalStateException("Unexpected value: " + state.get(StairsBlock.FACING));
            });
        }

        // set up the seat
        GenericSitBasedCommand.run((ServerPlayerEntity) player, Pose.SITTING, sitPos, Position.IN_BLOCK);

        return ActionResult.PASS;
    }

    private static boolean isBottomSlab(BlockState state) {
        return state.getProperties().contains(SlabBlock.TYPE) && state.get(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    private static boolean isBottomStair(BlockState state) {
        return state.getProperties().contains(StairsBlock.HALF) && state.get(StairsBlock.HALF) == BlockHalf.BOTTOM;
    }
}
