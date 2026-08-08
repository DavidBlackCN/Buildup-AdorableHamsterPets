package net.dawson.adorablehamsterpets.mixin.server;

import net.dawson.adorablehamsterpets.event.AHPCommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "openMenu", at = @At("RETURN"))
    private void adorablehamsterpets$onOpenMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        if (cir.getReturnValue().isPresent()) {
            ServerPlayer player = (ServerPlayer) (Object) this;
            AHPCommonEvents.onOpenMenu(player, player.containerMenu);
        }
    }
}
