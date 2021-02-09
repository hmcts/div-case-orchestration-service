package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AosInternalControllerTest {

    @Mock
    private AosService aosService;

    @InjectMocks
    private AosInternalController controller;

    @Test
    public void shouldCallRightServiceMethod() throws CaseOrchestrationServiceException {
        ResponseEntity<String> response = controller.markCasesForBeingMovedToAosOverdue(AUTH_TOKEN);

        verify(aosService).findCasesForWhichAosIsOverdue(AUTH_TOKEN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

}