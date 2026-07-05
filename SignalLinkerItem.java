package com.irs.signals.item;

import com.irs.signals.blockentity.SignalBlockEntity;
import com.irs.signals.block.SignalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 信号链路工具。
 *
 * 操作方式：
 *   1) 右键点击第一架信号机 → 选定为“起点（A）”。
 *   2) 右键点击第二架信号机 → 将 A 的“下一架信号机”设为 B，完成闭塞区间连接。
 *   3) 潜行右键某架信号机 → 清除该信号机的“下一架”连接。
 *   4) 潜行右键空气 → 取消当前选择。
 *
 * 连接后，A 守护的闭塞区间即从 A 沿朝向延伸到 B；B 进一步连接 C，即可形成多闭塞区间链。
 */
public class SignalLinkerItem extends Item {

    /** 服务端临时存储每位玩家正在进行的链路“起点”选择。 */
    private static final Map<UUID, BlockPos> LINKING_FROM = new HashMap<>();

    public SignalLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.irs_signals.signal_linker.tooltip.line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.irs_signals.signal_linker.tooltip.line2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.irs_signals.signal_linker.tooltip.line3")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (!SignalBlock.isSignal(state)) {
            // 不是信号机，放行，允许其他交互。
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // ---- 服务端逻辑 ----
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SignalBlockEntity sig)) {
            return InteractionResult.SUCCESS;
        }

        // 潜行右键：清除该信号机的下一架连接。
        if (player.isShiftKeyDown()) {
            if (sig.getNextSignal() != null) {
                sig.clearNextSignal();
                player.sendSystemMessage(Component.translatable("msg.irs_signals.link.cleared", pos));
            }
            LINKING_FROM.remove(player.getUUID());
            return InteractionResult.CONSUME;
        }

        UUID id = player.getUUID();
        BlockPos from = LINKING_FROM.get(id);

        if (from == null) {
            // 选定起点。
            LINKING_FROM.put(id, pos);
            player.sendSystemMessage(Component.translatable("msg.irs_signals.link.select", pos));
            return InteractionResult.CONSUME;
        }

        // 已有起点：完成连接 from -> pos
        if (from.equals(pos)) {
            LINKING_FROM.remove(id);
            player.sendSystemMessage(Component.translatable("msg.irs_signals.link.cancel"));
            return InteractionResult.CONSUME;
        }

        BlockEntity fromBe = level.getBlockEntity(from);
        if (fromBe instanceof SignalBlockEntity fromSig) {
            fromSig.setNextSignal(pos);
            player.sendSystemMessage(Component.translatable("msg.irs_signals.link.success", from, pos));
        } else {
            player.sendSystemMessage(Component.translatable("msg.irs_signals.link.lost"));
        }
        LINKING_FROM.remove(id);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 潜行右键空气：取消当前选择
        if (player.isShiftKeyDown() && !level.isClientSide) {
            if (LINKING_FROM.remove(player.getUUID()) != null) {
                player.sendSystemMessage(Component.translatable("msg.irs_signals.link.cancel"));
                return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    }
}
