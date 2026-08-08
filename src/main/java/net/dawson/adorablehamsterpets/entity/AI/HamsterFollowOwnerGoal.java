package net.dawson.adorablehamsterpets.entity.AI;

import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.mixin.accessor.FollowOwnerGoalAccessor;
import net.dawson.adorablehamsterpets.util.HamsterMovementUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class HamsterFollowOwnerGoal extends FollowOwnerGoal {

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constants
     * ────────────────────────────────────────────────────────────────────────────*/

    private static final double BUFFED_FOLLOW_SPEED = 1.5D;

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Instance Fields
     * ────────────────────────────────────────────────────────────────────────────*/

    private final HamsterEntity hamster;

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Constructors
     * ────────────────────────────────────────────────────────────────────────────*/

    public HamsterFollowOwnerGoal(HamsterEntity hamster, double speed, float minDistance, float maxDistance) {
        super(hamster, speed, minDistance, maxDistance);
        this.hamster = hamster;
    }

    /* ──────────────────────────────────────────────────────────────────────────────
     *        Lifecycle Hooks
     * ────────────────────────────────────────────────────────────────────────────*/

    @Override
    public boolean canUse() {
        // --- Base Logic ---
        if (!super.canUse()) {
            return false;
        }

        // --- Parent Override ---
        // Abort if baby is tracking a living parent
        if (this.hamster.isBaby() && this.hamster.getParentUuid() != null) {
            if (this.hamster.level() instanceof ServerLevel serverWorld) {
                Entity parent = serverWorld.getEntity(this.hamster.getParentUuid());
                if (parent instanceof HamsterEntity parentHamster && parentHamster.isAlive()) {
                    return false;
                }
            }
        }

        // --- State Exclusions ---
        if (HamsterMovementUtil.shouldNotFollow(this.hamster)) {
            return false;
        }

        // --- Distance Calculation ---
        // Recalculate minimum follow distance for certain states
        float minDist = ((FollowOwnerGoalAccessor) this).getMinDistance();
        LivingEntity owner = ((FollowOwnerGoalAccessor) this).getOwner();

        if (owner == null || this.hamster.unableToMoveToOwner()) {
            return false;
        }

        if (this.hamster.hasGreenBeanBuff() || this.hamster.getAggressionState() == HamsterEntity.AggressionState.MENACE) {
            minDist += 5.0F;
        }

        return !(this.hamster.distanceToSqr(owner) < (double) (minDist * minDist));
    }

    @Override
    public boolean canContinueToUse() {
        // --- State Exclusions ---
        if (HamsterMovementUtil.shouldNotFollow(this.hamster)) {
            return false;
        }

        // --- Distance Calculation ---
        // Recalculate maximum follow distance for certain states
        float maxDist = ((FollowOwnerGoalAccessor) this).getMaxDistance();
        LivingEntity owner = ((FollowOwnerGoalAccessor) this).getOwner();

        if (owner == null) {
            return false;
        }

        if (this.hamster.hasGreenBeanBuff() || this.hamster.getAggressionState() == HamsterEntity.AggressionState.MENACE) {
            maxDist += 5.0F;
        }

        return !this.hamster.getNavigation().isDone() && this.hamster.distanceToSqr(owner) > (double) (maxDist * maxDist);
    }

    @Override
    public void start() {
        super.start();
        this.hamster.setActiveCustomGoalName(this.getClass().getSimpleName() + (this.hamster.hasGreenBeanBuff() ? " (Zoomies)" : ""));
    }

    @Override
    public void stop() {
        super.stop();
        if (this.hamster.getActiveCustomGoalName().startsWith(this.getClass().getSimpleName())) {
            this.hamster.setActiveCustomGoalName("None");
        }
    }

    @Override
    public void tick() {
        // --- Target Resolution ---
        FollowOwnerGoalAccessor accessor = (FollowOwnerGoalAccessor) this;
        LivingEntity owner = accessor.getOwner();

        if (owner == null) {
            return;
        }

        boolean shouldTeleport = HamsterMovementUtil.shouldTeleportTo(this.hamster, owner);

        // --- Facing Logic ---
        if (!shouldTeleport) {
            HamsterMovementUtil.faceEntity(this.hamster, owner);
        }

        // --- Update Timer ---
        int currentTicks = accessor.getUpdateCountdownTicks() - 1;
        accessor.setUpdateCountdownTicks(currentTicks);

        if (currentTicks <= 0) {
            accessor.setUpdateCountdownTicks(this.adjustedTickDelay(10));

            // --- Movement Execution ---
            if (shouldTeleport) {
                HamsterMovementUtil.tryTeleportTo(this.hamster, owner);
            } else {
                // Calculate base speed and apply a 50% reduction if they are currently busting a move
                double activeSpeed = this.hamster.hasGreenBeanBuff() ? BUFFED_FOLLOW_SPEED : accessor.getSpeed();
                if (this.hamster.isDancing()) {
                    activeSpeed *= 0.5;
                }

                if (this.hamster.hasGreenBeanBuff()) {
                    // Zoomies erratic pathfinding
                    Vec3 targetPos = LandRandomPos.getPosTowards(this.hamster, 8, 5, Vec3.atCenterOf(owner.blockPosition()));
                    if (targetPos != null) {
                        this.hamster.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, activeSpeed);
                    }
                } else {
                    // Standard pathfinding
                    this.hamster.getNavigation().moveTo(owner, activeSpeed);
                }
            }
        }
    }
}