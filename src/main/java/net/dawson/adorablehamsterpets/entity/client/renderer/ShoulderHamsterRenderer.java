package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * A specialized renderer for the shoulder-mounted hamster.
 * It extends the base HamsterRenderer but overrides methods to suppress
 * sounds, particles, and other world-interactive effects that are not
 * needed for a purely cosmetic render.
 */
public class ShoulderHamsterRenderer extends HamsterRenderer {

    public ShoulderHamsterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }
}
