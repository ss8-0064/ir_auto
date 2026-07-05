package com.irs.signals.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TrainDetector {

    private static Boolean irPresent = null;
    private static Class<? extends Entity> rollingStockClass = null;

    @SuppressWarnings("unchecked")
    private static boolean checkIR() {
        if (irPresent != null) return irPresent;
        try {
            Class<?> cls = Class.forName("cam72cam.immersiverailroading.entity.EntityRollingStock");
            if (Entity.class.isAssignableFrom(cls)) {
                rollingStockClass = (Class<? extends Entity>) cls;
                irPresent = true;
            } else {
                irPresent = false;
            }
        } catch (ClassNotFoundException e) {
            irPresent = false;
        } catch (Throwable t) {
            irPresent = false;
        }
        return irPresent;
    }

    public static boolean isTrainNearby(World world, BlockPos pos, double range) {
        if (world == null || world.isRemote) return false;
        if (!checkIR() || rollingStockClass == null) return false;

        try {
            AxisAlignedBB box = new AxisAlignedBB(pos).grow(range);
            List<? extends Entity> entities =
                    world.getEntitiesWithinAABB(rollingStockClass, box);
            return !entities.isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }
}
