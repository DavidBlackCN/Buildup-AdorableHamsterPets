package net.dawson.adorablehamsterpets.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class HamsterDietUtilTest {

    @Test
    void everyAcceptedAggressionToggleConsumesItsItem() {
        List<HamsterDietUtil.AggressionToggleResult> acceptedResults =
                Arrays.stream(HamsterDietUtil.AggressionToggleResult.values())
                        .filter(HamsterDietUtil.AggressionToggleResult::isAccepted)
                        .toList();

        assertEquals(
                List.of(HamsterDietUtil.AggressionToggleResult.ACCEPTED_WITH_CONSUMPTION),
                acceptedResults);
    }

}
