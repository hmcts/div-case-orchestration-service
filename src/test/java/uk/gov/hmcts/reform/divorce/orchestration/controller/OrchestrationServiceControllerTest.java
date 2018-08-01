package uk.gov.hmcts.reform.divorce.orchestration.controller;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.service.OrchestrationService;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationServiceControllerTest {

    @Mock
    private OrchestrationService orchestraionService;

    @InjectMocks
    private OrchestrationServiceController orchestrationServiceController;

    @Test
    public void givenCoreCaseData_whenOrchestrateIsCalled_thenReturnResult() {
        Assert.assertTrue(true);
    }

}
