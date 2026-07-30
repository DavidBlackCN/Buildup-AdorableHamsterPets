package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class HamsterInventoryUtilTest {

    @Test
    void mapsOccupiedSlotsToIndependentCheekStates() {
        assertOccupancy(Set.of(), false, false);
        assertOccupancy(Set.of(0), true, false);
        assertOccupancy(Set.of(2), true, false);
        assertOccupancy(Set.of(3), false, true);
        assertOccupancy(Set.of(5), false, true);
        assertOccupancy(Set.of(1, 4), true, true);
    }

    private static void assertOccupancy(
            Set<Integer> occupiedSlots, boolean expectedLeft, boolean expectedRight) {
        HamsterInventoryUtil.CheekOccupancy actual =
                HamsterInventoryUtil.resolveCheekOccupancy(occupiedSlots::contains);

        assertEquals(
                new HamsterInventoryUtil.CheekOccupancy(expectedLeft, expectedRight), actual);
    }
}
