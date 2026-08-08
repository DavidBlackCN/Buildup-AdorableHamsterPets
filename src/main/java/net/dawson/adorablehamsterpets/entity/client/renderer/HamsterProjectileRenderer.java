package net.dawson.adorablehamsterpets.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dawson.adorablehamsterpets.entity.ModEntities;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterProjectileEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.dawson.adorablehamsterpets.util.HamsterInventoryUtil;
import net.dawson.adorablehamsterpets.util.HamsterState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

/** Delegates projectile visuals to the hamster GeckoLib renderer through a nested render state. */
public class HamsterProjectileRenderer extends EntityRenderer<HamsterProjectileEntity, HamsterProjectileRenderer.State> {
    private final HamsterRenderer hamsterRenderer;

    public HamsterProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.hamsterRenderer = new HamsterRenderer(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HamsterProjectileEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        ensureDummy(entity);
        HamsterEntity dummy = entity.clientDummyHamster;
        if (dummy == null) {
            state.hamster = null;
            return;
        }

        dummy.setDeltaMovement(entity.getDeltaMovement());
        dummy.setPos(entity.getX(), entity.getY(), entity.getZ());
        dummy.tickCount = entity.tickCount;
        Vec3 velocity = entity.getDeltaMovement();
        float yaw = (float) (Mth.atan2(-velocity.x, velocity.z) * Mth.RAD_TO_DEG);
        dummy.setYRot(yaw);
        dummy.yBodyRot = yaw;
        dummy.yBodyRotO = yaw;
        dummy.yHeadRot = yaw;
        dummy.yHeadRotO = yaw;
        state.hamster = hamsterRenderer.createRenderState(dummy, partialTick);
    }

    private static void ensureDummy(HamsterProjectileEntity entity) {
        if (entity.clientDummyHamster != null) {
            return;
        }
        entity.clientDummyHamster = ModEntities.HAMSTER.get().create(entity.level(), EntitySpawnReason.MOB_SUMMONED);
        HamsterEntity dummy = entity.clientDummyHamster;
        if (dummy == null) {
            return;
        }
        dummy.setNoGravity(true);
        dummy.setNoAi(true);
        dummy.isProjectileDummy = true;
        HamsterState.fromNbt(entity.getHamsterData()).ifPresent(data -> {
            dummy.setGenome(HamsterGenome.readFromNbt(data.genomeNbt()));
            dummy.setBaby(data.breedingAge() < 0);
            dummy.getEntityData().set(HamsterEntity.FLOWER_POS, data.flowerPosition());
            if (!data.inventoryNbt().isEmpty()) {
                dummy.getItems().clear();
                ContainerHelper.loadAllItems(
                        TagValueInput.create(ProblemReporter.DISCARDING, entity.level().registryAccess(), data.inventoryNbt()),
                        dummy.getItems()
                );
                HamsterInventoryUtil.syncEquipmentTrackers(dummy);
            }
        });
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.hamster != null) {
            hamsterRenderer.performRenderPass(state.hamster, poseStack, collector, cameraState);
        }
        super.submit(state, poseStack, collector, cameraState);
    }

    public static class State extends EntityRenderState {
        private LivingEntityRenderState hamster;
    }
}
