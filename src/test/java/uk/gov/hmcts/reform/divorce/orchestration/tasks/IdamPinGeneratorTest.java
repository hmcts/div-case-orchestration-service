package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;


@RunWith(MockitoJUnitRunner.class)
public class IdamPinGeneratorTest {

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private IdamPinGenerator idamPinGenerator;

    private Map<String, Object> payload;
    private TaskContext context;
    private Pin pin;

    @Before
    public void setUp() {
        pin = Pin.builder()
                .userId(TEST_USER_ID)
                .pin(TEST_PIN)
                .build();

        payload = new HashMap<>();
        payload.put(PIN,TEST_PIN );

        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() {
        //given
        when(idamClient.createPin(any(), anyString())).thenReturn(pin);
        when(authUtil.getIdamOauth2Token(any(), any()))
                .thenReturn(BEARER_AUTH_TOKEN);

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