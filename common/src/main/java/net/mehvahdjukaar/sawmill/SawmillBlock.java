package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
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
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SawmillBlock() {
        super(Properties.of()
                .explosionResistance(2.5f)
                .destroyTime(2.5f)
                .sound(SoundType.WOOD).mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS));
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
            return InteractionResult.CONSUME;
        }
    }

    @NotNull
    private static InteractionResult debugSpawnAllVillages(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            int off = 0;
            int zOff = 0;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
            int max = 1000;
            for (var t : structureTemplateManager.listTemplates().toList()) {
                String string = t.toString();
                if (string.contains("zombie")) {
                    var template = structureTemplateManager.get(t).get();
                    BlockPos offset = pos.offset(off, 0, zOff);
                    level.setBlock(offset, Blocks.STRUCTURE_BLOCK.defaultBlockState(), 3);
                    var te = BlockEntityType.STRUCTURE_BLOCK.getBlockEntity(level, offset);
                    CompoundTag compoundTag = te.saveWithoutMetadata();
                    compoundTag.putString("name", string);
                    te.load(compoundTag);
                    te.loadStructure(serverLevel, false, template);
                    off += template.getSize().get(Direction.Axis.X) + 3;

                    if (max-- < 0) break;
                    if (off > 400) {
                        zOff += 18;
                        off = 0;
                    }
                }
            }


        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new SawmillMenu(i, inventory, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BOTTOM)) {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_Z : SHAPE_X;
        } else {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_Z_UP : SHAPE_X_UP;
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

}
