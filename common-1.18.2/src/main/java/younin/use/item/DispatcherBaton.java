package younin.use.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 调度棒：与 MTR 的扳手、刷子等效。
 *
 * 做法是把玩家手里的物品临时换成 MTR 的扳手 / 刷子，让方块自己原有的判定逻辑照常生效，
 * 调用返回后再还原。这样不需要了解每个方块的内部实现，也不用为不同 MTR 版本编译两份代码。
 */
public class DispatcherBaton extends Item {
    private static final String[] PROXY_ITEMS = {"mtr:wrench", "mtr:brush"};

    public DispatcherBaton(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Player player = context.getPlayer();
        if (player == null || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = level.getBlockState(pos);
        final InteractionHand hand = context.getHand();
        final BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());

        for (final String id : PROXY_ITEMS) {
            final Item proxy = Registry.ITEM.get(new ResourceLocation(id));

            if (proxy == Items.AIR) {
                continue;
            }

            final ItemStack previous = player.getItemInHand(hand);

            try {
                player.setItemInHand(hand, new ItemStack(proxy));
                final InteractionResult result = state.use(level, player, hand, hitResult);

                if (result.consumesAction()) {
                    return result;
                }
            } finally {
                player.setItemInHand(hand, previous);
            }
        }

        return InteractionResult.PASS;
    }
}
