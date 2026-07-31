package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HamsterCombatUtilTest {

    private static final UUID CONFLICT_TARGET = UUID.fromString("8f6f12af-e659-44d7-8e18-7df6cbcd74f9");

    @Test
    void currentTargetIsEngagedWithoutAnActiveWindow() {
        assertTrue(
                HamsterCombatUtil.isConflictEngaged(
                        CONFLICT_TARGET, null, 600L, Long.MIN_VALUE));
    }

    @Test
    void targetlessUnexpiredConflictWindowRemainsEngaged() {
        assertTrue(
                HamsterCombatUtil.isConflictEngaged(
                        null, CONFLICT_TARGET, 599L, 600L));
    }

    @Test
    void targetlessExpiredConflictWindowIsIdle() {
        assertFalse(
                HamsterCombatUtil.isConflictEngaged(
                        null, CONFLICT_TARGET, 600L, 600L));
    }

    @Test
    void unrelatedOwnerCombatCannotMakeAnIdleHamsterEngaged() {
        assertFalse(
                HamsterCombatUtil.isConflictEngaged(
                        null, null, 100L, Long.MIN_VALUE));
    }
}
