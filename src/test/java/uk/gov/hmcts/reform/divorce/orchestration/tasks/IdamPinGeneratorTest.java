package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USERID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;


@RunWith(MockitoJUnitRunner.class)
public class IdamPinGeneratorTest {
    private IdamPinGenerator idamPinGenerator;

    @Mock
    private IdamClient idamClient;
    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext context;
    private Pin pin;
    private TokenExchangeResponse tokenExchangeResponse;
    private AuthenticateUserResponse authenticateUserResponse;

    @Before
    public void setUp() {
        idamPinGenerator = new IdamPinGenerator(idamClient);

        pin = Pin.builder()
                .userId(TEST_USERID)
                .pin(TEST_PIN)
                .build();

        payload = new HashMap<>();
        payload.put(PIN,TEST_PIN );

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() throws TaskException {
        //given
        when(idamClient.createPin(any(), anyString())).thenReturn(pin);
        authenticateUserResponse = AuthenticateUserResponse.builder().code(TEST_CODE).build();
        when(idamClient.authenticateUser(anyString(), anyString(), any(), any()))
                .thenReturn(authenticateUserResponse);
        tokenExchangeResponse = TokenExchangeResponse.builder().accessToken(AUTH_TOKEN).build();
        when(idamClient.exchangeCode(anyString(), anyString(), any(), any(), any()))
                .thenReturn(tokenExchangeResponse);

        //when
        Map<String, Object> response = idamPinGenerator.execute(context, payload);

        //then
        assertNotNull(response);
        assertEquals(pin.getPin(), response.get(PIN));
        assertEquals(pin.getUserId(), response.get(RESPONDENT_LETTER_HOLDER_ID));
    }

    @After
    public void tearDown() {
        idamPinGenerator = null;
    }

}