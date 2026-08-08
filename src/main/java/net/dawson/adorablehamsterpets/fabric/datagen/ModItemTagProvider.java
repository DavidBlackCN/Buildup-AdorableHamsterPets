package net.dawson.adorablehamsterpets.fabric.datagen;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    // --- Custom Tags ---
    public static final TagKey<Item> HAMSTER_ARMOR_ENCHANTABLE = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/hamster_armor")
    );

    // Frost Walker (Vanilla Foot Armor + Hamster Armor)
    public static final TagKey<Item> FROST_WALKER_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/frost_walker_supported")
    );

    // Fire Protection (Vanilla Armor + Hamster Armor)
    public static final TagKey<Item> FIRE_PROTECTION_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/fire_protection_supported")
    );

    // Soul Speed (Vanilla Foot Armor + Hamster Armor)
    public static final TagKey<Item> SOUL_SPEED_SUPPORTED = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "enchantable/soul_speed_supported")
    );

    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        // 1. Define Hamster Armor Group
        valueLookupBuilder(HAMSTER_ARMOR_ENCHANTABLE)
                .add(ModItems.HAMSTER_ARMOR_ACORN.get())
                .add(ModItems.HAMSTER_ARMOR_IRON.get())
                .add(ModItems.HAMSTER_ARMOR_GOLD.get())
                .add(ModItems.HAMSTER_ARMOR_DIAMOND.get())
                .add(ModItems.HAMSTER_ARMOR_NETHERITE.get());

        // 2. Add to Vanilla Durability (Enables Unbreaking/Mending)
        builder(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 3. Frost Walker Wrapper
        builder(FROST_WALKER_SUPPORTED)
                .addOptionalTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 4. Fire Protection Wrapper
        builder(FIRE_PROTECTION_SUPPORTED)
                .addOptionalTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 5. Soul Speed Wrapper
        builder(SOUL_SPEED_SUPPORTED)
                .addOptionalTag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);

        // 6. Lectern Books Wrapper
        valueLookupBuilder(ItemTags.LECTERN_BOOKS)
                .add(ModItems.HAMSTER_GUIDE_BOOK.get());

        // 7. Vanilla Trimmable Armor Wrapper
        builder(ItemTags.TRIMMABLE_ARMOR)
                .addTag(HAMSTER_ARMOR_ENCHANTABLE);
    }
}
