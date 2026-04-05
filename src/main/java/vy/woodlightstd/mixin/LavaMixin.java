package vy.woodlightstd.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(LavaFluid.class)
public class LavaMixin {
    // removes lava random tick behavior
    @WrapMethod(method="onRandomTick")
    private void onRandomTick(World world, BlockPos pos, FluidState state, Random random, Operation<Void> original) {
    }
}
