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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INITIATED_PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_AMOUNT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_CHANNEL_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_FEE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_FEE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_SITE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_TRANSACTION_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;

@RunWith(MockitoJUnitRunner.class)
public class GetInconsistentPaymentInfoUTest {

    private static final String PAYMENT_EXTERNAL_REFERENCE = "06kd1v30vm45hqvggphdjqbeqa";
    private static final String PAYMENT_REFERENCE = "RC-1547-0733-1813-9545";
    private static final String PAYMENT_AMOUNT = "55000";
    private static final String PAYMENT_SITE_ID = "AA04";
    private static final String PAYMENT_DATE = "09012019";
    private static final String PAYMENT_ID = "1";
    private static final String PAYLOADS_PAYMENT_FROM_CMS_JSON = "/jsonExamples/payloads/paymentFromCMS.json";
    private static final String PAYLOADS_PAYMENT_FROM_PAYMENT_JSON = "/jsonExamples/payloads/paymentSystemPaid.json";

    @InjectMocks
    private GetInconsistentPaymentInfo target;

    @Mock
    private PaymentClient paymentClientMock;
    @Mock
    private AuthTokenGenerator serviceAuthGeneratorMock;
    @Mock
    private TaskCommons taskCommons;
    @Mock
    private Court courtInfo;
    @Mock
    private CcdUtil ccdUtil;

    private TaskContext context;

    @Before
    public void setup() {
        when(ccdUtil.getCurrentDatePaymentFormat()).thenReturn(PAYMENT_DATE);
        when(ccdUtil.mapCCDDateToDivorceDate(notNull())).thenReturn(PAYMENT_DATE);

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
        when(courtInfo.getSiteId()).thenReturn(PAYMENT_SITE_ID);
        when(serviceAuthGeneratorMock.generate()).thenReturn(TEST_SERVICE_TOKEN);
    }

    @Test
    public void givenCaseWithoutSuccessfulPayment_whenSetPaymentRefOnSession_thenReturnSameData() throws Exception {
        Map<String, Object> testData = Maps.newHashMap(EXISTING_PAYMENTS,
            Arrays.asList(ImmutableMap.of(
                PAYMENT_REFERENCE, "ref1",
                PAYMENT_STATUS, "nothing"
            )));

        assertNull(target.execute(context, testData));
        assertTrue(context.hasTaskFailed());
    }

    @Test
    public void givenCaseWithSuccessfulExistingPayment_whenGetPaymentInfo_thenReturnPaymentInfo() throws Exception {
        Map<String, Object> testObjectData = getDraftObject(INITIATED_PAYMENT_STATUS);
        when(taskCommons.getCourt(any())).thenReturn(courtInfo);

        when(paymentClientMock
            .checkPayment(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_TOKEN, PAYMENT_REFERENCE))
            .thenReturn(getPaymentSystemResponse());
        TaskContext taskContext = createTaskContext(TEST_CASE_ID, AWAITING_PAYMENT);

        validateResponse(getExpectedPayment(PAYMENT_DATE),
            target.execute(taskContext, testObjectData));
    }

    @Test
    public void givenCaseWithSuccessfulExistingPayment_whenSetPaymentRefOnSession_thenReturnSessionWithPaymentRef()
        throws Exception {
        Map<String, Object> testObjectData = getDraftObject(SUCCESS_STATUS);

        TaskContext taskContext = createTaskContext(TEST_CASE_ID, AWAITING_PAYMENT);
        Map<String, Object> response = target.execute(taskContext, testObjectData);
        validateResponse(getExpectedPayment(PAYMENT_DATE), response);
    }

    private void validateResponse(Map<String, Object> expectedPayment, Map<String, Object> response) {
        Object payment = response.get(PAYMENT);
        assertNotNull(payment);
        assertThat(expectedPayment, is(payment));
    }

    private Map<String, Object> getExpectedPayment(String expectedDate) {
        return new ImmutableMap.Builder<String, Object>()
            .put(PAYMENT_CHANNEL_KEY, PAYMENT_CHANNEL)
            .put(PAYMENT_TRANSACTION_ID_KEY, PAYMENT_EXTERNAL_REFERENCE)
            .put(PAYMENT_REFERENCE_KEY, PAYMENT_REFERENCE)
            .put(PAYMENT_DATE_KEY, expectedDate)
            .put(PAYMENT_AMOUNT_KEY, PAYMENT_AMOUNT)
            .put(PAYMENT_STATUS_KEY, SUCCESS_PAYMENT_STATUS)
            .put(PAYMENT_FEE_ID_KEY, PAYMENT_FEE_ID)
            .put(PAYMENT_SITE_ID_KEY, PAYMENT_SITE_ID)
            .build();
    }

    private TaskContext createTaskContext(String caseId, String caseState) {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, caseId);
        context.setTransientObject(CASE_STATE_JSON_KEY, caseState);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_SERVICE_AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> getDraftObject(String status) throws Exception {
        Map<String, Object> paymentObject = ObjectMapperTestUtil
            .getJsonFromResourceFile(PAYLOADS_PAYMENT_FROM_CMS_JSON, new TypeReference<HashMap<String, Object>>() {});
        paymentObject.put(PAYMENT_STATUS, status);

        return ImmutableMap.of(
            IS_DRAFT_KEY, Boolean.FALSE.toString(),
            D_8_PAYMENTS, Arrays.asList(ImmutableMap.of(
                ID, PAYMENT_ID,
                PAYMENT_VALUE, paymentObject
            )));
    }

    private Map<String, Object> getPaymentSystemResponse() throws IOException {
        return ObjectMapperTestUtil.getJsonFromResourceFile(PAYLOADS_PAYMENT_FROM_PAYMENT_JSON,
            new TypeReference<HashMap<String, Object>>() {
            });
    }
}
