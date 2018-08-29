package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationControllerTest {

    @Mock
    private CaseOrchestrationService service;

    @InjectMocks
    private OrchestrationController controller;

    @Test
    public void whenPetitionIssued_thenCallbackWorksAsExpected() throws WorkflowException {
        final String authToken = "authtoken";
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        CcdCallbackResponse expected = CcdCallbackResponse.builder().data(new HashMap<>()).build();

        when(service.ccdCallbackHandler(createEvent, authToken)).thenReturn(new HashMap<>());

        ResponseEntity<CcdCallbackResponse> actual = controller.petitionIssuedCallback(authToken, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void whenAosRetrieveCase_thenReturnExpectedCase() throws WorkflowException {
        final String authToken = "authtoken";
        final Map<String, Object> expected = ImmutableMap.of("testKey", "testValue");
        when(service.ccdRetrieveCaseDetailsHandler(true, authToken)).thenReturn(expected);

        ResponseEntity<Map<String, Object>> actual = controller.retrieveAosCase(authToken, true);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }
}