package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetClaimCostsFromTest {

    private static final String EXPECTED_VALUE = DIVORCE_COSTS_CLAIM_FROM_CCD_CODE_FOR_RESPONDENT;

    @InjectMocks
    private SetClaimCostsFrom setClaimCostsFrom;


    @Test
    public void testSetsClaimCostsFromToRespondent() {
        HashMap<String, Object> payload = new HashMap<>();

        setClaimCostsFrom.execute(null, payload);

        assertEquals(EXPECTED_VALUE, payload.get(DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD));
    }
}