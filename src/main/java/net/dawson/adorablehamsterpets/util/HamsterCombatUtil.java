package net.dawson.adorablehamsterpets.util;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import java.util.UUID;

/**
 * Handles hamster-specific melee range and owner-combat exclusions.
 */
public final class HamsterCombatUtil {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final double ATTACK_BOX_EXPANSION = 0.70D;

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Static Utilities
     * ────────────────────────────────────────────────────────────────────────────*/

    public static boolean isInAttackRange(HamsterEntity hamster, LivingEntity target) {
        AABB attackBox =
                hamster.getBoundingBox().inflate(ATTACK_BOX_EXPANSION, 0.0D, ATTACK_BOX_EXPANSION);
        return attackBox.intersects(target.getBoundingBox());
    }

    public static boolean canAttackWithOwner(
            HamsterEntity hamster, LivingEntity target, LivingEntity owner) {
        // --- 1. Aggression and Target Freshness ---
        if (hamster.getAggressionState() == HamsterEntity.AggressionState.PACIFIST) {
            return false;
        }
        if (target == owner.getLastHurtMob() && owner.tickCount - owner.getLastHurtMobTimestamp() > 100) {
            return false;
        }
        if (target == owner.getLastHurtByMob() && owner.tickCount - owner.getLastHurtByMobTimestamp() > 100) {
            return false;
        }

        // --- 2. Owner and Hamster Exclusions ---
        UUID ownerUuid = owner.getUUID();
        AdorableHamsterPets.LOGGER.trace(
                "[canAttackWithOwner] Hamster: {}, Target: {}, Owner: {}",
                hamster.getName().getString(),
                target.getName().getString(),
                owner.getName().getString());

        if (target == hamster || target == owner) {
            return false;
        }
        if (target instanceof Player && target.getUUID().equals(ownerUuid)) {
            return false;
        }
        if (target instanceof Creeper || target instanceof ArmorStand) {
            return false;
        }
        // --- 3. Shared-Owner Pet Exclusions ---
        if (target instanceof TamableAnimal tameablePet) {
            UUID petOwnerUuid = tameablePet.getOwnerReference() == null ? null : tameablePet.getOwnerReference().getUUID();
            if (petOwnerUuid != null && petOwnerUuid.equals(ownerUuid)) {
                AdorableHamsterPets.LOGGER.trace(
                        "[canAttackWithOwner] Target is a TameableEntity owned by the same player."
                                + " Preventing attack.");
                return false;
            }
        } else if (target instanceof AbstractHorse horsePet) {
            Entity horseOwnerEntity = horsePet.getOwner();
            if (horseOwnerEntity != null && horseOwnerEntity.getUUID().equals(ownerUuid)) {
                AdorableHamsterPets.LOGGER.trace(
                        "[canAttackWithOwner] Target is an AbstractHorseEntity owned by the same"
                                + " player. Preventing attack.");
                return false;
            }
        } else if (target instanceof TraceableEntity ownableFallback) {
            Entity fallbackOwnerEntity = ownableFallback.getOwner();
            if (fallbackOwnerEntity != null && fallbackOwnerEntity.getUUID().equals(ownerUuid)) {
                AdorableHamsterPets.LOGGER.trace(
                        "[canAttackWithOwner] Target is an Ownable (fallback) owned by the same"
                                + " player. Preventing attack.");
                return false;
            }
        }
        return true;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constructors
     * ────────────────────────────────────────────────────────────────────────────*/

    private HamsterCombatUtil() {}
}
