package net.dawson.adorablehamsterpets.event;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.event.api.v2.OnUpdateServerListener;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.accessor.PlayerEntityAccessor;
import net.dawson.adorablehamsterpets.block.custom.HamsterBedBlock;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.dawson.adorablehamsterpets.command.HamsterSpawnCommandUtil;
import net.dawson.adorablehamsterpets.config.ConfigDataCache;
import net.dawson.adorablehamsterpets.entity.custom.HamsterAbstractHiddenEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterBlockHiderEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterTreeSearcherEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterPaletteManager;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.dawson.adorablehamsterpets.mixin.accessor.SlotAccessor;
import net.dawson.adorablehamsterpets.util.ParticleEffectsUtil;
import net.dawson.adorablehamsterpets.util.TreeHeistUtil;
import net.dawson.adorablehamsterpets.world.ModWorldGeneration;
import net.dawson.adorablehamsterpets.world.gen.ModEntitySpawns;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.api.PatchouliAPI;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central handler for common, cross-loader events.
 */
public class AHPCommonEvents {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Static Registration
     * ────────────────────────────────────────────────────────────────────────────*/

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(AHPCommonEvents::onLivingHurt);
        UseBlockCallback.EVENT.register((player, world, hand, hit) ->
                onRightClickBlock(player, hand, hit.getBlockPos(), hit.getDirection()));
        AttackBlockCallback.EVENT.register(AHPCommonEvents::onLeftClickBlock);

