package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AcornRingEquipmentTest {

    @Test
    void mutualEquipmentCreatesContract() {
        assertTrue(AcornRingEquipment.hasMutualEquipment(true, true));
    }

    @Test
    void oneSidedEquipmentDoesNotCreateContract() {
        assertFalse(AcornRingEquipment.hasMutualEquipment(true, false));
        assertFalse(AcornRingEquipment.hasMutualEquipment(false, true));
        assertFalse(AcornRingEquipment.hasMutualEquipment(false, false));
    }

    @Test
    void equipmentRemovalEndsOnlyPairsContainingThatPlayer() {
        boolean firstEquipped = true;
        boolean secondEquipped = false;
        boolean thirdEquipped = true;

        assertFalse(AcornRingEquipment.hasMutualEquipment(firstEquipped, secondEquipped));
        assertFalse(AcornRingEquipment.hasMutualEquipment(secondEquipped, thirdEquipped));
        assertTrue(AcornRingEquipment.hasMutualEquipment(firstEquipped, thirdEquipped));
    }
}
