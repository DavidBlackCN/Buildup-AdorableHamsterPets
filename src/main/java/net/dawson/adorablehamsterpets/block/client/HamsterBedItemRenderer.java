package net.dawson.adorablehamsterpets.block.client;

import com.geckolib.renderer.GeoItemRenderer;
import net.dawson.adorablehamsterpets.item.client.HamsterBedItemModel;
import net.dawson.adorablehamsterpets.item.custom.HamsterBedItem;

public class HamsterBedItemRenderer extends GeoItemRenderer<HamsterBedItem> {
    public HamsterBedItemRenderer() {
        super(new HamsterBedItemModel());
    }
}
