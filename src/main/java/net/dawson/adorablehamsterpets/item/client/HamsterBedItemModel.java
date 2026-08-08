package net.dawson.adorablehamsterpets.item.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.item.custom.HamsterBedItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class HamsterBedItemModel extends GeoModel<HamsterBedItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster_bed");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        Item item = renderState.getGeckolibData(GeoItemRenderer.CURRENT_ITEM);
        WoodVariant variant = item instanceof HamsterBedItem bed ? bed.getVariant() : WoodVariant.OAK;
        return Identifier.fromNamespaceAndPath(
                AdorableHamsterPets.MOD_ID,
                "textures/block/hamster_bed_" + variant.getSerializedName() + ".png");
    }

    @Override
    public Identifier getAnimationResource(HamsterBedItem animatable) {
        return Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "anim_hamster_bed");
    }
}
