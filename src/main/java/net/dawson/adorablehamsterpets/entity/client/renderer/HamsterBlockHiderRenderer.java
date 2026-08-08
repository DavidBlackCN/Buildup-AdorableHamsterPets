package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.entity.custom.HamsterBlockHiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

public class HamsterBlockHiderRenderer extends NoopRenderer<HamsterBlockHiderEntity> {
    public HamsterBlockHiderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
