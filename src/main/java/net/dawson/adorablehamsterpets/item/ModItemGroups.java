package net.dawson.adorablehamsterpets.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.dawson.adorablehamsterpets.registry.DeferredRegister;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;

public class ModItemGroups {

    public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(AdorableHamsterPets.MOD_ID, BuiltInRegistries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> ADORABLE_HAMSTER_PETS_GROUP = ITEM_GROUPS.register(
            "adorable_hamster_pets",
            () -> FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.adorablehamsterpets.main"))
                    .icon(() -> new ItemStack(ModItems.HAMSTER_SPAWN_EGG.get()))
                    .displayItems((featureSet, creativeOutput) -> {
                        ItemCollector output = creativeOutput::accept;
                        output.add(ModItems.HAMSTER_GUIDE_BOOK.get());
                        output.add(ModItems.MUSIC_DISC_CHEESE.get());
                        output.add(ModItems.MUSIC_DISC_BLUE_CHEESE.get());
                        output.add(ModItems.MUSIC_DISC_PARMESAN.get());
                        output.add(ModItems.CHEESE.get());
                        output.add(ModItems.HAMSTER_FOOD_MIX.get());
                        output.add(ModItems.CUCUMBER.get());
                        output.add(ModItems.CUCUMBER_SEEDS.get());
                        output.add(ModItems.SLICED_CUCUMBER.get());
                        output.add(ModItems.GREEN_BEANS.get());
                        output.add(ModItems.GREEN_BEAN_SEEDS.get());
                        output.add(ModItems.STEAMED_GREEN_BEANS.get());
                        output.add(ModItems.SUNFLOWER_SEEDS.get());
                        output.add(ModItems.HAMSTER_SPAWN_EGG.get());
                        output.add(ModItems.SUNFLOWER_BLOCK_ITEM.get());
                        output.add(ModItems.WILD_GREEN_BEAN_BUSH_ITEM.get());
                        output.add(ModItems.WILD_CUCUMBER_BUSH_ITEM.get());
                        output.add(ModItems.HAMSTER_BEDDING.get());
                        output.add(ModItems.ACORN.get());
                        output.add(ModItems.ACORN_SHARD.get());
                        output.add(ModItems.ACORN_HAT.get());
                        output.add(ModItems.HAMSTER_ARMOR_ACORN.get());
                        output.add(ModItems.HAMSTER_ARMOR_IRON.get());
                        output.add(ModItems.HAMSTER_ARMOR_GOLD.get());
                        output.add(ModItems.HAMSTER_ARMOR_DIAMOND.get());
                        output.add(ModItems.HAMSTER_ARMOR_NETHERITE.get());
                        output.add(ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_IRON.get());
                        output.add(ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_GOLD.get());
                        output.add(ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_DIAMOND.get());
                        output.add(ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_NETHERITE.get());
                        ModItems.HAMSTER_BED_ITEMS.values().forEach(supplier -> output.add(supplier.get()));
                        output.add(ModItems.ACORN_CRATE.get());
                        output.add(ModItems.CUCUMBER_CRATE.get());
                        output.add(ModItems.GREEN_BEANS_CRATE.get());
                        output.add(ModItems.HAMSTER_FOOD_MIX_CRATE.get());
                    }).build()
    );

    public static void register() {
        ITEM_GROUPS.register();
    }

    @FunctionalInterface
    private interface ItemCollector {
        void add(ItemLike item);
    }
}
