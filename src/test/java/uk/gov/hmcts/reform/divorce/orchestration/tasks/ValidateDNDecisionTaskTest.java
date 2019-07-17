package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class ValidateDNDecisionTaskTest {

    private static final String COST_DECISION_NOT_ALLOWED = "Cost decision can only be made if cost has been requested";
    private static final String COST_DECISION_EXPECTED = "Cost decision expected";
    private static final String NO_ERROR = EMPTY_STRING;
    private static final String ANY = "anyString";

    private static final String CLAIM_COST_YES = YES_VALUE;
    private static final String CLAIM_COST_NO = NO_VALUE;
    private static final String CLAIM_COST_DN_YES = YES_VALUE;
    private static final String CLAIM_COST_DN_END = "endClaim";
    private static final String CLAIM_COST_GRANTED_YES = YES_VALUE;
    private static final String CLAIM_COST_DN_EMPTY = StringUtils.EMPTY;
    private static final String DN_GRANTED_YES = YES_VALUE;
    private static final String DN_GRANTED_NO = NO_VALUE;

    private final String claimCostValue;
    private final String claimCostDNValue;
    private final String dnGranted;
    private final String claimCostGranted;
    private final String exceptionMessage;

    private ValidateDNDecisionTask classToTest;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: claimCost: {0}, claimCostDN:{1}, dnGranted:{2}, claimCostGranted:{3}, exception:{4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {null, null, null, null, NO_ERROR},
            {CLAIM_COST_YES, null, DN_GRANTED_YES, CLAIM_COST_GRANTED_YES, NO_ERROR},
            { ANY, ANY, DN_GRANTED_NO, CLAIM_COST_DN_EMPTY, NO_ERROR},
            { CLAIM_COST_YES, CLAIM_COST_DN_YES, DN_GRANTED_YES, CLAIM_COST_DN_EMPTY, COST_DECISION_EXPECTED},
            { CLAIM_COST_YES, CLAIM_COST_DN_YES, DN_GRANTED_YES, CLAIM_COST_GRANTED_YES, NO_ERROR},
            { CLAIM_COST_NO, CLAIM_COST_DN_YES, DN_GRANTED_YES, CLAIM_COST_GRANTED_YES, COST_DECISION_NOT_ALLOWED},
            { CLAIM_COST_YES, CLAIM_COST_DN_END, DN_GRANTED_YES, CLAIM_COST_GRANTED_YES, COST_DECISION_NOT_ALLOWED}
        });
    }


    @Before
    public void setup() {
        classToTest = new ValidateDNDecisionTask();
    }

    @Test
    public void testClaimCostValidation() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(DN_COSTS_CLAIM_CCD_FIELD, claimCostDNValue);
        payload.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, claimCostValue);
        payload.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, claimCostGranted);
        payload.put(DECREE_NISI_GRANTED_CCD_FIELD, dnGranted);

        if (StringUtils.isNotBlank(exceptionMessage)) {
            expectedException.expect(TaskException.class);
            expectedException.expectMessage(is(exceptionMessage));
        }

        classToTest.execute(new DefaultTaskContext(), payload);
    }
}