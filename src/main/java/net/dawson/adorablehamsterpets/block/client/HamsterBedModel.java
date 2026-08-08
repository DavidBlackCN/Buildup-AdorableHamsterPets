package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.custom.HamsterBedBlock;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.minecraft.resources.Identifier;

public class HamsterBedModel extends GeoModel<HamsterBedBlockEntity> {
    public static final DataTicket<WoodVariant> WOOD_VARIANT =
            DataTickets.create("adorablehamsterpets_hamster_bed_wood", WoodVariant.class);
    public static final DataTicket<Boolean> UPSIDE_DOWN =
            DataTickets.create("adorablehamsterpets_hamster_bed_upside_down", Boolean.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster_bed");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        WoodVariant variant = renderState.getOrDefaultGeckolibData(WOOD_VARIANT, WoodVariant.OAK);
        return Identifier.fromNamespaceAndPath(
                AdorableHamsterPets.MOD_ID,
                "textures/block/hamster_bed_" + variant.getSerializedName() + ".png");
    }

    @Override
    public Identifier getAnimationResource(HamsterBedBlockEntity animatable) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster_bed");
    }

    @Override
    public void addAdditionalStateData(HamsterBedBlockEntity animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(WOOD_VARIANT, animatable.getBlockState().getValue(HamsterBedBlock.WOOD_VARIANT));
        renderState.addGeckolibData(UPSIDE_DOWN, animatable.getBlockState().getValue(HamsterBedBlock.UPSIDE_DOWN));
    }
}
