package net.dawson.adorablehamsterpets.mixin.client;

import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

/** Offsets a rolling hamster's extracted shadow pieces in the 26.1 render-state pipeline. */
@Mixin(EntityRenderer.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void adorablehamsterpets$offsetRollingShadow(Entity entity, EntityRenderState state,
                                                          float partialTick, CallbackInfo ci) {
        if (!(entity instanceof HamsterEntity hamster) || state.shadowPieces.isEmpty()) {
            return;
        }

        double offset = hamster.getRollShadowOffset(partialTick);
        if (offset <= 0.0) {
            return;
        }

        float bodyYaw = Mth.lerp(partialTick, hamster.yBodyRotO, hamster.yBodyRot);
        float yawRadians = (float) Math.toRadians(bodyYaw);
        float xOffset = (float) (Math.sin(yawRadians) * offset);
        float zOffset = (float) (-Math.cos(yawRadians) * offset);

        var shiftedPieces = new ArrayList<EntityRenderState.ShadowPiece>(state.shadowPieces.size());
        for (EntityRenderState.ShadowPiece piece : state.shadowPieces) {
            shiftedPieces.add(new EntityRenderState.ShadowPiece(
                    piece.relativeX() + xOffset,
                    piece.relativeY(),
                    piece.relativeZ() + zOffset,
                    piece.shapeBelow(),
                    piece.alpha()));
        }
        state.shadowPieces.clear();
        state.shadowPieces.addAll(shiftedPieces);
    }
}
