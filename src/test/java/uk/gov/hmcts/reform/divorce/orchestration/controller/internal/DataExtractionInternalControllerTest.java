package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionInternalControllerTest {

    @Mock
    private DataExtractionService dataExtractionService;

    @InjectMocks
    private DataExtractionInternalController classUnderTest;

    @Test
    public void shouldRequestDataExtractionForFamilyMan() throws CaseOrchestrationServiceException {
        classUnderTest.startDataExtractionToFamilyMan();

        verify(dataExtractionService).requestDataExtractionForPreviousDay();
    }

}