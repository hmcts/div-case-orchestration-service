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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;


@RunWith(MockitoJUnitRunner.class)
public class RespondentPinGeneratorTest {

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private RespondentPinGenerator respondentPinGenerator;

    private Map<String, Object> caseData;
    private TaskContext context;
    private Pin pin;

    @Before
    public void setUp() {
        pin = Pin.builder()
                .userId(TEST_USER_ID)
                .pin(TEST_PIN)
                .build();

        caseData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldPopulatePinAndLetterHolderId() {
        //given
        when(idamClient.createPin(any(), anyString())).thenReturn(pin);
        when(authUtil.getCitizenToken())
            .thenReturn(BEARER_AUTH_TOKEN);

        //when
        Map<String, Object> response = respondentPinGenerator.execute(context, caseData);

        //then
        assertThat(response, is(notNullValue()));
        assertThat(response.get(RESPONDENT_LETTER_HOLDER_ID), is(pin.getUserId()));
        assertThat(context.getTransientObject(RESPONDENT_PIN), is(pin.getPin()));
    }

    @After
    public void tearDown() {
        respondentPinGenerator = null;
    }

}
