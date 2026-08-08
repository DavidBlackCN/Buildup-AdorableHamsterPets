package net.dawson.adorablehamsterpets.entity.client;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dawson.adorablehamsterpets.AdorableHamsterPetsClient;
import net.dawson.adorablehamsterpets.config.Configs;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.item.ModItems;
import net.dawson.adorablehamsterpets.item.custom.HamsterArmorItem;
import net.dawson.adorablehamsterpets.networking.NetworkManager;
import net.dawson.adorablehamsterpets.networking.payload.HamsterAnimationSoundPayload;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.sound.ModSounds;
import net.dawson.adorablehamsterpets.util.HamsterMouthItemOffsets;
import net.dawson.adorablehamsterpets.util.HamsterRenderUtil;
import net.dawson.adorablehamsterpets.util.HamsterRidingUtil;
import net.dawson.adorablehamsterpets.util.HamsterTextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class HamsterRenderer extends GeoEntityRenderer<HamsterEntity, LivingEntityRenderState> {
    private static final float ADULT_SCALE = 0.8F;
    private static final float ADULT_HEAD_SCALE = 1.0F;
    private static final float BABY_SCALE = 0.5F;
    private static final float BABY_HEAD_SCALE = 1.2F;
    private static final float ADULT_SHADOW_RADIUS = 0.2F;

    public static final ThreadLocal<Boolean> IS_RENDERING_PASSENGER = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Boolean> IS_RENDERING_IN_GUI = ThreadLocal.withInitial(() -> false);

    public HamsterRenderer(EntityRendererProvider.Context context) {
        super(context, new HamsterModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        withRenderLayer(new MouthItemLayer(context));
    }

    @Override
    public void addRenderData(HamsterEntity entity, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        ItemStack armor = entity.getArmorStack();
        boolean armorVisible = Configs.AHP_MAIN.enableArmorVisuals
                && !armor.isEmpty()
                && armor.getItem() instanceof HamsterArmorItem;
        boolean showHat = entity.getAccessoryStack().is(ModItems.ACORN_HAT.get())
                || (armor.is(ModItems.HAMSTER_ARMOR_ACORN.get()) && Configs.AHP_MAIN.renderAcornHat.get());

        float ageProgress = entity.isBaby()
                ? 1.0F - Math.abs(entity.getEntityData().get(HamsterEntity.EXACT_AGE)) / 24000.0F
                : 1.0F;
        float baseScale = Mth.lerp(ageProgress, BABY_SCALE, ADULT_SCALE);
        float headScale = Mth.lerp(ageProgress, BABY_HEAD_SCALE, ADULT_HEAD_SCALE);
        float scaleY = entity.isShoulderPet() ? baseScale * entity.dynamicScaleY : baseScale;

        float pitch = 0.0F;
        Vec3 velocity = entity.getDeltaMovement();
        if (entity.isProjectileDummy) {
            double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            pitch = (float) Math.atan2(velocity.y, horizontalSpeed);
        } else if (entity.isInWater() || entity.isInLava()) {
            pitch = Mth.lerp(partialTick, entity.prevClientSwimPitch, entity.clientSwimPitch);
        } else if (entity.clientFallPitchProgress > 0.0F || entity.prevClientFallPitchProgress > 0.0F) {
            float progress = Mth.lerp(partialTick, entity.prevClientFallPitchProgress, entity.clientFallPitchProgress);
            pitch = (float) (-Math.PI / 2.0) * ((1.0F - Mth.cos(progress * (float) Math.PI)) * 0.5F);
        }

        if (IS_RENDERING_IN_GUI.get() || entity.isShoulderPet() || entity.isProjectileDummy) {
            entity.renderedGroundYOffset = 0.0F;
        } else {
            float target = HamsterRenderUtil.getGroundSurfaceOffset(entity);
            entity.renderedGroundYOffset += (target - entity.renderedGroundYOffset) * 0.15F;
        }

        double currentTick = entity.tickCount + partialTick;
        entity.lastRenderTime = currentTick;

        List<HamsterModel.PassengerRenderData> passengers = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        for (net.minecraft.world.entity.Entity passenger : entity.getPassengers()) {
            if (!(passenger instanceof LivingEntity living)) {
                continue;
            }
            if (passenger == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
                continue;
            }

            EntityRenderState passengerState = entityRenderDispatcher.extractEntity(passenger, partialTick);
            passengerState.passengerOffset = Vec3.ZERO;
            passengerState.shadowPieces.clear();
            float bodyRot = passengerState instanceof LivingEntityRenderState livingState ? livingState.bodyRot : 0.0F;
            passengers.add(new HamsterModel.PassengerRenderData(
                    passengerState,
                    HamsterRidingUtil.HamsterSeatOffsets.visualSeatOffset(living, entity.getScale()),
                    bodyRot));
        }

        renderState.addGeckolibData(HamsterModel.RENDER_DATA, new HamsterModel.RenderData(
                HamsterTextureUtil.getHamsterTexture(entity),
                entity.getId(),
                entity.isBaby(),
                entity.isNoAi(),
                entity.isMoonwalking(),
                entity.isLeftCheekFull(),
                entity.isRightCheekFull(),
                showHat,
                showHat,
                entity.getEntityData().get(HamsterEntity.FLOWER_POS),
                armorVisible && Configs.AHP_MAIN.renderFlowersWithArmor.get(),
                baseScale,
                headScale,
                scaleY,
                pitch,
                entity.renderedGroundYOffset,
                AdorableHamsterPetsClient.isPerformanceModeEnabled,
                entity.isProjectileDummy,
                velocity,
                List.copyOf(passengers),
                entity.particleEffectId,
                entity.soundEffectId));
    }

    @Override
    public boolean shouldShowName(HamsterEntity entity, double distanceToCameraSq) {
        return !IS_RENDERING_IN_GUI.get() && super.shouldShowName(entity, distanceToCameraSq);
    }

    @Override
    protected float getShadowRadius(LivingEntityRenderState renderState) {
        HamsterModel.RenderData data = renderState.getGeckolibData(HamsterModel.RENDER_DATA);
        return data != null && data.baby() ? ADULT_SHADOW_RADIUS * 0.5F : ADULT_SHADOW_RADIUS;
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<LivingEntityRenderState> renderPass) {
        super.adjustRenderPose(renderPass);
        HamsterModel.RenderData data = renderPass.getGeckolibData(HamsterModel.RENDER_DATA);
        if (data != null) {
            renderPass.poseStack().translate(0.0, data.groundYOffset(), 0.0);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPass, BoneSnapshots snapshots) {
        HamsterModel.RenderData data = renderPass.getGeckolibData(HamsterModel.RENDER_DATA);
        if (data == null) return;

        if (data.performanceMode()) {
            renderPass.model().boneLookup().get().values().forEach(bone -> {
                String name = bone.name();
                snapshots.get(bone).skipRender(!name.equals("root") && !name.equals("body_parent") && !name.equals("body_child"));
            });
            return;
        }

        snapshots.ifPresent("closed_eyes", bone -> bone.skipRender(data.noAi()));
        setHidden(snapshots, "left_cheek_deflated", data.leftCheekFull());
        setHidden(snapshots, "left_cheek_inflated", !data.leftCheekFull());
        setHidden(snapshots, "right_cheek_deflated", data.rightCheekFull());
        setHidden(snapshots, "right_cheek_inflated", !data.rightCheekFull());
        setHidden(snapshots, "right_ear", data.hideRightEar());
        setHidden(snapshots, "acorn_hat", !data.showAcornHat());

        setHidden(snapshots, "flower_head_no_armor", data.flowerType() != 1 || data.useArmorFlowers());
        setHidden(snapshots, "flower_side_no_armor", data.flowerType() != 2 || data.useArmorFlowers());
        setHidden(snapshots, "flower_lower_back_no_armor", data.flowerType() != 3 || data.useArmorFlowers());
        setHidden(snapshots, "flower_head_with_armor", data.flowerType() != 1 || !data.useArmorFlowers());
        setHidden(snapshots, "flower_side_with_armor", data.flowerType() != 2 || !data.useArmorFlowers());
        setHidden(snapshots, "flower_lower_back_with_armor", data.flowerType() != 3 || !data.useArmorFlowers());

        snapshots.ifPresent("root", root -> root
                .setScale(data.baseScale(), data.scaleY(), data.baseScale())
                .setRotX(data.pitch())
                .setRotY(data.moonwalking() ? (float) Math.PI : 0.0F));
        snapshots.ifPresent("head_parent", head -> head.setScale(data.headScale(), data.headScale(), data.headScale()));

        if (!data.passengers().isEmpty()) {
            renderPass.model().getBone("body_child").ifPresent(bone ->
                    renderPass.addPerBoneRender(bone, this::submitPassengers));
        }

        if (data.particleEffectId() != null) {
            String boneName = data.particleEffectId().equals("attack_poof") ? "left_foot" : "nose";
            renderPass.addBonePositionListener(boneName,
                    (position, rotation, scale) -> handleParticleKeyframe(data.entityId(), data.particleEffectId(), position));
        }
    }

    @Override
    public void postRenderPass(RenderPassInfo<LivingEntityRenderState> renderPass, SubmitNodeCollector submitNodes) {
        super.postRenderPass(renderPass, submitNodes);
        HamsterModel.RenderData data = renderPass.getGeckolibData(HamsterModel.RENDER_DATA);
        if (data != null) {
            AdorableHamsterPetsClient.onHamsterRendered(data.entityId());
            if (data.soundEffectId() != null) {
                handleSoundKeyframe(data.entityId(), data.soundEffectId());
            }
        }
    }

    private static void setHidden(BoneSnapshots snapshots, String name, boolean hidden) {
        snapshots.ifPresent(name, bone -> bone.skipRender(hidden));
    }

    private void submitPassengers(RenderPassInfo<LivingEntityRenderState> renderPass,
                                  com.geckolib.cache.model.GeoBone bone,
                                  SubmitNodeCollector submitNodes) {
        HamsterModel.RenderData data = renderPass.getGeckolibData(HamsterModel.RENDER_DATA);
        if (data == null || renderPass.cameraState() == null) {
            return;
        }

        PoseStack poseStack = renderPass.poseStack();
        for (HamsterModel.PassengerRenderData passenger : data.passengers()) {
            poseStack.pushPose();
            poseStack.scale(1.0F / Math.max(data.baseScale(), 1.0e-6F),
                    1.0F / Math.max(data.scaleY(), 1.0e-6F),
                    1.0F / Math.max(data.baseScale(), 1.0e-6F));
            poseStack.translate(passenger.seatOffset().x, passenger.seatOffset().y, passenger.seatOffset().z);
            poseStack.mulPose(Axis.YP.rotationDegrees(passenger.bodyRot() - 180.0F));

            IS_RENDERING_PASSENGER.set(true);
            try {
                entityRenderDispatcher.submit(passenger.state(), renderPass.cameraState(),
                        0.0, 0.0, 0.0, poseStack, submitNodes);
            } finally {
                IS_RENDERING_PASSENGER.set(false);
                poseStack.popPose();
            }
        }
    }

    private static HamsterEntity getLiveHamster(int entityId) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        net.minecraft.world.entity.Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return entity instanceof HamsterEntity hamster ? hamster : null;
    }

    private static void handleParticleKeyframe(int entityId, String effectId, Vec3 position) {
        HamsterEntity hamster = getLiveHamster(entityId);
        if (hamster == null || !effectId.equals(hamster.particleEffectId)) {
            return;
        }
        hamster.particleEffectId = null;

        var random = hamster.getRandom();
        switch (effectId) {
            case "attack_poof" -> {
                for (int i = 0; i < 8; i++) {
                    hamster.level().addParticle(ParticleTypes.WHITE_SMOKE,
                            position.x + random.nextGaussian() * 0.1,
                            position.y + random.nextGaussian() * 0.2,
                            position.z + random.nextGaussian() * 0.1,
                            random.nextGaussian() * 0.05,
                            random.nextGaussian() * 0.05,
                            random.nextGaussian() * 0.05);
                }
            }
            case "seeking_dust" -> {
                BlockPos below = BlockPos.containing(position.x, position.y - 0.1, position.z).below();
                BlockState state = hamster.level().getBlockState(below);
                if (state.isAir()) {
                    state = Blocks.DIRT.defaultBlockState();
                }
                for (int i = 0; i < 12; i++) {
                    hamster.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state),
                            position.x + random.nextGaussian() * 0.2,
                            position.y + random.nextGaussian() * 0.03,
                            position.z + random.nextGaussian() * 0.2,
                            0.0, 0.0, 0.0);
                }
            }
            case "hamster_spit_particles" -> {
                ItemStack mouthStack = hamster.getMouthItemStack();
                if (!mouthStack.isEmpty()) {
                    for (int i = 0; i < 5; i++) {
                        hamster.level().addParticle(new ItemParticleOption(
                                        ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(mouthStack)),
                                position.x, position.y, position.z,
                                (random.nextDouble() - 0.5) * 0.3,
                                random.nextDouble() * 0.2,
                                (random.nextDouble() - 0.5) * 0.3);
                    }
                }
                for (int i = 0; i < 8; i++) {
                    hamster.level().addParticle(ParticleTypes.SPIT,
                            position.x, position.y, position.z,
                            (random.nextDouble() - 0.5) * 0.1,
                            random.nextDouble() * 0.1,
                            (random.nextDouble() - 0.5) * 0.1);
                }
            }
            default -> { }
        }
    }

    private static void handleSoundKeyframe(int entityId, String soundId) {
        HamsterEntity hamster = getLiveHamster(entityId);
        if (hamster == null || !soundId.equals(hamster.soundEffectId)) {
            return;
        }
        hamster.soundEffectId = null;

        switch (soundId) {
            case "dynamic_item_sound" -> {
                ItemStack mouthStack = hamster.getMouthItemStack();
                if (!mouthStack.isEmpty()) {
                    SoundEvent sound = ModSounds.getDynamicItemSound(mouthStack);
                    playSound(hamster, sound, ModSounds.getDynamicSoundVolume(sound) * 0.6F,
                            1.0F + (hamster.getRandom().nextFloat() - 0.5F) * 0.2F);
                }
            }
            case "hamster_scratch_sound" -> playRandom(hamster, ModSounds.HAMSTER_SCRATCH_SOUNDS, 0.2F, 0.8F);
            case "hamster_roll_back_sound" -> playSound(hamster,
                    Configs.AHP_MAIN.enableRollingSlideWhistle
                            ? ModSounds.HAMSTER_ROLL_BACK.get()
                            : ModSounds.HAMSTER_ROLL_BACK_NO_SLIDE_WHISTLE.get(), 0.3F, 1.4F);
            case "hamster_roll_forward_sound" -> playSound(hamster, ModSounds.HAMSTER_ROLL_FORWARD.get(), 0.2F, 1.4F);
            case "hamster_step_sound" -> {
                BlockState state = hamster.level().getBlockState(hamster.blockPosition().below());
                if (state.isAir()) {
                    state = hamster.level().getBlockState(hamster.blockPosition().below(2));
                }
                if (!state.isAir()) {
                    SoundType soundType = state.getSoundType();
                    playSound(hamster, soundType.getStepSound(), state.is(Blocks.GRAVEL) ? 0.06F : 0.1F,
                            soundType.getPitch() * 1.5F);
                }
            }
            case "hamster_bounce_sound" -> {
                if (!hamster.isDancing()) {
                    SoundEvent sound = ModSounds.getRandomSoundFrom(ModSounds.HAMSTER_BOUNCE_SOUNDS, hamster.getRandom());
                    if (sound != null) {
                        playSound(hamster, sound, 0.6F,
                                hamster.getHamsterVoicePitch() * 1.2F + hamster.getRandom().nextFloat() * 0.2F);
                    }
                }
            }
            case "hamster_thump_sound" -> {
                playSound(hamster, ModSounds.HAMSTER_THUMP.get(), 0.3F,
                        1.0F + hamster.getRandom().nextFloat() * 0.4F);
                if (Minecraft.getInstance().player != null
                        && hamster.distanceToSqr(Minecraft.getInstance().player) > 256.0) {
                    NetworkManager.sendToServer(new HamsterAnimationSoundPayload(entityId, soundId));
                }
            }
            case "hamster_spit_sound" -> playSound(hamster, SoundEvents.LLAMA_SPIT, 0.4F, 2.0F);
            case "hamster_sniff_sound" -> playRandom(hamster, ModSounds.HAMSTER_DIAMOND_SNIFF_SOUNDS,
                    1.0F, hamster.getHamsterVoicePitch());
            case "hamster_head_shake_fast_sound" -> playSound(hamster, ModSounds.HAMSTER_HEAD_SHAKE_FAST.get(),
                    0.35F, 1.0F + (hamster.getRandom().nextFloat() - 0.5F) * 0.2F);
            case "hamster_swish_sound" -> playSound(hamster, ModSounds.HAMSTER_SWISH.get(), 0.1F,
                    1.0F + hamster.getRandom().nextFloat() * 0.5F);
            case "hamster_water_swish_sound" -> playRandom(hamster, ModSounds.HAMSTER_WATER_SWISH_SOUNDS, 0.25F, 1.0F);
            case "hamster_affection_sound" -> playRandom(hamster, ModSounds.HAMSTER_AFFECTION_SOUNDS, 1.0F, 1.0F);
            case "hamster_celebrate_sound" -> playRandom(hamster, ModSounds.HAMSTER_CELEBRATE_SOUNDS, 1.0F, 1.0F);
            default -> { }
        }
    }

    private static void playRandom(HamsterEntity hamster, List<RegistrySupplier<SoundEvent>> sounds,
                                   float volume, float pitch) {
        SoundEvent sound = ModSounds.getRandomSoundFrom(sounds, hamster.getRandom());
        if (sound != null) {
            playSound(hamster, sound, volume, pitch);
        }
    }

    private static void playSound(HamsterEntity hamster, SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                sound, SoundSource.NEUTRAL, volume, pitch, hamster.getRandom(),
                hamster.getX(), hamster.getY(), hamster.getZ()));
    }

    private final class MouthItemLayer extends BlockAndItemGeoLayer<HamsterEntity, Void, LivingEntityRenderState> {
        private MouthItemLayer(EntityRendererProvider.Context context) {
            super(context, HamsterRenderer.this);
        }

        @Override
        protected List<RenderData> getRelevantBones(HamsterEntity entity, Void relatedObject,
                                                     LivingEntityRenderState renderState, float partialTick) {
            ItemStack mouthStack = entity.getMouthItemStack();
            if (!entity.isHoldingMouthItem() || mouthStack.isEmpty()) {
                return List.of();
            }

            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForLiving(itemState, mouthStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
            return List.of(RenderData.item("nose", ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, itemState));
        }

        @Override
        public void addRenderData(HamsterEntity entity, Void relatedObject, LivingEntityRenderState renderState,
                                  float partialTick) {
            renderState.addGeckolibData(CONTENTS, getRelevantBones(entity, relatedObject, renderState, partialTick));
        }

        @Override
        protected void submitItemStackRender(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                             com.geckolib.cache.model.GeoBone bone,
                                             ItemStackRenderState itemState,
                                             ItemDisplayContext displayContext,
                                             LivingEntityRenderState renderState,
                                             SubmitNodeCollector submitNodes,
                                             int packedLight) {
            poseStack.pushPose();
            HamsterMouthItemOffsets.applyMouthItemTransforms(poseStack);
            super.submitItemStackRender(poseStack, bone, itemState, displayContext, renderState, submitNodes, packedLight);
            poseStack.popPose();
        }
    }
}
