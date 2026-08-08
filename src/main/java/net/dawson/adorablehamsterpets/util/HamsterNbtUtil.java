package net.dawson.adorablehamsterpets.util;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.List;

/**
 * Isolates NBT serialization and deserialization logic for Hamsters.
 */
public final class HamsterNbtUtil {

    private HamsterNbtUtil() {}

    /* ──────────────────────────────────────────────────────────────────────────────
     *                        Core Data Serialization
     * ────────────────────────────────────────────────────────────────────────────*/

    public static void writeCustomData(HamsterEntity hamster, ValueOutput output) {
        // --- 1. Core Data & Flags ---
        output.store("HamsterGenome", CompoundTag.CODEC, hamster.getGenome().saveToNbt());
        output.putLong("TotalAgeTicks", hamster.totalAgeTicks);
        output.putInt("TimesBred", hamster.timesBred);
        // Write flags as individual booleans for backwards compat
        if (hamster.isTame()) {
            output.putBoolean("Sitting", hamster.getHamsterFlag(HamsterEntity.SITTING_FLAG));
            output.putBoolean("IsSleeping", hamster.getHamsterFlag(HamsterEntity.SLEEPING_FLAG));
        } else {
            output.putBoolean("IsSleeping", false);
        }
        output.putBoolean("KnockedOut", hamster.getHamsterFlag(HamsterEntity.KNOCKED_OUT_FLAG));
        output.putBoolean("CheekPouchUnlocked", hamster.getHamsterFlag(HamsterEntity.CHEEK_POUCH_UNLOCKED_FLAG));
        output.putLong("ThrowCooldownEnd", hamster.throwCooldownEndTick);
        output.putLong("GreenBeanBuffDuration", hamster.getEntityData().get(HamsterEntity.GREEN_BEAN_BUFF_DURATION));
        output.putInt("AutoEatCooldown", hamster.getAutoEatCooldownTicks());
        output.putInt("EjectionCheckCooldown", hamster.getEjectionCheckCooldown());
        output.putInt("FlowerPosition", hamster.getEntityData().get(HamsterEntity.FLOWER_POS));
        output.putInt("AnimationPersonalityId", hamster.getEntityData().get(HamsterEntity.ANIMATION_PERSONALITY_ID));
        output.putBoolean("isGeneticsVisualizerMember", hamster.isGeneticsVisualizerMember());
        output.putInt("AggressionState", hamster.getAggressionState().ordinal());

        // --- 2. Parent Following ---
        if (hamster.getParentUuid() != null) {
            output.store("ParentUuid", UUIDUtil.CODEC, hamster.getParentUuid());
        }

        // --- 3. Sleep State ---
        output.putInt("DozingPhase", hamster.getDozingPhase().ordinal());
        output.putString("CurrentDeepSleepAnimId", hamster.getEntityData().get(HamsterEntity.CURRENT_DEEP_SLEEP_ANIM_ID));
        output.putInt("QuiescentSitTimer", hamster.getQuiescentSitTimer());
        output.putInt("DriftingOffTimer", hamster.getDriftingOffTimer());
        output.putInt("SettleSleepCooldown", hamster.getSettleSleepCooldown());

        // --- 4. Inventory ---
        ContainerHelper.saveAllItems(output.child("Inventory"), hamster.getItems());

        // --- 5. Ore Seeking ---
        output.putBoolean("IsPrimedToSeekDiamonds", hamster.isPrimedToSeekDiamonds);
        output.putLong("FoundOreCooldownEndTick", hamster.foundOreCooldownEndTick);
        if (hamster.currentOreTarget != null) {
            output.putInt("OreTargetX", hamster.currentOreTarget.getX());
            output.putInt("OreTargetY", hamster.currentOreTarget.getY());
            output.putInt("OreTargetZ", hamster.currentOreTarget.getZ());
        }
        output.putBoolean("IsCelebratingDiamond", hamster.getHamsterFlag(HamsterEntity.CELEBRATING_DIAMOND_FLAG));

        // --- 6. Interaction & Mini-Game ---
        output.putBoolean("IsSulking", hamster.getHamsterFlag(HamsterEntity.SULKING_FLAG));
        output.putInt("SulkTimer", hamster.sulkTimer);
        output.putLong("TagGameCooldownEnd", hamster.tagGameCooldownEndTick);
        output.putLong("StealingCooldownEnd", hamster.stealingCooldownEndTick);
        output.putLong("CropSnackCooldownEnd", hamster.cropSnackCooldownEndTick);
        output.putLong("HideAndSeekCooldownEnd", hamster.hideAndSeekCooldownEndTick);
        if (hamster.getGenericInteractionTimer() > 0) {
            output.putInt("GenericInteractionTimer", hamster.getGenericInteractionTimer());
        }
        if (hamster.isHoldingMouthItem()) {
            output.putBoolean("IsHoldingMouthItem", true);
            if (!hamster.getMouthItemStack().isEmpty()) {
                output.store("MouthItemStack", ItemStack.CODEC, hamster.getMouthItemStack());
            }
        }

        // --- 7. Wander Mode ---
        output.putBoolean("IsWanderModeActive", hamster.isWanderModeActive());
        hamster.getLinkedBedPos().ifPresent(globalPos -> output.store("LinkedBedPos", GlobalPos.CODEC, globalPos));
        output.putBoolean("BypassNextSleepDelay", hamster.shouldBypassNextSleepDelay());
        output.putBoolean("StuckSearchingForBed", hamster.isStuckSearchingForBed());
        output.putBoolean("IsRescueSleeping", hamster.isRescueSleeping());
    }

