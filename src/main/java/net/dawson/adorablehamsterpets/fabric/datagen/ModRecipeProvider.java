package net.dawson.adorablehamsterpets.fabric.datagen;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.ModBlocks;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    // --- 1. Constructor ---
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    // --- 2. Helpers ---
    // For Hamster Bed variants
    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, path));
    }

    private void offerHamsterBedRecipe(RecipeProvider recipes, RecipeOutput exporter, Item planks, WoodVariant variant) {
        // Result is the specific item for this variant
        Item resultItem = ModItems.HAMSTER_BED_ITEMS.get(variant).get();

        recipes.shaped(RecipeCategory.DECORATIONS, resultItem)
                .pattern(" H ")
                .pattern("HHH")
                .pattern("PPP")
                .define('H', ModItems.HAMSTER_BEDDING.get())
                .define('P', planks)
                .group("hamster_bed")
                .unlockedBy("has_hamster_bedding", recipes.has(ModItems.HAMSTER_BEDDING.get()))
                .save(exporter, recipeKey("hamster_bed_" + variant.getSerializedName()));
    }

    // Helper for Smithing Upgrades
    private void offerHamsterArmorUpgrade(RecipeProvider recipes, RecipeOutput exporter, Item template, Item material, Item result) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(template),
                        recipes.tag(ModItemTagProvider.HAMSTER_ARMOR_ENCHANTABLE), // Allow any armor tier as base
                        Ingredient.of(material),
                        RecipeCategory.COMBAT,
                        result
                )
                .unlocks("has_acorn_armor", recipes.has(ModItems.HAMSTER_ARMOR_ACORN.get()))
                .unlocks("has_material", recipes.has(material))
                .save(exporter, recipeKey(RecipeProvider.getItemName(result) + "_smithing"));
    }

    // Helper for Template Duplication
    private void offerHamsterTemplateDuplication(RecipeProvider recipes, RecipeOutput exporter, Item template, Item material) {
        recipes.shaped(RecipeCategory.MISC, template, 2)
                .pattern("STS")
                .pattern("SMS")
                .pattern("SSS")
                .define('T', template)
                .define('M', material)
                .define('S', ModItems.ACORN_SHARD.get())
                .unlockedBy("has_template", recipes.has(template))
                .save(exporter, recipeKey(RecipeProvider.getItemName(template) + "_duplication"));
    }

    // --- 3. Public Methods ---
    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                ModRecipeProvider.this.generate(this, output);
            }
        };
    }

    private void generate(RecipeProvider recipes, RecipeOutput recipeExporter) {
        // --- Smelting Recipes ---
        // Smelting Green Beans to Steamed Green Beans
        recipes.oreSmelting(List.of(ModItems.GREEN_BEANS.get()), RecipeCategory.FOOD, CookingBookCategory.FOOD, ModItems.STEAMED_GREEN_BEANS.get(),
                0.35f, 200, "steamed_green_beans");

        // Smoking Green Beans to Steamed Green Beans
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.GREEN_BEANS.get()), RecipeCategory.FOOD, ModItems.STEAMED_GREEN_BEANS.get(), 0.35f, 100)
                .group("steamed_green_beans")
                .unlockedBy("has_green_beans", recipes.has(ModItems.GREEN_BEANS.get()))
                .save(recipeExporter, recipeKey("steamed_green_beans_from_smoking_green_beans"));

        // Smelting Acorns to Charcoal
        recipes.oreSmelting(List.of(ModItems.ACORN.get()), RecipeCategory.MISC, CookingBookCategory.MISC, Items.CHARCOAL,
                0.15f, 200, "charcoal");

        // --- Shaped Crafting Recipes ---
        // Hamster Bed
        offerHamsterBedRecipe(recipes, recipeExporter, Items.OAK_PLANKS, WoodVariant.OAK);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.SPRUCE_PLANKS, WoodVariant.SPRUCE);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.BIRCH_PLANKS, WoodVariant.BIRCH);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.JUNGLE_PLANKS, WoodVariant.JUNGLE);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.ACACIA_PLANKS, WoodVariant.ACACIA);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.DARK_OAK_PLANKS, WoodVariant.DARK_OAK);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.MANGROVE_PLANKS, WoodVariant.MANGROVE);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.CHERRY_PLANKS, WoodVariant.CHERRY);
        offerHamsterBedRecipe(recipes, recipeExporter, Items.BAMBOO_PLANKS, WoodVariant.BAMBOO);
        // offerHamsterBedRecipe(recipeExporter, Items.PALE_OAK_PLANKS, WoodVariant.PALE_OAK); // TODO: add pale oak when porting to 1.21.5

        // Hamster Food Mix
        recipes.shaped(RecipeCategory.FOOD, ModItems.HAMSTER_FOOD_MIX.get(), 1)
                .pattern("SSS")
                .pattern("PCP")
                .pattern("WWW")
                .define('S', ModItems.SUNFLOWER_SEEDS.get())
                .define('P', Items.PUMPKIN_SEEDS)
                .define('C', Items.CARROT)
                .define('W', Items.WHEAT_SEEDS)
                .unlockedBy("has_sunflower_seeds", recipes.has(ModItems.SUNFLOWER_SEEDS.get()))
                .save(recipeExporter, recipeKey("hamster_food_mix_from_ingredients"));

        // Hamster Bedding
        recipes.shapeless(RecipeCategory.MISC, ModItems.HAMSTER_BEDDING.get(), 2)
                .requires(Items.OAK_LEAVES)
                .requires(Items.BIRCH_LEAVES)
                .requires(Items.DEAD_BUSH)
                .requires(Items.PODZOL)
                .unlockedBy("has_oak_leaves", recipes.has(Items.OAK_LEAVES))
                .unlockedBy("has_birch_leaves", recipes.has(Items.BIRCH_LEAVES))
                .unlockedBy("has_dead_bush", recipes.has(Items.DEAD_BUSH))
                .unlockedBy("has_podzol", recipes.has(Items.PODZOL))
                .save(recipeExporter, recipeKey("hamster_bedding"));

        // Restore Blue Cheese Disc to Regular
        recipes.shaped(RecipeCategory.MISC, ModItems.MUSIC_DISC_CHEESE.get(), 1)
                .pattern("CCC")
                .pattern("CDC")
                .pattern("CCC")
                .define('C', ModItems.CHEESE.get())
                .define('D', ModItems.MUSIC_DISC_BLUE_CHEESE.get())
                .unlockedBy("has_blue_cheese_disc", recipes.has(ModItems.MUSIC_DISC_BLUE_CHEESE.get()))
                .save(recipeExporter, recipeKey("music_disc_cheese_from_blue_cheese"));

        // Restore Parmesan Disc to Regular
        recipes.shaped(RecipeCategory.MISC, ModItems.MUSIC_DISC_CHEESE.get(), 1)
                .pattern("CCC")
                .pattern("CDC")
                .pattern("CCC")
                .define('C', ModItems.CHEESE.get())
                .define('D', ModItems.MUSIC_DISC_PARMESAN.get())
                .unlockedBy("has_parmesan_disc", recipes.has(ModItems.MUSIC_DISC_PARMESAN.get()))
                .save(recipeExporter, recipeKey("music_disc_cheese_from_parmesan"));

        // --- Shapeless Crafting Recipes ---
        // Sliced Cucumber
        recipes.shapeless(RecipeCategory.FOOD, ModItems.SLICED_CUCUMBER.get(), 3)
                .requires(ModItems.CUCUMBER.get())
                .unlockedBy("has_cucumber", recipes.has(ModItems.CUCUMBER.get()))
                .save(recipeExporter, recipeKey("sliced_cucumber"));

        // Cheese
        recipes.shapeless(RecipeCategory.FOOD, ModItems.CHEESE.get(), 3)
                .requires(Items.MILK_BUCKET)
                .unlockedBy("has_milk_bucket", recipes.has(Items.MILK_BUCKET))
                .save(recipeExporter, recipeKey("cheese"));

        // Modded Sunflower to Vanilla Sunflower
        recipes.shapeless(RecipeCategory.DECORATIONS, Items.SUNFLOWER, 1)
                .requires(ModBlocks.SUNFLOWER_BLOCK.get())
                .unlockedBy("has_modded_sunflower", recipes.has(ModBlocks.SUNFLOWER_BLOCK.get()))
                .save(recipeExporter, recipeKey("vanilla_sunflower_from_modded"));

        // --- Acorn & Armor Recipes ---

        // Acorn Shard and Hat (Stonecutting)
        recipes.stonecutterResultFromBase(RecipeCategory.MISC, ModItems.ACORN_SHARD.get(), ModItems.ACORN.get(), 2);
        recipes.stonecutterResultFromBase(RecipeCategory.MISC, ModItems.ACORN_HAT.get(), ModItems.ACORN.get(), 1);

        // Acorn Armor (Shaped)
        recipes.shaped(RecipeCategory.COMBAT, ModItems.HAMSTER_ARMOR_ACORN.get(), 1)
                .pattern(" H ")
                .pattern("SSS")
                .pattern("SSS")
                .define('H', ModItems.ACORN_HAT.get())
                .define('S', ModItems.ACORN_SHARD.get())
                .unlockedBy("has_acorn_hat", recipes.has(ModItems.ACORN_HAT.get()))
                .unlockedBy("has_acorn_shard", recipes.has(ModItems.ACORN_SHARD.get()))
                .save(recipeExporter, recipeKey("hamster_armor_acorn"));

        // Smithing Upgrades
        offerHamsterArmorUpgrade(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_IRON.get(), Items.IRON_INGOT, ModItems.HAMSTER_ARMOR_IRON.get());
        offerHamsterArmorUpgrade(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_GOLD.get(), Items.GOLD_INGOT, ModItems.HAMSTER_ARMOR_GOLD.get());
        offerHamsterArmorUpgrade(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_DIAMOND.get(), Items.DIAMOND, ModItems.HAMSTER_ARMOR_DIAMOND.get());
        offerHamsterArmorUpgrade(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_NETHERITE.get(), Items.NETHERITE_INGOT, ModItems.HAMSTER_ARMOR_NETHERITE.get());

        // Template Duplication
        offerHamsterTemplateDuplication(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_IRON.get(), Items.IRON_INGOT);
        offerHamsterTemplateDuplication(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_GOLD.get(), Items.GOLD_INGOT);
        offerHamsterTemplateDuplication(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_DIAMOND.get(), Items.DIAMOND);
        offerHamsterTemplateDuplication(recipes, recipeExporter, ModItems.HAMSTER_ARMOR_TRIM_SMITHING_TEMPLATE_NETHERITE.get(), Items.NETHERITE_INGOT);

        // --- Compacting Recipes (Crates) ---
        recipes.nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.ACORN.get(), RecipeCategory.BUILDING_BLOCKS, ModItems.ACORN_CRATE.get());
        recipes.nineBlockStorageRecipes(RecipeCategory.FOOD, ModItems.CUCUMBER.get(), RecipeCategory.BUILDING_BLOCKS, ModItems.CUCUMBER_CRATE.get());
        recipes.nineBlockStorageRecipes(RecipeCategory.FOOD, ModItems.GREEN_BEANS.get(), RecipeCategory.BUILDING_BLOCKS, ModItems.GREEN_BEANS_CRATE.get());
        recipes.nineBlockStorageRecipes(RecipeCategory.FOOD, ModItems.HAMSTER_FOOD_MIX.get(), RecipeCategory.BUILDING_BLOCKS, ModItems.HAMSTER_FOOD_MIX_CRATE.get());
    }

    @Override
    public String getName() {
        return "Adorable Hamster Pets Recipes";
    }
}
