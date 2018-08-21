package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseMaintenanceServiceImplTest {

    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @InjectMocks
    private CaseMaintenanceServiceImpl caseMaintenanceService;

    private String authToken;
    private Map<String, Object> testData;

    @Before
    public void setup() {
        authToken = "authToken";
        testData = new HashMap<>();
        testData.put("id", "test-id");
    }

    @Test
    public void givenCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenReturn(testData);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        assertEquals(testData, caseMaintenanceService.submit(testData, authToken));

        verify(submitToCCDWorkflow).run(testData, authToken);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenReturn(testData);

        Map<String, Object> errors = new HashMap<>();
        errors.put("new_Error", "An Error");
        when(submitToCCDWorkflow.errors()).thenReturn(errors);

        assertEquals(errors, caseMaintenanceService.submit(testData, authToken));

        verify(submitToCCDWorkflow).run(testData, authToken);
        verify(submitToCCDWorkflow, times(2)).errors();
    }

    @Test(expected=WorkflowException.class)
    public void givenCaseDataValid_whenSubmitFails_thenThrowWorkflowException() throws Exception {
        when(submitToCCDWorkflow.run(testData, authToken)).thenThrow(new WorkflowException("An Error"));

        caseMaintenanceService.submit(testData, authToken);

        verify(submitToCCDWorkflow).run(testData, authToken);
    }
}