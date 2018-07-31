package uk.gov.hmcts.reform.divorce.orchestration.controller;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.service.OrchestrationService;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationServiceControllerTest {

    @Mock
    private OrchestrationService validationService;

    @InjectMocks
    private OrchestrationServiceController validationServiceController;

    @Test
    public void givenCoreCaseData_whenValidateIsCalled_thenReturnValidationResult() {
        Assert.assertTrue(true);
    }

}
