package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HamsterInteractionGestureUtilTest {

    @Test
    void pacifistItemsUseTheNormalFeedingGesture() {
        assertTrue(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        false, true, false, false));
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        true, true, false, false));
    }

    @Test
    void pacifistClassificationWinsWhenConfiguredListsOverlap() {
        assertTrue(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        false, true, true, true));
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        true, true, true, true));
    }

    @Test
    void standardAndMenaceItemsUseTheNormalFeedingGesture() {
        assertTrue(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        false, false, true, false));
        assertTrue(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        false, false, false, true));
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        true, false, true, false));
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        true, false, false, true));
    }

    @Test
    void nonAggressionItemsNeverUseTheToggleGesture() {
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        false, false, false, false));
        assertFalse(
                HamsterInteractionGestureUtil.isAggressionToggleGesture(
                        true, false, false, false));
    }

    @Test
    void flowersEquipWhileSneaking() {
        assertTrue(HamsterInteractionGestureUtil.isAccessoryEquipGesture(true, true));
        assertFalse(HamsterInteractionGestureUtil.isAccessoryEquipGesture(false, true));
    }

    @Test
    void nonFlowerAccessoriesRetainNormalUse() {
        assertTrue(HamsterInteractionGestureUtil.isAccessoryEquipGesture(false, false));
        assertFalse(HamsterInteractionGestureUtil.isAccessoryEquipGesture(true, false));
    }

    @Test
    void activeCombatEndsInStandardInsteadOfPacifist() {
        assertEquals(
                HamsterInteractionGestureUtil.PacifistItemAction.END_FIGHT_IN_STANDARD,
                HamsterInteractionGestureUtil.resolvePacifistItemAction(false, true));
    }

    @Test
    void calmNonPacifistHamstersEnterPacifist() {
        assertEquals(
                HamsterInteractionGestureUtil.PacifistItemAction.ENABLE_PACIFIST,
                HamsterInteractionGestureUtil.resolvePacifistItemAction(false, false));
    }

    @Test
    void existingPacifistModeFallsThroughToFeeding() {
        assertEquals(
                HamsterInteractionGestureUtil.PacifistItemAction.FALL_THROUGH,
                HamsterInteractionGestureUtil.resolvePacifistItemAction(true, false));
        assertEquals(
                HamsterInteractionGestureUtil.PacifistItemAction.FALL_THROUGH,
                HamsterInteractionGestureUtil.resolvePacifistItemAction(true, true));
    }
}
