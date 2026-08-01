package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HamsterCombatUtilTest {

    private static final UUID CONFLICT_TARGET = UUID.fromString("8f6f12af-e659-44d7-8e18-7df6cbcd74f9");
    private static final UUID HAMSTER_OWNER = UUID.fromString("e85723de-9bf3-45b5-98e8-6d2749ab8240");
    private static final UUID CONTRACTED_OWNER = UUID.fromString("faee1d44-6758-4ee7-82f2-4612282a526d");

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

    @Test
    void mutualRingEquipmentProtectsAnotherOwnersTargets() {
        assertTrue(
                HamsterCombatUtil.isContractProtected(
                        HAMSTER_OWNER, CONTRACTED_OWNER, true, true));
    }

    @Test
    void oneSidedRingEquipmentDoesNotProtectTargets() {
        assertFalse(
                HamsterCombatUtil.isContractProtected(
                        HAMSTER_OWNER, CONTRACTED_OWNER, true, false));
        assertFalse(
                HamsterCombatUtil.isContractProtected(
                        HAMSTER_OWNER, CONTRACTED_OWNER, false, true));
    }

    @Test
    void sameOwnerRelationshipUsesExistingProtectionInsteadOfAContract() {
        assertFalse(
                HamsterCombatUtil.isContractProtected(
                        HAMSTER_OWNER, HAMSTER_OWNER, true, true));
    }
}