    public static void readCustomData(HamsterEntity hamster, ValueInput input) {
        // --- 1. Read Core Data ---
        hamster.setLoadingNbt(true); // Suppress sounds
        hamster.totalAgeTicks = input.getLongOr("TotalAgeTicks", 0L);
        hamster.timesBred = input.getIntOr("TimesBred", 0);
        // Migrate legacy variant IDs to v3.6.0's Genome structure
        if (input.contains("HamsterGenome")) {
            hamster.setGenome(HamsterGenome.readFromNbt(input.read("HamsterGenome", CompoundTag.CODEC).orElseGet(CompoundTag::new)));
        } else if (input.contains("HamsterVariant")) {
            // Catch old integer IDs from pre 3.6.0
            int legacyId = input.getIntOr("HamsterVariant", 0);
            hamster.setGenome(HamsterGeneticsUtil.getGenomeForLegacyId(legacyId));
        } else {
            hamster.setGenome(HamsterGenome.createDefault());
        }
        // Backwards compat: read individual booleans & set flags
        boolean wasSittingNbt = hamster.isTame() && input.getBooleanOr("Sitting", false);
        hamster.setSitting(wasSittingNbt, true); // This will correctly set the SITTING_FLAG
        hamster.setHamsterFlag(HamsterEntity.KNOCKED_OUT_FLAG, input.getBooleanOr("KnockedOut", false));
        hamster.setHamsterFlag(HamsterEntity.CHEEK_POUCH_UNLOCKED_FLAG, input.getBooleanOr("CheekPouchUnlocked", false));
        hamster.setHamsterFlag(HamsterEntity.SULKING_FLAG, input.getBooleanOr("IsSulking", false));
        if (input.contains("SulkTimer")) {
            hamster.sulkTimer = input.getIntOr("SulkTimer", 0);
        } else if (hamster.isSulking()) {
            // Backwards compat: if older save has them sulking, assign timer
            hamster.sulkTimer = 160 + hamster.getRandom().nextInt(80);
        }

        hamster.setHamsterFlag(HamsterEntity.CELEBRATING_DIAMOND_FLAG, input.getBooleanOr("IsCelebratingDiamond", false));
        boolean loadedSleeping = input.getBooleanOr("IsSleeping", false);
        if (!hamster.isTame()) {
            loadedSleeping = false;
        }
        hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, loadedSleeping);
        hamster.throwCooldownEndTick = input.getLongOr("ThrowCooldownEnd", 0L);
        hamster.setHamsterFlag(HamsterEntity.THROW_COOLDOWN_FLAG, hamster.throwCooldownEndTick > hamster.level().getGameTime());
        hamster.getEntityData().set(HamsterEntity.GREEN_BEAN_BUFF_DURATION, input.getLongOr("GreenBeanBuffDuration", 0L));
        hamster.setAutoEatCooldownTicks(input.getIntOr("AutoEatCooldown", 0));
        hamster.setEjectionCheckCooldown(input.contains("EjectionCheckCooldown") ? input.getIntOr("EjectionCheckCooldown", 0) : 20);
        // Backwards compat for old Pink Petals
        if (input.contains("FlowerPosition")) {
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, input.getIntOr("FlowerPosition", 0));
        } else if (input.contains("PinkPetalType")) {
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, input.getIntOr("PinkPetalType", 0));
        }
        // Backwards compat: personality ID verification
        if (!input.contains("AnimationPersonalityId")) {
            int personalityId = hamster.getRandom().nextIntBetweenInclusive(1, 3);
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, personalityId);
            AdorableHamsterPets.LOGGER.debug("[NBT READ] Hamster ID {}: NBT had no personality, assigned new ID {}", hamster.getId(), personalityId);
        } else {
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, input.getIntOr("AnimationPersonalityId", 0));
        }
        hamster.setGeneticsVisualizerMember(input.getBooleanOr("isGeneticsVisualizerMember", false));
        if (input.contains("AggressionState")) {
            int stateOrdinal = input.getIntOr("AggressionState", 0);
            if (stateOrdinal >= 0 && stateOrdinal < HamsterEntity.AggressionState.values().length) {
                hamster.setAggressionState(HamsterEntity.AggressionState.values()[stateOrdinal]);
            }
        }

        // --- 2. Parent Following ---
        input.read("ParentUuid", UUIDUtil.CODEC).ifPresent(hamster::setParentUuid);

        // --- 3. Sleep State ---
        if (input.contains("DozingPhase")) {
            int phaseOrdinal = input.getIntOr("DozingPhase", 0);
            if (phaseOrdinal >= 0 && phaseOrdinal < HamsterEntity.DozingPhase.values().length) {
                HamsterEntity.DozingPhase phase = HamsterEntity.DozingPhase.values()[phaseOrdinal];
                hamster.setDozingPhase(phase);
                if (phase == HamsterEntity.DozingPhase.DEEP_SLEEP) {
                    hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, true);
                }
            } else {
                hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);
            }
        } else {
            hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);
        }
        hamster.getEntityData().set(HamsterEntity.CURRENT_DEEP_SLEEP_ANIM_ID, input.getStringOr("CurrentDeepSleepAnimId", ""));
        hamster.setQuiescentSitTimer(input.getIntOr("QuiescentSitTimer", 0));
        hamster.setDriftingOffTimer(input.getIntOr("DriftingOffTimer", 0));
        hamster.setSettleSleepCooldown(input.getIntOr("SettleSleepCooldown", 0));

        // --- 4. Inventory ---
        if (input.contains("Inventory")) {
            hamster.getItems().clear();
            ContainerHelper.loadAllItems(input.childOrEmpty("Inventory"), hamster.getItems());
            HamsterInventoryUtil.updateCheekStates(hamster);
            HamsterInventoryUtil.syncEquipmentTrackers(hamster);
        } else if (!hamster.level().isClientSide() && !hamster.isTame()) {
            HamsterInventoryUtil.generateWildLoot(hamster, hamster.getRandom());
            HamsterInventoryUtil.updateCheekStates(hamster);
            HamsterInventoryUtil.syncEquipmentTrackers(hamster);
        }

        // --- 5. Ore Seeking ---
        hamster.isPrimedToSeekDiamonds = input.getBooleanOr("IsPrimedToSeekDiamonds", false);
        hamster.foundOreCooldownEndTick = input.getLongOr("FoundOreCooldownEndTick", 0L);
        if (input.contains("OreTargetX") && input.contains("OreTargetY") && input.contains("OreTargetZ")) {
            hamster.currentOreTarget = new BlockPos(input.getIntOr("OreTargetX", 0), input.getIntOr("OreTargetY", 0), input.getIntOr("OreTargetZ", 0));
        } else {
            hamster.currentOreTarget = null;
        }

        // --- 6. Interaction & Mini-Game ---
        hamster.tagGameCooldownEndTick = input.getLongOr("TagGameCooldownEnd", 0L);
        hamster.stealingCooldownEndTick = input.getLongOr("StealingCooldownEnd", 0L);
        hamster.cropSnackCooldownEndTick = input.getLongOr("CropSnackCooldownEnd", 0L);
        hamster.hideAndSeekCooldownEndTick = input.getLongOr("HideAndSeekCooldownEnd", 0L);
        hamster.setGenericInteractionTimer(input.getIntOr("GenericInteractionTimer", 0));

        boolean holding = input.getBooleanOr("IsHoldingMouthItem", false);
        hamster.setHoldingMouthItem(holding);

        if (holding) {
            input.read("MouthItemStack", ItemStack.CODEC).ifPresent(hamster::setMouthItemStack);
        } else {
            hamster.setMouthItemStack(ItemStack.EMPTY);
        }

        // --- 7. Wander Mode ---
        hamster.setWanderModeActive(input.getBooleanOr("IsWanderModeActive", false));
        hamster.setLinkedBedPos(input.read("LinkedBedPos", GlobalPos.CODEC));
        hamster.setBypassNextSleepDelay(input.getBooleanOr("BypassNextSleepDelay", false));
        hamster.setStuckSearchingForBed(input.getBooleanOr("StuckSearchingForBed", false));
        hamster.setRescueSleeping(input.getBooleanOr("IsRescueSleeping", false));
        if (hamster.isRescueSleeping()) {
            hamster.setHamsterFlag(HamsterEntity.SLEEPING_FLAG, true);
        }

        // --- 8. Reconcile Accessory State ---
        hamster.updateAccessoryState();

        hamster.setLoadingNbt(false);
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                       Shoulder Data Handlers
     * ────────────────────────────────────────────────────────────────────────────*/

    /**
     * Takes a hamster's NBT data, deserializes it, sets the knocked-out flag,
     * and re-serializes it to a new NbtCompound.
     */
    public static CompoundTag setKnockedOutInNbt(CompoundTag originalNbt) {
        return HamsterState.fromNbt(originalNbt).map(data -> {
            int newFlags = data.hamsterFlags() | HamsterEntity.KNOCKED_OUT_FLAG;
            return data.withFlags(newFlags).toNbt();
        }).orElse(originalNbt); // Fallback
    }

    /**
     * Captures the current state of this hamster into a {@link HamsterState} record.
     */
    public static HamsterState saveToHamsterState(HamsterEntity hamster) {
        // --- 1. Update Trackers and Prepare NBT ---
        HamsterInventoryUtil.updateCheekStates(hamster);
        CompoundTag inventoryNbt = new CompoundTag();
        if (hamster.level() instanceof ServerLevel serverWorld) {
            TagValueOutput inventoryOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverWorld.registryAccess());
            ContainerHelper.saveAllItems(inventoryOutput, hamster.getItems());
            inventoryNbt = inventoryOutput.buildResult();
        }

        // --- 2. Save Active Status Effects to NBT ---
        CompoundTag effectsNbt = new CompoundTag();
        if (!hamster.getActiveEffects().isEmpty()) {
            effectsNbt.store("active_effects", MobEffectInstance.CODEC.listOf(), List.copyOf(hamster.getActiveEffects()));
        }

        // --- 3. Get Custom Name ---
        Optional<String> nameOptional = Optional.ofNullable(hamster.getCustomName()).map(Component::getString);

        // --- 4. Create Inner Data Record Instances ---
        HamsterState.MiniGameBehaviorData seekingData = new HamsterState.MiniGameBehaviorData(
                hamster.isPrimedToSeekDiamonds,
                hamster.foundOreCooldownEndTick,
                hamster.cropSnackCooldownEndTick,
                hamster.hideAndSeekCooldownEndTick,
                Optional.ofNullable(hamster.currentOreTarget)
        );
        HamsterState.GreenBeanBuffData buffData = new HamsterState.GreenBeanBuffData(
                hamster.getGreenBeanBuffEndTick(),
                hamster.getEntityData().get(HamsterEntity.GREEN_BEAN_BUFF_DURATION),
                effectsNbt
        );
        HamsterState.WanderModeData wanderData = new HamsterState.WanderModeData(
                hamster.getLinkedBedPos(),
                hamster.shouldBypassNextSleepDelay()
        );

        // --- 5. Create and Return the Main Data Record ---
        return new HamsterState(
                hamster.getUUID(),
                hamster.getGenome().saveToNbt(),
                hamster.getHealth(),
                inventoryNbt,
                hamster.getAge(),
                hamster.throwCooldownEndTick,
                buffData,
                hamster.getAutoEatCooldownTicks(),
                nameOptional,
                hamster.getEntityData().get(HamsterEntity.FLOWER_POS),
                hamster.getEntityData().get(HamsterEntity.ANIMATION_PERSONALITY_ID),
                seekingData,
                wanderData,
                hamster.getEntityData().get(HamsterEntity.HAMSTER_FLAGS),
                hamster.totalAgeTicks,
                hamster.timesBred
        );
    }

    /**
     * Creates a HamsterEntity instance from NBT data, typically from a player's shoulder.
     * Loads the hamster's variant, health, age, inventory, effects, and custom name.
     * Does NOT set the entity's position or spawn it in the world.
     */
    @Nullable
    public static HamsterEntity createFromNbt(ServerLevel world, @Nullable Player player, CompoundTag nbt) {
        Optional<HamsterState> dataOpt = HamsterState.fromNbt(nbt);
        if (dataOpt.isEmpty()) {
            AdorableHamsterPets.LOGGER.error("Failed to deserialize HamsterState from NBT: {}", nbt);
            return null;
        }
        HamsterState data = dataOpt.get();

        HamsterEntity hamster = ModEntities.HAMSTER.get().create(world, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);

        if (hamster != null) {
            // --- 1. Load Core Data ---
            hamster.setUUID(data.entityUuid());
            hamster.setGenome(HamsterGenome.readFromNbt(data.genomeNbt()));
            hamster.setHealth(data.health());
            if (player != null) {hamster.setOwnerUUID(player.getUUID());}
            hamster.setTame(true, true);
            hamster.setAge(data.breedingAge());
            hamster.throwCooldownEndTick = data.throwCooldownEndTick();
            hamster.setAutoEatCooldownTicks(data.autoEatCooldownTicks());
            hamster.getEntityData().set(HamsterEntity.FLOWER_POS, data.flowerPosition());
            hamster.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, data.animationPersonalityId());
            hamster.getEntityData().set(HamsterEntity.HAMSTER_FLAGS, data.hamsterFlags());
            hamster.totalAgeTicks = data.totalAgeTicks();
            hamster.timesBred = data.timesBred();

            // Sync vanilla sitting pose with restored flag
            hamster.setInSittingPose(hamster.getHamsterFlag(HamsterEntity.SITTING_FLAG));

            // --- 2. Load Custom Name ---
            data.customName().ifPresent(name -> {
                if (!name.isEmpty()) {
                    hamster.setCustomName(Component.literal(name));
                }
            });

            // --- 3. Load Inventory ---
            HolderLookup.Provider registries = world.registryAccess();
            if (!data.inventoryNbt().isEmpty()) {
                ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING, registries, data.inventoryNbt()), hamster.getItems());
                HamsterInventoryUtil.updateCheekStates(hamster);
                HamsterInventoryUtil.syncEquipmentTrackers(hamster);
            }

            // --- 4. Load Green Bean Buff Data/Status Effects ---
            HamsterState.GreenBeanBuffData buffData = data.greenBeanBuffData();
            hamster.setGreenBeanBuffEndTick(buffData.greenBeanBuffEndTick());
            hamster.getEntityData().set(HamsterEntity.GREEN_BEAN_BUFF_DURATION, buffData.greenBeanBuffDuration());
            CompoundTag effectsNbt = buffData.activeEffectsNbt();
            effectsNbt.read("active_effects", MobEffectInstance.CODEC.listOf())
                    .orElse(List.of())
                    .forEach(hamster::addEffect);

            // --- 5. Load Diamond Seeking Data ---
            HamsterState.MiniGameBehaviorData seekingData = data.seekingBehaviorData();
            hamster.isPrimedToSeekDiamonds = seekingData.isPrimedToSeekDiamonds();
            hamster.foundOreCooldownEndTick = seekingData.foundOreCooldownEndTick();
            hamster.cropSnackCooldownEndTick = seekingData.cropSnackCooldownEndTick();
            hamster.hideAndSeekCooldownEndTick = seekingData.hideAndSeekCooldownEndTick();
            hamster.currentOreTarget = seekingData.currentOreTarget().orElse(null);

            // --- 6. Load Wander Mode/Bed Data ---
            HamsterState.WanderModeData wanderData = data.wanderModeData();
            hamster.setLinkedBedPos(wanderData.linkedBedPos());
            hamster.setBypassNextSleepDelay(wanderData.bypassNextSleepDelay());

            // --- 7. Reset Transient Action Flags ---
            hamster.setHamsterFlag(HamsterEntity.CLEANING_FLAG, false);
            hamster.setDozingPhase(HamsterEntity.DozingPhase.NONE);

            // --- 8. Reconcile Accessory State ---
            hamster.updateAccessoryState();
        }
        return hamster;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *                               Private Helpers
     * ────────────────────────────────────────────────────────────────────────────*/

    private static boolean hasInventoryData(CompoundTag nbt) {
        return nbt.getCompound("Inventory").isPresent();
    }
}
