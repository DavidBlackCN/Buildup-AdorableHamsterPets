package net.dawson.adorablehamsterpets.mixin.client;

import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.client.feature.HamsterShoulderFeatureRenderer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Unique
    private HamsterShoulderFeatureRenderer adorablehamsterpets$shoulderLayer;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void adorablehamsterpets$addShoulderLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        RenderLayerParent<AvatarRenderState, PlayerModel> parent =
                (RenderLayerParent<AvatarRenderState, PlayerModel>) (Object) this;
        this.adorablehamsterpets$shoulderLayer = new HamsterShoulderFeatureRenderer(parent, new HamsterRenderer(context));
        ((LivingEntityRendererInvoker) this).callAddFeature(this.adorablehamsterpets$shoulderLayer);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL")
    )
    private void adorablehamsterpets$captureShoulderState(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (this.adorablehamsterpets$shoulderLayer != null && avatar instanceof AbstractClientPlayer player) {
            this.adorablehamsterpets$shoulderLayer.capture(player, state, partialTick);
        }
    }
}
