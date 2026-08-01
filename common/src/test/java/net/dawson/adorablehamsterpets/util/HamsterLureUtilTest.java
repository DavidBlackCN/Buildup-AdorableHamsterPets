package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HamsterLureUtilTest {

    @Test
    void dietaryItemsTriggerTemptation() {
        assertTrue(HamsterLureUtil.shouldTempt(true, false));
    }

    @Test
    void shoulderMountItemsTriggerTemptationWithoutBeingDietary() {
        assertTrue(HamsterLureUtil.shouldTempt(false, true));
    }

    @Test
    void unrelatedItemsDoNotTriggerTemptation() {
        assertFalse(HamsterLureUtil.shouldTempt(false, false));
    }

    @Test
    void tamingFoodTriggersBegging() {
        assertTrue(HamsterLureUtil.shouldBeg(true, false));
    }

    @Test
    void shoulderMountFoodTriggersBegging() {
        assertTrue(HamsterLureUtil.shouldBeg(false, true));
    }

    @Test
    void overlappingBeggingCategoriesStillTriggerBegging() {
        assertTrue(HamsterLureUtil.shouldBeg(true, true));
    }

    @Test
    void dietaryOnlyFoodDoesNotTriggerBegging() {
        assertFalse(HamsterLureUtil.shouldBeg(false, false));
    }
}
