package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @InjectMocks
    private BulkCaseController classUnderTest;


    @Test
    public void whenSearchCases_thenReturnExpectedResponse() throws WorkflowException {
        Map<String, Object> expected = Collections.emptyMap();

        when(caseOrchestrationService.generateBulkCaseForListing()).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = classUnderTest.createBulkCase();

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expected));

    }

    @Test
    public void whenScheduleCases_thenReturnExpectedResponse() throws WorkflowException {
        CcdCallbackRequest request = CcdCallbackRequest.builder().build();
        String authToken = "authToken";

        when(caseOrchestrationService.processBulkCaseScheduleForHearing(request, authToken))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.scheduleBulkCaseForHearing(authToken, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().build()));
    }
}
