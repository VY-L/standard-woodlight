package vy.woodlightstd;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Iterator;

public class PotentialPortalManager {
    public static long totalTime = 0;
    public static int callCount = 0;
    public static void tickCounters(WorldAccess worldAccess) {
        for(PotentialPortal portal: StandardWoodlight.POTENTIAL_PORTALS)
            portal.tick(worldAccess);
//            if (portal.tick(worldAccess))
//                worldAccess.setBlockState(portal.lowerCorner.up(), Blocks.FIRE.getDefaultState(), 0b00000011);
    }
    public static void updatePotentialPortals(WorldAccess worldAccess, BlockState state1, BlockState state2, BlockPos pos) {
        long time = System.nanoTime();
        int portalCount = StandardWoodlight.POTENTIAL_PORTALS.size();
        // Remove Portals
        if (worldAccess.isClient())
            return;
        if(state1.isOf(Blocks.OBSIDIAN) && !state2.isOf(Blocks.OBSIDIAN))
        {
            Iterator<PotentialPortal> it = StandardWoodlight.POTENTIAL_PORTALS.iterator();
            while (it.hasNext()) {
                PotentialPortal portal = it.next();

                if (portal.isCrutialObsidianPos(pos)) {
                    it.remove();
                }
            }
        }
        // TODO: check for added portals
        if (state2.isOf(Blocks.OBSIDIAN) && !state1.isOf(Blocks.OBSIDIAN)) {
            System.out.println("Obsidian is placed");
//            System.out.println(" ");
//            System.out.println("Obsidian placed at " + pos + ", checking for potential portals...");
            // FIXME: I am using arbitrary "22", not sure specifically what to set to
            // region side obsidian

            // check vertical column
            BlockPos colTop = pos.offset(Direction.UP, 22);
            BlockPos colBottom = pos.offset(Direction.DOWN, 22);
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.UP, i)).isOf(Blocks.OBSIDIAN)) {
                    colTop = pos.offset(Direction.UP, i); // note that this position is already not obsidian
                    break;
                }
            }
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.DOWN, i)).isOf(Blocks.OBSIDIAN)) {
                    colBottom = pos.offset(Direction.DOWN, i); // note that this position is already not obsidian
                    break;
                }
            }
