package net.dawson.adorablehamsterpets.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Mixin to cancel vanilla passenger rendering when the passenger is riding a hamster.
 * Prevents double-rendering, allowing custom bone-locked rendering in HamsterRenderer to work properly.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    private static final Map<LivingEntityRenderState, Boolean> ADORABLEHAMSTERPETS$HAMSTER_PASSENGERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void adorablehamsterpets$markHamsterPassenger(LivingEntity entity, LivingEntityRenderState state,
                                                          float partialTick, CallbackInfo ci) {
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof HamsterEntity) {
            ADORABLEHAMSTERPETS$HAMSTER_PASSENGERS.put(state, Boolean.TRUE);
        } else {
            ADORABLEHAMSTERPETS$HAMSTER_PASSENGERS.remove(state);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void adorablehamsterpets$cancelDuplicateRender(LivingEntityRenderState state, PoseStack poseStack,
                                                            SubmitNodeCollector submitNodes,
                                                            CameraRenderState cameraState, CallbackInfo ci) {
        if (ADORABLEHAMSTERPETS$HAMSTER_PASSENGERS.containsKey(state)
                && !HamsterRenderer.IS_RENDERING_PASSENGER.get()) {
            ci.cancel();
        }
    }
}
