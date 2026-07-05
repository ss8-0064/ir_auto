package com.irs.signals.block;

import com.irs.signals.ModConfig;
import com.irs.signals.IRSMod;
import com.irs.signals.SignalAspect;
import com.irs.signals.util.TrainDetector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class SignalTileEntity extends TileEntity implements ITickable {

    private BlockPos nextSignal = null;
    private int updateTimer = 0;

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        updateTimer++;
        if (updateTimer < ModConfig.updateInterval) return;
        updateTimer = 0;

        boolean occupied = TrainDetector.isTrainNearby(world, pos, ModConfig.detectionRange);

        SignalAspect newAspect;
        if (occupied) {
            newAspect = SignalAspect.RED;
        } else if (nextSignal != null) {
            TileEntity nextTe = world.getTileEntity(nextSignal);
            if (nextTe instanceof SignalTileEntity) {
                SignalAspect nextAspect = ((SignalTileEntity) nextTe).getCurrentAspect();
                if (nextAspect == SignalAspect.RED) {
                    newAspect = SignalAspect.YELLOW;
                } else {
                    newAspect = SignalAspect.GREEN;
                }
            } else {
                newAspect = SignalAspect.GREEN;
            }
        } else {
            newAspect = SignalAspect.GREEN;
        }

        setAspect(newAspect);
    }

    public SignalAspect getCurrentAspect() {
        if (world == null) return SignalAspect.RED;
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof SignalBlock) {
            return state.getValue(SignalBlock.ASPECT);
        }
        return SignalAspect.RED;
    }

    private void setAspect(SignalAspect aspect) {
        if (world == null || world.isRemote) return;
        SignalAspect current = getCurrentAspect();
        if (current != aspect) {
            IBlockState state = world.getBlockState(pos);
            IBlockState newState = state.withProperty(SignalBlock.ASPECT, aspect);
            world.setBlockState(pos, newState, 3);
            if (ModConfig.debug) {
                IRSMod.logger.info("Signal at {} changed from {} to {}", pos, current, aspect);
            }
        }
    }

    public void setNextSignal(BlockPos next) {
        this.nextSignal = next;
        markDirty();
    }

    public void clearNextSignal() {
        this.nextSignal = null;
        markDirty();
    }

    public BlockPos getNextSignal() {
        return nextSignal;
    }

    public void onRemoved() {
        // nothing to clean externally for now
    }

    // ===== NBT =====

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (nextSignal != null) {
            compound.setLong("nextSignal", nextSignal.toLong());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("nextSignal")) {
            nextSignal = BlockPos.fromLong(compound.getLong("nextSignal"));
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }
}
