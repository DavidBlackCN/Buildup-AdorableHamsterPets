package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HamsterModel extends GeoModel<HamsterEntity> {
    public static final Identifier FALLBACK_TEXTURE = Identifier.fromNamespaceAndPath(
            AdorableHamsterPets.MOD_ID, "textures/entity/hamster/fur_base_pattern/fur_pattern.png");
    public static final DataTicket<RenderData> RENDER_DATA = DataTickets.create(
            "adorablehamsterpets_hamster_render_data", RenderData.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        RenderData data = renderState.getGeckolibData(RENDER_DATA);
        return data == null ? FALLBACK_TEXTURE : data.texture();
    }

    @Override
    public Identifier getAnimationResource(HamsterEntity animatable) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster");
    }

    public record RenderData(
            Identifier texture,
            int entityId,
            boolean baby,
            boolean noAi,
            boolean moonwalking,
            boolean leftCheekFull,
            boolean rightCheekFull,
            boolean hideRightEar,
            boolean showAcornHat,
            int flowerType,
            boolean useArmorFlowers,
            float baseScale,
            float headScale,
            float scaleY,
            float pitch,
            float groundYOffset,
            boolean performanceMode,
            boolean projectileDummy,
            Vec3 velocity,
            List<PassengerRenderData> passengers,
            String particleEffectId,
            String soundEffectId) {}

    public record PassengerRenderData(EntityRenderState state, Vec3 seatOffset, float bodyRot) {}
}
