package net.dawson.adorablehamsterpets.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.client.render.BlockJiggleManager;
import net.dawson.adorablehamsterpets.client.render.BlockJiggleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    @Unique
    private static final ThreadLocal<Boolean> adorablehamsterpets$didPushJiggle =
            ThreadLocal.withInitial(() -> false);

    @Inject(method = "submit", at = @At("HEAD"))
    private void adorablehamsterpets$pushJiggle(BlockEntityRenderState state, PoseStack poseStack,
                                                 SubmitNodeCollector submitNodes, CameraRenderState cameraState,
                                                 CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && BlockJiggleManager.INSTANCE.hasJiggle(state.blockPos.asLong())) {
            poseStack.pushPose();
            adorablehamsterpets$didPushJiggle.set(true);
            BlockJiggleRenderer.applyJiggleTransform(
                    poseStack,
                    state.blockPos,
                    minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(minecraft.isPaused()),
                    minecraft.level.getGameTime());
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void adorablehamsterpets$popJiggle(BlockEntityRenderState state, PoseStack poseStack,
                                                 SubmitNodeCollector submitNodes, CameraRenderState cameraState,
                                                 CallbackInfo ci) {
        if (adorablehamsterpets$didPushJiggle.get()) {
            poseStack.popPose();
            adorablehamsterpets$didPushJiggle.set(false);
        }
    }
}
