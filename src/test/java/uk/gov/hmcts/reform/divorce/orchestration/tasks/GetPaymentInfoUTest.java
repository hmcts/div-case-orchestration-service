package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.io.IOException;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SESSION_PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentInfoUTest {
    @InjectMocks
    private GetPaymentInfo target;

    @Mock
    private PaymentClient paymentClientMock;
    @Mock
    private AuthTokenGenerator serviceAuthGeneratorMock;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
    }

    @Test
    public void givenCaseWithoutSuccessfulPayment_whenSetPaymentRefOnSession_thenReturnSameData() {
        Map<String, Object> testData = Maps.newHashMap(EXISTING_PAYMENTS,
            Arrays.asList(ImmutableMap.of(
                PAYMENT_REFERENCE, "ref1",
                PAYMENT_STATUS, "nothing"
            )));

        assertEquals(testData, target.execute(context, testData));
    }

    @Test
    public void givenCaseWithFailedPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object> testData = Maps.newHashMap(PAYMENT,
            ImmutableMap.of(
                PAYMENT_REFERENCE, "ref2",
                PAYMENT_STATUS, "fail"
            )
        );

        Map<String, Object> resultData = new HashMap<>(testData);
        assertEquals(resultData, target.execute(context, testData));
    }

    @Test
    public void givenCaseWithSuccessfulPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object> testData = Maps.newHashMap(PAYMENT,
            ImmutableMap.of(
                PAYMENT_REFERENCE, "ref2",
                PAYMENT_STATUS, SUCCESS_STATUS
            )
        );

        Map<String, Object> resultData = new HashMap<>(testData);
        resultData.put(SESSION_PAYMENT_REFERENCE, "ref2");

        assertEquals(resultData, target.execute(context, testData));
    }


    @Test
    public void givenCaseWithSuccessfulExistingPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef() {
        Map<String, Object> testData = Maps.newHashMap(EXISTING_PAYMENTS,
            Arrays.asList(ImmutableMap.of(
                PAYMENT_VALUE, ImmutableMap.of(
                    PAYMENT_REFERENCE, "ref1",
                    PAYMENT_STATUS, "nothing"
                )
                ),
                ImmutableMap.of(PAYMENT_VALUE, ImmutableMap.of(
                    PAYMENT_REFERENCE, "ref2",
                    PAYMENT_STATUS, SUCCESS_STATUS
                    )
                )
            ));

        Map<String, Object> testObjectData = getDraftObject();
        Map<String, Object> resultData = new HashMap<>(testData);
        resultData.put(SESSION_PAYMENT_REFERENCE, "ref2");
        assertEquals(resultData, target.execute(context, testObjectData));
    }

    private TaskContext createTaskContext(String caseId, String caseState) {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);
        context.setTransientObject(CASE_STATE_JSON_KEY, caseState);
        return context;
    }

    private Map<String, Object> getDraftObject() {
        try {
            return ObjectMapperTestUtil.getJsonFromString(jsonElem, new TypeReference<HashMap<String,Object>>() {});
        } catch (IOException e) {
            return null;
        }
    }

    private static final String jsonElem = "{\n" +
        "\t\"id\": 1547073120300616,\n" +
        "\t\"state\": \"AwaitingPayment\",\n" +
        "\t\"case_data\": {\n" +
        "\t\t\"Payments\": [{\n" +
        "\t\t\t\"id\": \"0e4fdf6b-2895-4a8a-a718-14cad7248978\",\n" +
        "\t\t\t\"value\": {\n" +
        "\t\t\t\t\"PaymentDate\": \"2019-01-09\",\n" +
        "\t\t\t\t\"PaymentFeeId\": \"FEE0002\",\n" +
        "\t\t\t\t\"PaymentAmount\": null,\n" +
        "\t\t\t\t\"PaymentSiteId\": \"2\",\n" +
        "\t\t\t\t\"PaymentStatus\": \"Initiated\",\n" +
        "\t\t\t\t\"PaymentChannel\": \"online\",\n" +
        "\t\t\t\t\"PaymentReference\": \"RC-1547-0733-1813-9545\",\n" +
        "\t\t\t}\n" +
        "\t\t\t\t\"PaymentTransactionId\": null\n" +
        "\t\t}]\n" +
        "\t}\n" +
        "}";


}
