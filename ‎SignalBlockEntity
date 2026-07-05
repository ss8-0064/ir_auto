package com.irs.signals.blockentity;

import com.irs.signals.Config;
import com.irs.signals.IRSMod;
import com.irs.signals.SignalAspect;
import com.irs.signals.block.SignalBlock;
import com.irs.signals.util.TrainDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 信号机方块实体 —— 固定闭塞逻辑的核心。
 *
 * <p>固定闭塞（Fixed Block Signalling）原理：
 * <ul>
 *   <li>线路被相邻信号机划分为若干固定的闭塞区间（Track Circuit）。</li>
 *   <li>每架信号机守护其“前方”一个闭塞区间：从本信号机沿朝向延伸到“下一架信号机”为止。</li>
 *   <li>检测该区间内是否存在列车（滚动车辆），据此决定显示：
 *     <ul>
 *       <li>本区间有车 → 红（停车）</li>
 *       <li>本区间无车，但下一架信号机为红 → 黄（减速）</li>
 *       <li>本区间及前方均无车 → 绿（通行）</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>“下一架信号机”由玩家使用 {@link com.irs.signals.item.SignalLinkerItem} 显式连接，
 * 从而定义闭塞区间的拓扑。未连接时使用默认长度形成末端区间。
 */
public class SignalBlockEntity extends BlockEntity {

    /** 本信号机前方所指的下一架信号机（定义闭塞区间边界）。null 表示线路末端。 */
    private BlockPos nextSignal = null;

    /** 扫描计数器，配合错峰避免所有信号机同 tick 扫描。 */
    private int tickCounter = 0;

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(IRSMod.SIGNAL_BE.get(), pos, state);
        // 按位置错峰，分散首次扫描。
        this.tickCounter = (pos.getX() ^ pos.getY() ^ pos.getZ()) & 31;
    }

    public SignalAspect getAspect() {
        BlockState st = getBlockState();
        return st.hasProperty(SignalBlock.ASPECT) ? st.getValue(SignalBlock.ASPECT) : SignalAspect.RED;
    }

    public BlockPos getNextSignal() {
        return nextSignal;
    }

    public void setNextSignal(BlockPos pos) {
        this.nextSignal = pos;
        setChanged();
    }

    public void clearNextSignal() {
        this.nextSignal = null;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SignalBlockEntity be) {
        be.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        tickCounter++;
        int interval = Math.max(5, Config.SCAN_INTERVAL.get());
        if (tickCounter < interval) return;
        tickCounter = 0;

        // 1) 本闭塞区间占用检测
        boolean occupied = isSectionOccupied(level, pos, state);

        // 2) 下一架信号机的当前显示
        SignalAspect nextAspect = nextSignalAspect(level);

        // 3) 计算本信号机应显示
        SignalAspect newAspect;
        if (occupied) {
            newAspect = SignalAspect.RED;
        } else if (nextAspect == SignalAspect.RED) {
            newAspect = SignalAspect.YELLOW;
        } else {
            newAspect = SignalAspect.GREEN;
        }

        // 4) 若发生变化则写回方块状态（自动同步到客户端渲染）
        if (newAspect != getAspect()) {
            level.setBlock(pos, state.setValue(SignalBlock.ASPECT, newAspect), 3);
            if (Config.LOG_ASPECT_CHANGES.get()) {
                IRSMod.LOGGER.debug("[IRS Signals] 信号机 {} 显示变更 -> {}", pos, newAspect);
            }
        }
    }

    private SignalAspect nextSignalAspect(Level level) {
        if (nextSignal == null) {
            // 线路末端：前方视为空闲。
            return SignalAspect.GREEN;
        }
        BlockEntity be = level.getBlockEntity(nextSignal);
        if (be instanceof SignalBlockEntity sig) {
            return sig.getAspect();
        }
        // 下一架不存在或区块未加载：保守视为空闲（与真实信号系统一致）。
        return SignalAspect.GREEN;
    }

    private boolean isSectionOccupied(Level level, BlockPos pos, BlockState state) {
        AABB box = computeSectionAABB(pos, state);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, box);
        for (Entity e : entities) {
            if (TrainDetector.isTrain(e)) {
                return true;
            }
        }
        return false;
    }

    private AABB computeSectionAABB(BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(SignalBlock.FACING)
                ? state.getValue(SignalBlock.FACING)
                : Direction.NORTH;

        BlockPos start = pos.relative(facing);
        BlockPos end;
        if (nextSignal != null && !nextSignal.equals(pos)) {
            end = nextSignal;
        } else {
            end = pos.relative(facing, Math.max(1, Config.DEFAULT_SECTION_LENGTH.get()));
        }

        double minX = Math.min(start.getX(), end.getX()) - 0.5;
        double minY = Math.min(start.getY(), end.getY()) - 1.0;
        double minZ = Math.min(start.getZ(), end.getZ()) - 0.5;
        double maxX = Math.max(start.getX(), end.getX()) + 1.5;
        double maxY = Math.max(start.getY(), end.getY()) + 3.0;
        double maxZ = Math.max(start.getZ(), end.getZ()) + 1.5;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (nextSignal != null) {
            tag.putLong("NextSignal", nextSignal.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("NextSignal")) {
            nextSignal = BlockPos.of(tag.getLong("NextSignal"));
        } else {
            nextSignal = null;
        }
    }
}
