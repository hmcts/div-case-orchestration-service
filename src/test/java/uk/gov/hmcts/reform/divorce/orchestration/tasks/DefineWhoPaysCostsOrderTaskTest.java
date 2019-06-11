package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class DefineWhoPaysCostsOrderTaskTest {

    private static final String WHO_PAYS_COSTS_CCD_FIELD = "WhoPaysCosts";
    private static final String WHO_PAYS_CCD_CODE_FOR_RESPONDENT = "respondent";

    @Test
    public void testRespondentShouldPayByDefault() {
        DefineWhoPaysCostsOrderTask defineWhoPaysCostsOrderTask = new DefineWhoPaysCostsOrderTask();

        Map<String, Object> returnedPayload = defineWhoPaysCostsOrderTask.execute(null, emptyMap());

        assertThat(returnedPayload, hasEntry(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_RESPONDENT));
    }

    @Test
    public void testPartyChosenToPay_RemainsChosen() {
        DefineWhoPaysCostsOrderTask defineWhoPaysCostsOrderTask = new DefineWhoPaysCostsOrderTask();

        Map<String, Object> inputPayload = singletonMap(WHO_PAYS_COSTS_CCD_FIELD, "someoneElse");
        Map<String, Object> returnedPayload = defineWhoPaysCostsOrderTask.execute(null, inputPayload);

        assertThat(returnedPayload, hasEntry(WHO_PAYS_COSTS_CCD_FIELD, "someoneElse"));
    }

}