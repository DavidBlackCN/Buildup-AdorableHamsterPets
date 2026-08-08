package net.dawson.adorablehamsterpets.entity.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dawson.adorablehamsterpets.accessor.PlayerEntityAccessor;
import net.dawson.adorablehamsterpets.client.state.ClientShoulderHamsterData;
import net.dawson.adorablehamsterpets.entity.ShoulderLocation;
import net.dawson.adorablehamsterpets.entity.client.HamsterRenderer;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.genetics.HamsterGenome;
import net.dawson.adorablehamsterpets.util.HamsterInventoryUtil;
import net.dawson.adorablehamsterpets.util.HamsterState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Renders persisted hamster state on an avatar using the 26.1 render-state pipeline. */
public class HamsterShoulderFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final float HAMSTER_SHOULDER_SCALE = 0.8F;

    private final HamsterRenderer hamsterRenderer;
    private final Map<AvatarRenderState, EnumMap<ShoulderLocation, Entry>> capturedStates = new WeakHashMap<>();

    public HamsterShoulderFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, HamsterRenderer hamsterRenderer) {
        super(parent);
        this.hamsterRenderer = hamsterRenderer;
    }

    public void capture(AbstractClientPlayer player, AvatarRenderState avatarState, float partialTick) {
        EnumMap<ShoulderLocation, Entry> entries = new EnumMap<>(ShoulderLocation.class);
        capturedStates.put(avatarState, entries);

        PlayerEntityAccessor accessor = (PlayerEntityAccessor) player;
        try {
            if (!accessor.hasAnyShoulderHamster()) {
                return;
            }
        } catch (RuntimeException ignored) {
            return;
        }

        ClientShoulderHamsterData clientData = accessor.adorablehamsterpets$getClientHamsterState();
        if (clientData == null) {
            return;
        }

        boolean wearingChestplate = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                && !player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        boolean slim = player.getSkin().model() == PlayerModelType.SLIM;

        for (ShoulderLocation location : ShoulderLocation.values()) {
            HamsterState.fromNbt(accessor.getShoulderHamster(location)).ifPresent(data -> {
                HamsterEntity dummy = clientData.getOrCreateDummy(location, player.level());
                applyHamsterState(dummy, data, player);
                dummy.tickCount = clientData.getAnimationAge(location);
                dummy.setPos(player.getX(), player.getY(), player.getZ());
                dummy.setYRot(180.0F - player.getVisualRotationYInDegrees());
                dummy.shoulderLocation = location;

                ShoulderHamsterState animationState = clientData.getHamsterState(location);
                if (animationState != null) {
                    ShoulderAnimationState current = animationState.getCurrentState();
                    dummy.getEntityData().set(HamsterEntity.SHOULDER_ANIMATION_STATE, current.ordinal());
                    dummy.setSitting(current == ShoulderAnimationState.SITTING, true);
                }

                dummy.dynamicScaleY = clientData.getRenderScaleY(location, partialTick);
                LivingEntityRenderState hamsterState = hamsterRenderer.createRenderState(dummy, partialTick);
                entries.put(location, new Entry(
                        hamsterState,
                        clientData.getRenderOffsetY(location, partialTick),
                        wearingChestplate,
                        slim
                ));
            });
        }
    }

    private static void applyHamsterState(HamsterEntity dummy, HamsterState data, AbstractClientPlayer owner) {
        dummy.setGenome(HamsterGenome.readFromNbt(data.genomeNbt()));
        dummy.setLeftCheekFull((data.hamsterFlags() & HamsterEntity.LEFT_CHEEK_FULL_FLAG) != 0);
        dummy.setRightCheekFull((data.hamsterFlags() & HamsterEntity.RIGHT_CHEEK_FULL_FLAG) != 0);
        dummy.getEntityData().set(HamsterEntity.FLOWER_POS, data.flowerPosition());
        dummy.getEntityData().set(HamsterEntity.ANIMATION_PERSONALITY_ID, data.animationPersonalityId());
        dummy.getEntityData().set(HamsterEntity.HAMSTER_FLAGS, data.hamsterFlags());
        dummy.getEntityData().set(HamsterEntity.EXACT_AGE, data.breedingAge());
        dummy.setAge(data.breedingAge());
        dummy.setBaby(data.breedingAge() < 0);
        dummy.setShoulderPet(true);
        dummy.setCustomName(data.customName().filter(name -> !name.isEmpty()).map(net.minecraft.network.chat.Component::literal).orElse(null));
        dummy.getItems().clear();
        if (!data.inventoryNbt().isEmpty()) {
            ContainerHelper.loadAllItems(
                    TagValueInput.create(ProblemReporter.DISCARDING, owner.registryAccess(), data.inventoryNbt()),
                    dummy.getItems()
            );
            HamsterInventoryUtil.syncEquipmentTrackers(dummy);
        }
        dummy.setOwnerUUID(owner.getUUID());
        dummy.setTame(true, false);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState avatarState,
                       float yRot, float xRot) {
        EnumMap<ShoulderLocation, Entry> entries = capturedStates.get(avatarState);
        if (entries == null || entries.isEmpty()) {
            return;
        }

        entries.forEach((location, entry) -> {
            poseStack.pushPose();
            switch (location) {
                case RIGHT_SHOULDER -> {
                    getParentModel().rightArm.translateAndRotate(poseStack);
                    poseStack.translate(entry.wearingChestplate ? -0.18F : entry.slim ? -0.08F : -0.12F,
                            entry.wearingChestplate ? -0.18F : -0.12F, -0.016F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(15.0F));
                }
                case LEFT_SHOULDER -> {
                    getParentModel().leftArm.translateAndRotate(poseStack);
                    poseStack.translate(entry.wearingChestplate ? 0.18F : entry.slim ? 0.08F : 0.12F,
                            entry.wearingChestplate ? -0.18F : -0.12F, -0.016F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-15.0F));
                }
                case HEAD -> {
                    getParentModel().head.translateAndRotate(poseStack);
                    poseStack.translate(0.0F, -0.5F, -0.05F);
                }
            }
            poseStack.translate(0.0F, -entry.offsetY, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.scale(HAMSTER_SHOULDER_SCALE, HAMSTER_SHOULDER_SCALE, HAMSTER_SHOULDER_SCALE);
            hamsterRenderer.performRenderPass(entry.state, poseStack, collector, null);
            poseStack.popPose();
        });
    }

    private record Entry(LivingEntityRenderState state, float offsetY, boolean wearingChestplate, boolean slim) {}
}
