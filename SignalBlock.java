package com.irs.signals.block;

import com.irs.signals.IRSMod;
import com.irs.signals.SignalAspect;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBlock extends BlockContainer {
    public static final String REGISTRY_NAME = "signal";

    public static final PropertyEnum<SignalAspect> ASPECT =
            PropertyEnum.create("aspect", SignalAspect.class);
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public SignalBlock() {
        super(Material.IRON);
        setRegistryName(IRSMod.MODID, REGISTRY_NAME);
        setUnlocalizedName(IRSMod.MODID + ".signal");
        setCreativeTab(CreativeTabs.TRANSPORTATION);
        setHardness(3.0F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.5F);
        setDefaultState(blockState.getBaseState()
                .withProperty(ASPECT, SignalAspect.RED)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    // ===== TileEntity =====

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SignalTileEntity();
    }

    // ===== Rendering =====

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    // ===== Placement =====

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer) {
        return getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // ===== Metadata <-> State =====

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta & 3);
        SignalAspect aspect = SignalAspect.values()[(meta >> 2) % 3];
        return getDefaultState()
                .withProperty(FACING, facing)
                .withProperty(ASPECT, aspect);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(ASPECT).ordinal() << 2);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ASPECT, FACING);
    }

    // ===== Cleanup =====

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SignalTileEntity) {
            ((SignalTileEntity) te).onRemoved();
        }
        super.breakBlock(world, pos, state);
    }
}
