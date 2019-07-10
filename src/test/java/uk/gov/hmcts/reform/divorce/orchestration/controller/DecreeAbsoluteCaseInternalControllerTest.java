package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteCaseInternalControllerTest {

    private static final String TEST_AUTHORISATION_TOKEN = "testAuthorisationToken";

    @Mock
    DecreeAbsoluteService decreeAbsoluteService;

    @InjectMocks
    DecreeAbsoluteCaseInternalController classUnderTest;

    @Test
    public void testServiceIsCalled() throws WorkflowException {
        ResponseEntity<String> response = classUnderTest.makeCasesEligibleForDA(TEST_AUTHORISATION_TOKEN);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getBody(), is("Cases made eligible for DA: 0"));
        verify(decreeAbsoluteService).enableCaseEligibleForDecreeAbsolute("testAuthorisationToken");
    }

}