package vy.woodlightstd.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import vy.woodlightstd.PotentialPortalManager;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @WrapMethod(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    private boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, Operation<Boolean> original){
        BlockState original_state = getBlockState(pos);
        boolean result = original.call(pos, state, flags, maxUpdateDepth);
        if (original_state.getBlock() != state.getBlock())
            PotentialPortalManager.updatePotentialPortals((WorldAccess)(Object)this, original_state, state, pos);
        return result;
    }
}
