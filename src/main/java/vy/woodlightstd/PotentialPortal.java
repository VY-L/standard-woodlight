package vy.woodlightstd;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
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

        if (counter > 10000)
            NetherPortalBlock.createPortalAt(worldAccess, lowerCorner);
        return false;
    }
}
