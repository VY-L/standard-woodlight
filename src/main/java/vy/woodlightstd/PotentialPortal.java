package vy.woodlightstd;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PotentialPortal {
    private boolean axis;
    public boolean clear;
    public BlockPos lowerCorner;
    private int height;
    private int width;
    public int counter;
    public PotentialPortal(boolean axis, BlockPos lowerCorner, int height, int width, boolean clear) {
        this.axis = axis; // true: east westBl
        this.lowerCorner=lowerCorner;
        this.clear = clear;
        this.height = height;
        this.width = width;
    }
    public PotentialPortal(boolean axis, BlockPos lowerCorner, int height, int width, WorldAccess worldAccess) {
        this.axis = axis;
        this.lowerCorner=lowerCorner;
        this.height = height;
        this.width = width;
        clear = true;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                BlockPos blockPos = lowerCorner.offset(axis ? Direction.EAST : Direction.SOUTH, i).up(j);
                if (!validStateInsidePortal(worldAccess.getBlockState(blockPos))) {
                    clear = false;
                    return;
                }
            }
        }
    }
    protected static boolean validStateInsidePortal(BlockState state) {
        return state.isAir() || state.isIn(BlockTags.FIRE) || state.isOf(Blocks.NETHER_PORTAL);
    }

    public boolean isCrutialObsidianPos(BlockPos pos) {
        int lowerCornerMajor = lowerCorner.getX();
        int lowerCornerMinor = lowerCorner.getZ();
        int posMajor = pos.getX();
        int posMinor = pos.getZ();
        if (!axis){
            int temp = lowerCornerMajor;
            lowerCornerMajor = lowerCornerMinor;
            lowerCornerMinor = temp;
            temp = posMajor;
            posMajor = posMinor;
            posMinor = temp;
        }
        if(posMinor != lowerCornerMinor) return false;
        // Top or bottom
        if(pos.getY() == lowerCorner.getY()-1 || pos.getY() == lowerCorner.getY() + height)
            if(lowerCornerMajor <= posMajor && posMajor < lowerCornerMajor + width)
                return true;
        //sides
        if(lowerCornerMajor - 1 == posMajor || posMajor == lowerCornerMajor + width)
            return pos.getY() >= lowerCorner.getY() && pos.getY() < lowerCorner.getY() + height;
        return false;
    }

    public boolean isClear(WorldAccess worldAccess) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                BlockPos blockPos = lowerCorner.offset(axis ? Direction.EAST : Direction.SOUTH, i).up(j);
                if (!validStateInsidePortal(worldAccess.getBlockState(blockPos))) {
                    clear = false;
                    return false;
                }
            }
        }
        clear = true;
        return true;
    }

    public boolean isIn(BlockPos pos){
        int lowerCornerMajor = lowerCorner.getX();
        int lowerCornerMinor = lowerCorner.getZ();
        int posMajor = pos.getX();
        int posMinor = pos.getZ();
        if (!axis){
            int temp = lowerCornerMajor;
            lowerCornerMajor = lowerCornerMinor;
            lowerCornerMinor = temp;
            temp = posMajor;
            posMajor = posMinor;
            posMinor = temp;
        }
        return posMinor == lowerCornerMinor && lowerCornerMajor <= posMajor && posMajor < lowerCornerMajor + width
            && pos.getY() >= lowerCorner.getY() && pos.getY() < lowerCorner.getY() + height;
    }

    public boolean tick(WorldAccess worldAccess)  {
        //TODO: find out how much to tick
        if(!clear) return false;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                BlockPos blockPos = lowerCorner.offset(axis ? Direction.EAST : Direction.SOUTH, i).up(j);
                if (canLightFire(worldAccess, blockPos)) {
                    int lavaCnt = 0;
                    for(int x=-1; x<2; x++) {
                        for(int z=-1; z<2; z++) {
                            if (worldAccess.getFluidState(blockPos.add(x, -1, z)).isIn(FluidTags.LAVA)) {
                                lavaCnt++;
                            }
                        }
                    }
                    counter += lavaCnt;
                }
            }
        }
        if (counter > 1000) {
            NetherPortalBlock.createPortalAt(worldAccess, lowerCorner);
            counter = 0;
        }
        return false;
    }

    private boolean canLightFire(WorldView world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.hasBurnableBlock(world, pos.offset(direction))) {
                return true;
            }
        }
        return false;
    }
    private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < 256 && !world.isChunkLoaded(pos) ? false : world.getBlockState(pos).getMaterial().isBurnable();
    }
}
