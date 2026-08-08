package net.dawson.adorablehamsterpets.entity.client.renderer;

import net.dawson.adorablehamsterpets.entity.custom.HamsterTreeSearcherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

public class HamsterTreeSearcherRenderer extends NoopRenderer<HamsterTreeSearcherEntity> {
    public HamsterTreeSearcherRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
