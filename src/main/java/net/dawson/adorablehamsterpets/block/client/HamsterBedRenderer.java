package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.dawson.adorablehamsterpets.block.custom.HamsterBedBlock;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class HamsterBedRenderer extends GeoBlockRenderer<HamsterBedBlockEntity, BlockEntityRenderState> {
    public HamsterBedRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new HamsterBedModel());
    }

    @Override
    public RenderType getRenderType(BlockEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    protected Direction getBlockStateDirection(HamsterBedBlockEntity block) {
        BlockState state = block.getBlockState();
        return state.hasProperty(HamsterBedBlock.ORIENTATION)
                ? state.getValue(HamsterBedBlock.ORIENTATION)
                : super.getBlockStateDirection(block);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<BlockEntityRenderState> renderPass) {
        super.adjustRenderPose(renderPass);
        if (renderPass.getOrDefaultGeckolibData(HamsterBedModel.UPSIDE_DOWN, false)) {
            renderPass.poseStack().translate(0.5, 0.5, 0.5);
            renderPass.poseStack().mulPose(Axis.XP.rotationDegrees(180));
            renderPass.poseStack().translate(-0.5, -0.5, -0.5);
        }
    }
}
