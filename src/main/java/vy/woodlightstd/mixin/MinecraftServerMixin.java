package vy.woodlightstd.mixin;

import com.google.common.collect.Maps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vy.woodlightstd.PotentialPortalManager;

import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    private final Map<RegistryKey<World>, ServerWorld> worlds = Maps.<RegistryKey<World>, ServerWorld>newLinkedHashMap();

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        WorldAccess worldAccess = worlds.get(World.OVERWORLD);
        PotentialPortalManager.tickCounters(worldAccess);
    }
}
