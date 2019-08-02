package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCallbackControllerTest {

    @Mock
    SolicitorService solicitorService;

    @InjectMocks
    SolicitorCallbackController classUnderTest;

    @Test
    public void whenSubmitDa_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> divorceSession = Collections.emptyMap();

        when(solicitorService.issuePersonalServicePack(divorceSession, AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(divorceSession);

        ResponseEntity<Map<String, Object>> response = classUnderTest.issuePersonalServicePack(
                AUTH_TOKEN, TEST_CASE_ID, divorceSession
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(divorceSession, response.getBody());

        verify(solicitorService).issuePersonalServicePack(divorceSession, AUTH_TOKEN, TEST_CASE_ID);
    }
}
