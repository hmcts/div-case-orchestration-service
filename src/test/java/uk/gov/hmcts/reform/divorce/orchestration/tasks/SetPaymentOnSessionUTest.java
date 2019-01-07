package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SESSION_PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;

@RunWith(MockitoJUnitRunner.class)
public class SetPaymentOnSessionUTest {
    @InjectMocks
    private SetPaymentOnSession setPaymentOnSession;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
    }

    @Test
    public void givenCaseWithoutSuccessfulPayment_whenSetPaymentRefOnSession_thenReturnSameData() {
        Map<String, Object>  testData = Maps.newHashMap(EXISTING_PAYMENTS,
                Arrays.asList(ImmutableMap.of(
                      PAYMENT_REFERENCE, "ref1",
                      PAYMENT_STATUS, "nothing"
                )));

        assertEquals(testData, setPaymentOnSession.execute(context, testData));
    }

    @Test
    public void givenCaseWithFailedPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object>  testData = Maps.newHashMap(PAYMENT,
                ImmutableMap.of(
                        PAYMENT_REFERENCE, "ref2",
                        PAYMENT_STATUS, "fail"
                )
        );

        Map<String, Object> resultData = new HashMap<>(testData);
        assertEquals(resultData, setPaymentOnSession.execute(context, testData));
    }

    @Test
    public void givenCaseWithSuccessfulPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object>  testData = Maps.newHashMap(PAYMENT,
                ImmutableMap.of(
                        PAYMENT_REFERENCE, "ref2",
                        PAYMENT_STATUS, SUCCESS_STATUS
                )
        );

        Map<String, Object> resultData = new HashMap<>(testData);
        resultData.put(SESSION_PAYMENT_REFERENCE, "ref2");

        assertEquals(resultData, setPaymentOnSession.execute(context, testData));
    }


    @Test
    public void givenCaseWithSuccessfulExistingPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object>  testData = Maps.newHashMap(EXISTING_PAYMENTS,
                Arrays.asList(ImmutableMap.of(
                            PAYMENT_REFERENCE, "ref1",
                            PAYMENT_STATUS, "nothing"
                        ),
                        (ImmutableMap.of(
                                PAYMENT_REFERENCE, "ref2",
                                PAYMENT_STATUS, SUCCESS_STATUS
                        )

                ))
        );

        Map<String, Object> resultData = new HashMap<>(testData);
        resultData.put(SESSION_PAYMENT_REFERENCE, "ref2");

        assertEquals(resultData, setPaymentOnSession.execute(context, testData));
    }
}
