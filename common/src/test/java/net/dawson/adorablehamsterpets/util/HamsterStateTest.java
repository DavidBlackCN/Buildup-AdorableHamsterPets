package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

class HamsterStateTest {

    @Test
    void legacyStateDefaultsArmorToVisible() {
        NbtCompound nbt = createState(false).toNbt();
        nbt.remove("armorVisible");

        HamsterState decoded = decode(nbt);

        assertTrue(decoded.armorVisible());
    }

    @Test
    void hiddenArmorSurvivesStateRoundTrip() {
        HamsterState decoded = decode(createState(false).toNbt());

        assertFalse(decoded.armorVisible());
    }

    private static HamsterState createState(boolean armorVisible) {
        return new HamsterState(
                UUID.randomUUID(),
                new NbtCompound(),
                10.0F,
                new NbtCompound(),
                0,
                0L,
                HamsterState.GreenBeanBuffData.empty(),
                0,
                Optional.empty(),
                0,
                1,
                HamsterState.MiniGameBehaviorData.empty(),
                HamsterState.WanderModeData.empty(),
                0,
                0L,
                0,
                armorVisible);
    }

    private static HamsterState decode(NbtCompound nbt) {
        return HamsterState.getCodec().parse(NbtOps.INSTANCE, nbt).getOrThrow();
    }
}