//            System.out.println("Vertical obsidian column from " + colBottom + " to " + colTop);
            if (colTop.getY() - colBottom.getY() > 3) {
                System.out.println("Found vertical obsidian column of height " + (colTop.getY() - colBottom.getY() + 1));
                for (Direction direction : Direction.Type.HORIZONTAL) {
//                    System.out.println("Checking direction " + direction);
                    // enumerate possible bottom & top positions
                    BlockPos top = colTop;
                    BlockPos bottom = colBottom;
                    while (top.getY() - bottom.getY() > 3 && bottom.getY() < pos.getY()) {
                        while (top.getY() - bottom.getY() > 3 && top.getY() > pos.getY()) {
//                            System.out.println("Checking potential portal with bottom at " + bottom.getY() + " and top at " + top.getY());
                            //check: if index>2, check if full column; if top and bottom aren't obsidian, break
                            for(int i = 1; i<22; i++) {// i=offset=width+1
                                if (i >= 3) {
//                                    System.out.println("Checking for portal with width " + (i-1));
                                    BlockPos farColPos = bottom.offset(direction, i).up();
                                    boolean flag = true;
                                    while (farColPos.getY()<top.getY())
                                    {
                                        if (!worldAccess.getBlockState(farColPos).isOf(Blocks.OBSIDIAN)) {
                                            flag = false;
                                            break;
                                        }
                                        farColPos = farColPos.up();
                                    }
                                    if(flag) {
                                        // TODO: create potential portals
                                        boolean axis = (direction==Direction.EAST || direction==Direction.WEST);
                                        // figure out the specific dimensions
                                        int x, z;
                                        BlockPos anchor = bottom.up();
                                        BlockPos lowerCorner = null;

                                        switch(direction) {
                                            case EAST:
                                                lowerCorner = anchor.offset(Direction.EAST, 1);
                                                break;
                                            case SOUTH:
                                                lowerCorner = anchor.offset(Direction.SOUTH, 1);
                                                break;
                                            case WEST:
                                                lowerCorner = anchor.offset(Direction.WEST, i-1);
                                                break;
                                            case NORTH:
                                                lowerCorner = anchor.offset(Direction.NORTH, i-1);
                                                break;
                                        }
                                        assert lowerCorner != null;

                                        PotentialPortal portal = new PotentialPortal(axis, lowerCorner, top.getY() - bottom.getY() - 1, i-1, worldAccess);
                                        StandardWoodlight.POTENTIAL_PORTALS.add(portal);
                                        System.out.println("New potential portal: axis=" + axis + ", lowerCorner at " + lowerCorner + ", dimensions: " + (i-1) + "x" + (top.getY() - bottom.getY() - 1));
                                    }
                                }

                                if (!worldAccess.getBlockState(top.offset(direction, i)).isOf(Blocks.OBSIDIAN)) {
                                    break;
                                }
                                if (!worldAccess.getBlockState(bottom.offset(direction, i)).isOf(Blocks.OBSIDIAN)) {
                                    break;
                                }

                            }
                            top = top.down();
                        }
                        top = colTop;
                        bottom = bottom.up();
                    }
                }
            }
            // endregion

            // region east west
            BlockPos eastCorner = pos.offset(Direction.EAST, 22);
            BlockPos westCorner = pos.offset(Direction.WEST, 22);
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.EAST, i)).isOf(Blocks.OBSIDIAN)) {
                    eastCorner = pos.offset(Direction.EAST, i); // note that this position is already not obsidian
                    break;
                }
            }
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.WEST, i)).isOf(Blocks.OBSIDIAN)) {
                    westCorner = pos.offset(Direction.WEST, i); // note that this position is already not obsidian
                    break;
                }
            }
            // upwards
            if (eastCorner.getX() - westCorner.getX() > 2) {
                BlockPos east = eastCorner;
                BlockPos west = westCorner;
                while(east.getX() - west.getX() > 2 && west.getX() < pos.getX()) {
                    while(east.getX() - west.getX() > 2 && east.getX() > pos.getX()) {
                        for(int i = 1; i<22; i++) { //i = height+1
                            if (i >= 4) {
                                BlockPos farColPos = west.offset(Direction.UP, i).offset(Direction.EAST, 1);
                                boolean flag = true;
                                while (farColPos.getX() < east.getX()) {
                                    if (!worldAccess.getBlockState(farColPos).isOf(Blocks.OBSIDIAN)) {
                                        flag = false;
                                        break;
                                    }
                                    farColPos = farColPos.offset(Direction.EAST, 1);
                                }
                                if (flag) {
                                    // TODO: create potential portals
                                    BlockPos lowerCorner = null;
                                    PotentialPortal portal = new PotentialPortal(true, west.offset(Direction.EAST).offset(Direction.UP), i - 1, east.getX() - west.getX() - 1, worldAccess);
                                    StandardWoodlight.POTENTIAL_PORTALS.add(portal);
                                    System.out.println("New potential portal: axis=EW, lowerCorner at " + west.offset(Direction.EAST).offset(Direction.UP) + ", dimensions: " + (east.getX() - west.getX() - 1) + "x" + (i - 1));
                                }
                            }
                            if (!worldAccess.getBlockState(east.offset(Direction.UP, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                            if (!worldAccess.getBlockState(west.offset(Direction.UP, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                        }
                        east = east.offset(Direction.WEST, 1);
                    }
                    east = eastCorner;
                    west = west.offset(Direction.EAST, 1);
                }

            }
            // downwards
            if (eastCorner.getX() - westCorner.getX() > 2) {
                BlockPos east = eastCorner;
                BlockPos west = westCorner;
                while(east.getX() - west.getX() > 2 && west.getX() < pos.getX()) {
                    while(east.getX() - west.getX() > 2 && east.getX() > pos.getX()) {
                        for(int i = 1; i<22; i++) { //i = height+1
                            if (i >= 4) {
                                BlockPos farColPos = west.offset(Direction.DOWN, i).offset(Direction.EAST, 1);
                                boolean flag = true;
                                while (farColPos.getX() < east.getX()) {
                                    if (!worldAccess.getBlockState(farColPos).isOf(Blocks.OBSIDIAN)) {
                                        flag = false;
                                        break;
                                    }
                                    farColPos = farColPos.offset(Direction.EAST, 1);
                                }
                                if (flag) {
                                    // TODO: create potential portals
                                    BlockPos lowerCorner = null;
                                    PotentialPortal portal = new PotentialPortal(true, west.offset(Direction.EAST).offset(Direction.DOWN, i-1), i - 1, east.getX() - west.getX() - 1, worldAccess);
                                    StandardWoodlight.POTENTIAL_PORTALS.add(portal);
                                    System.out.println("New potential portal: axis=EW, lowerCorner at " + west.offset(Direction.EAST).offset(Direction.DOWN, i-1) + ", dimensions: " + (east.getX() - west.getX() - 1) + "x" + (i - 1));
                                }
                            }
                            if (!worldAccess.getBlockState(east.offset(Direction.DOWN, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                            if (!worldAccess.getBlockState(west.offset(Direction.DOWN, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                        }
                        east = east.offset(Direction.WEST, 1);
                    }
                    east = eastCorner;
                    west = west.offset(Direction.EAST, 1);
                }

            }
            // endregion
            // region north south
            BlockPos southCorner = pos.offset(Direction.SOUTH, 22);
            BlockPos northCorner = pos.offset(Direction.NORTH, 22);
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.SOUTH, i)).isOf(Blocks.OBSIDIAN)) {
                    southCorner = pos.offset(Direction.SOUTH, i); // note that this position is already not obsidian
                    break;
                }
            }
            for(int i = 0; i < 22; i++) {
                if (!worldAccess.getBlockState(pos.offset(Direction.NORTH, i)).isOf(Blocks.OBSIDIAN)) {
                    northCorner = pos.offset(Direction.NORTH, i); // note that this position is already not obsidian
                    break;
                }
            }
            // upwards
            if (southCorner.getZ() - northCorner.getZ() > 2) {
                BlockPos south = southCorner;
                BlockPos north = northCorner;
                while(south.getZ() - north.getZ() > 2 && north.getZ() < pos.getZ()) {
                    while(south.getZ() - north.getZ() > 2 && south.getZ() > pos.getZ()) {
                        for(int i = 1; i<22; i++) { //i = height+1
                            if (i >= 4) {
                                BlockPos farColPos = north.offset(Direction.UP, i).offset(Direction.SOUTH, 1);
                                boolean flag = true;
                                while (farColPos.getZ() < south.getZ()) {
                                    if (!worldAccess.getBlockState(farColPos).isOf(Blocks.OBSIDIAN)) {
                                        flag = false;
                                        break;
                                    }
                                    farColPos = farColPos.offset(Direction.SOUTH, 1);
                                }
                                if (flag) {
                                    // TODO: create potential portals
                                    BlockPos lowerCorner = null;
                                    PotentialPortal portal = new PotentialPortal(false, north.offset(Direction.SOUTH).offset(Direction.UP), i - 1, south.getZ() - north.getZ() - 1, worldAccess);
                                    StandardWoodlight.POTENTIAL_PORTALS.add(portal);
                                    System.out.println("New potential portal: axis=NS, lowerCorner at " + north.offset(Direction.SOUTH).offset(Direction.UP) + ", dimensions: " + (south.getZ() - north.getZ() - 1) + "x" + (i - 1));
                                }
                            }
                            if (!worldAccess.getBlockState(south.offset(Direction.UP, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                            if (!worldAccess.getBlockState(north.offset(Direction.UP, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                        }
                        south = south.offset(Direction.NORTH, 1);
                    }
                    south = southCorner;
                    north = north.offset(Direction.SOUTH, 1);
                }

            }
            // downwards
            if (southCorner.getZ() - northCorner.getZ() > 2) {
                BlockPos south = southCorner;
                BlockPos north = northCorner;
                while(south.getZ() - north.getZ() > 2 && north.getZ() < pos.getZ()) {
                    while(south.getZ() - north.getZ() > 2 && south.getZ() > pos.getZ()) {
                        for(int i = 1; i<22; i++) { //i = height+1
                            if (i >= 4) {
                                BlockPos farColPos = north.offset(Direction.DOWN, i).offset(Direction.SOUTH, 1);
                                boolean flag = true;
                                while (farColPos.getZ() < south.getZ()) {
                                    if (!worldAccess.getBlockState(farColPos).isOf(Blocks.OBSIDIAN)) {
                                        flag = false;
                                        break;
                                    }
                                    farColPos = farColPos.offset(Direction.SOUTH, 1);
                                }
                                if (flag) {
                                    // TODO: create potential portals
                                    BlockPos lowerCorner = null;
                                    PotentialPortal portal = new PotentialPortal(false, north.offset(Direction.SOUTH).offset(Direction.DOWN, i-1), i - 1, south.getZ() - north.getZ() - 1, worldAccess);
                                    StandardWoodlight.POTENTIAL_PORTALS.add(portal);
                                    System.out.println("New potential portal: axis=NS, lowerCorner at " + north.offset(Direction.SOUTH).offset(Direction.DOWN, i-1) + ", dimensions: " + (south.getZ() - north.getZ() - 1) + "x" + (i - 1));
                                }
                            }
                            if (!worldAccess.getBlockState(south.offset(Direction.DOWN, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                            if (!worldAccess.getBlockState(north.offset(Direction.DOWN, i)).isOf(Blocks.OBSIDIAN)) {
                                break;
                            }
                        }
                        south = south.offset(Direction.NORTH, 1);
                    }
                    south = southCorner;
                    north = north.offset(Direction.SOUTH, 1);
                }

            }
            // endregion
        }
        // blocked portals, note that counter should not reset
        if (state1.isOf(Blocks.AIR) && !state2.isOf(Blocks.AIR))
            for(final PotentialPortal portal : StandardWoodlight.POTENTIAL_PORTALS)
                if (portal.isIn(pos)) portal.clear = false;

        // clear portals
        if (!state1.isOf(Blocks.AIR) && state2.isOf(Blocks.AIR))
            for(final PotentialPortal portal : StandardWoodlight.POTENTIAL_PORTALS)
                if (portal.isIn(pos)) portal.isClear(worldAccess);
        int newCount = StandardWoodlight.POTENTIAL_PORTALS.size();
        int delta = newCount - portalCount;
        if (newCount != portalCount)
            System.out.println("Potential portal count is now " + newCount + " (" + (newCount>0?"+":"") + delta + ")");
        callCount++;
        totalTime += (System.nanoTime() - time);
        System.out.println("Currently used " + totalTime/1000000 + "ms to run" + callCount + "calls");
    }
}
