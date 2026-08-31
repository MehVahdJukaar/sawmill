package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SawmillBlock extends WaterBlock {

    private static final Component CONTAINER_TITLE = Component.translatable("container.sawmill.sawmill");
    protected static final VoxelShape SHAPE_Z =
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
                    Block.box(6.0, 7.0, 0.0, 10.0, 16.0, 16.0));
    protected static final VoxelShape SHAPE_X =
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
                    Block.box(0.0, 7.0, 6.0, 16.0, 16.0, 10.0));

    protected static final VoxelShape SHAPE_Z_UP =
            Shapes.or(Block.box(0.0, 9.0, 0.0, 16.0, 16.0, 16.0),
                    Block.box(6.0, 0.0, 0.0, 10.0, 9.0, 16.0));
    protected static final VoxelShape SHAPE_X_UP =
            Shapes.or(Block.box(0.0, 9.0, 0.0, 16.0, 16.0, 16.0),
                    Block.box(0.0, 0.0, 6.0, 16.0, 9.0, 10.0));

    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SawmillBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(BOTTOM, true));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, BOTTOM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        return direction != Direction.DOWN && (direction == Direction.UP ||
                context.getClickLocation().y - blockPos.getY() <= 0.5) ? blockState :
                blockState.setValue(BOTTOM, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if (menuProvider != null && player instanceof ServerPlayer serverPlayer) {
            // must use moonlight helper since the menu type is registered as an extended/synced
            // menu (RegHelper.registerMenuType), vanilla openMenu would crash on fabric
            PlatHelper.openCustomMenu(serverPlayer, menuProvider, buf -> {
            });
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new SawmillMenu(i, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BOTTOM)) {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_Z : SHAPE_X;
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_Z_UP : SHAPE_X_UP;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