        // Catch block breaks
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide()) {
                HamsterAbstractHiddenEntity occupant = HamsterAbstractHiddenEntity.getOccupant(world, pos);
                if (occupant instanceof HamsterBlockHiderEntity hider && hider.isOwnedBy(player)) {
                    hider.finishHiding(true, player);
                    return false; // Cancel the break, "find" hamster
                }
            }
            return true;
        });

        UseItemCallback.EVENT.register(AHPCommonEvents::onRightClickItem);
        ServerTickEvents.END_SERVER_TICK.register(HamsterSpawnCommandUtil::onServerTick);

        // Trigger the genetics report on headless servers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            HamsterPaletteManager.triggerInitialReport();
        });

        // Config reload listener
        ConfigApiJava.event().onUpdateServer((OnUpdateServerListener) (id, config, player) -> {
            if (id.getNamespace().equals(AdorableHamsterPets.MOD_ID)) {
                // Reparse cached tags and rules if any configs change
                ConfigDataCache.parseConfig();
                ModEntitySpawns.parseConfig();
                ModWorldGeneration.parseConfig();
                AdorableHamsterPets.LOGGER.info("Reloaded Adorable Hamster Pets config caches on server.");
            }
        });
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Event Handlers / Callbacks
     * ────────────────────────────────────────────────────────────────────────────*/

    private static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Level world = player.level();
        BlockState state = world.getBlockState(pos);

        // --- Hide & Seek Intercept ---
        if (!world.isClientSide()) {
            HamsterAbstractHiddenEntity occupant = HamsterAbstractHiddenEntity.getOccupant(world, pos);
            if (occupant instanceof HamsterBlockHiderEntity hider && hider.isOwnedBy(player)) {
                hider.finishHiding(true, player);
                player.swing(hand, true);
                return InteractionResult.SUCCESS;
            }
            // Allow non-owners normal interaction
        }

        // --- Lectern Intercept ---
        // Intercept the read action and route it through the Patchouli API
        if (state.is(Blocks.LECTERN) && state.getValue(LecternBlock.HAS_BOOK)) {
            // Let sneaking players take book out normally
            if (!player.isShiftKeyDown()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof LecternBlockEntity lectern) {
                    ItemStack bookStack = lectern.getBook();

                    if (bookStack.is(ModItems.HAMSTER_GUIDE_BOOK.get())) {
                        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                            PatchouliAPI.get().openBookGUI(serverPlayer, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, "hamster_tips_guide_book"));
                        }
                        // Interrupt to avoid vanilla written book UI
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        // --- Hamster Bed Unlink ---
        ItemStack stack = player.getItemInHand(hand);

        // Only care about specific unlink combination: sneaking + holding repellent
        if (player.isShiftKeyDown() && ConfigDataCache.isBedAvoidanceFood(stack)) {
            if (state.getBlock() instanceof HamsterBedBlock) {
                if (!world.isClientSide()) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof HamsterBedBlockEntity bedEntity) {
                        bedEntity.unlinkHamster(player);
                    }
                }
                // Stop vanilla eating
                return InteractionResult.SUCCESS;
            }
        }

        // --- Precision Tree Heist ---
        if (ConfigDataCache.isLureItem(stack) && TreeHeistUtil.isValidHeistStartBlock(state)) {
            if (!world.isClientSide() && player instanceof PlayerEntityAccessor accessor) {
                if (accessor.hasAnyShoulderHamster()) {
                    accessor.adorablehamsterpets$startPrecisionTreeHeist(pos);
                    return InteractionResult.SUCCESS;
                }
            } else if (world.isClientSide() && ((PlayerEntityAccessor) player).hasAnyShoulderHamster()) {
                // Prevent placing item
                return InteractionResult.SUCCESS;
            }
        }

        // --- Sapling to Dead Bush Conversion ---
        if (stack.is(Items.SHEARS) && state.is(BlockTags.SAPLINGS)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_ALL);
                world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);

                if (player instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, serverPlayer, hand);
                }

                ParticleEffectsUtil.spawnParticles(
                        world,
                        Vec3.atCenterOf(pos),
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        15,
                        new Vec3(0.2, 0.2, 0.2),
                        0.05
                );
            }
            // Stop further interaction
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult onLeftClickBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction face) {

        // --- Hide & Seek Intercept ---
        if (!world.isClientSide()) {
            HamsterAbstractHiddenEntity occupant = HamsterAbstractHiddenEntity.getOccupant(world, pos);
            if (occupant instanceof HamsterBlockHiderEntity hider && hider.isOwnedBy(player)) {
                hider.finishHiding(true, player);
                player.swing(hand, true);
                return InteractionResult.SUCCESS;
            }
            // Allow non-owners to break block normally
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult onRightClickItem(Player player, Level world, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // --- Precision Tree Heist Dynamic Exit ---
        if (ConfigDataCache.isLureItem(stack)) {
            boolean updated = false;

            if (world instanceof ServerLevel serverWorld) {
                for (Entity entity : serverWorld.getAllEntities()) {
                    if (entity instanceof HamsterTreeSearcherEntity searcher && searcher.isOwnedBy(player)) {
                        searcher.setForcedExitYaw(player.getYRot());
                        updated = true;
                    }
                }
                if (updated) {
                    player.sendOverlayMessage(Component.translatable("message.adorablehamsterpets.precision_tree_heist_exit_direction_set").withStyle(ChatFormatting.AQUA));                }
            } else {
                // Client side prediction check
                for (Entity entity : world.getEntitiesOfClass(HamsterTreeSearcherEntity.class, player.getBoundingBox().inflate(64.0), e -> true)) {
                    if (((HamsterTreeSearcherEntity) entity).isOwnedBy(player)) {
                        updated = true;
                        break;
                    }
                }
            }

            if (updated) {
                // Prevent the player from eating the cheese while configuring the heist
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * An event listener that fires whenever a player opens any menu (inventory, chest, etc.).
     * It scans all unique inventories within the menu and upgrades any outdated guide books.
     *
     * @param player The player opening the menu.
     * @param menu   The menu being opened.
     */
    public static void onOpenMenu(Player player, AbstractContainerMenu menu) {
        if (player.level().isClientSide()) {
            return;
        }

        // Use a set to avoid scanning the same inventory multiple times
        Set<Container> inventories = new HashSet<>();
        for (Slot slot : menu.slots) {
            Container inv = ((SlotAccessor) slot).adorablehamsterpets$getInventory();
            if (inv != null) {
                inventories.add(inv);
            }
        }

        // Run the upgrade logic on each unique inventory found
        for (Container inv : inventories) {
            AdorableHamsterPets.replaceOldBooksInInventory(inv);
        }
    }

    /**
     * An event listener that fires just before a living entity takes damage.
     * Prevents friendly fire between pets that share the same owner — including our
     * hamsters and vanilla pets (wolves, cats, parrots, horses, etc). Works cross-loader
     * from the common source set by relying on vanilla/Yarn types and simple reflection.
     *
     * @param victim The living entity about to be hurt.
     * @param source The source of the damage.
     * @param amount The amount of damage.
     * @return {@code false} to cancel the damage, or {@code true} to allow it.
     */
    private static boolean onLivingHurt(LivingEntity victim, DamageSource source, float amount) {
        if (victim.level().isClientSide()) {
            return true;
        }

        Entity direct = source.getDirectEntity();
        Entity attacker = source.getEntity();

        HamsterEntity hamster = null;
        if (direct instanceof HamsterEntity h && h.isTame()) {
            hamster = h;
        } else if (attacker instanceof HamsterEntity h && h.isTame()) {
            hamster = h;
        }

        // --- Hamster To Pet Protection ---
        if (hamster != null) {
            LivingEntity hamsterOwner = hamster.getOwner();
            LivingEntity victimOwner = getPetOwner(victim);

            if (hamsterOwner != null && victimOwner != null) {
                if (sameOwner(hamsterOwner, victimOwner)) {
                    return false;
                }
            }
        }

        // --- Pet To Hamster Protection ---
        if (victim instanceof HamsterEntity victimHamster && victimHamster.isTame()) {
            LivingEntity victimOwner = victimHamster.getOwner();
            LivingEntity attackerOwner = (attacker instanceof LivingEntity leAttacker) ? getPetOwner(leAttacker) : null;

            if (victimOwner != null && attackerOwner != null) {
                if (sameOwner(victimOwner, attackerOwner)) {
                    return false;
                }
            }
        }

        return true;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Private Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    private static void popOutHiddenHamster(ServerLevel world, BlockPos pos, Player player) {
        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof HamsterBlockHiderEntity hider) {
                if (hider.getAnchorPos() != null && hider.getAnchorPos().equals(pos)) {
                    if (hider.isOwnedBy(player)) {
                        hider.finishHiding(true, player);
                        return;
                    }
                }
            }
        }
    }

    @Nullable
    private static LivingEntity getPetOwner(LivingEntity entity) {
        // --- A. Direct vanilla APIs ---
        // TameableEntity (wolves, cats, parrots, etc.)
        if (entity instanceof TamableAnimal tame) {
            return tame.getOwner();
        }

        // AbstractHorseEntity stores only owner's UUID; resolve into an entity
        if (entity instanceof AbstractHorse horse) {
            UUID ownerId = horse.getOwnerReference() == null ? null : horse.getOwnerReference().getUUID();
            if (ownerId != null) {
                return lookupLivingByUuid(entity.level(), ownerId);
            }
        }

        // Some entities (esp. projectiles/custom) may implement Ownable marker that returns an Entity
        if (entity instanceof TraceableEntity ownable) {
            Entity e = ownable.getOwner();
            return (e instanceof LivingEntity le) ? le : null;
        }

        // Reflection fallback for common mod patterns
        try {
            Method m = entity.getClass().getMethod("getOwner");
            Object ret = m.invoke(entity);
            if (ret instanceof LivingEntity le) return le;
            if (ret instanceof Entity e) return (e instanceof LivingEntity le) ? le : null;
        } catch (Throwable ignored) {
        }

        UUID id = tryGetUuid(entity, "getOwnerUuid");
        if (id == null) id = tryGetUuid(entity, "getOwnerUUID");
        if (id != null) {
            return lookupLivingByUuid(entity.level(), id);
        }

        return null;
    }

    @Nullable
    private static UUID tryGetUuid(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object ret = m.invoke(target);
            return (ret instanceof UUID u) ? u : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private static LivingEntity lookupLivingByUuid(Level world, UUID id) {
        if (!(world instanceof ServerLevel server)) return null;
        Entity player = server.getPlayerByUUID(id);
        if (player instanceof LivingEntity le) return le;
        Entity any = server.getEntity(id);
        return (any instanceof LivingEntity le) ? le : null;
    }

    private static boolean sameOwner(LivingEntity a, LivingEntity b) {
        return a == b || a.getUUID().equals(b.getUUID());
    }
}
