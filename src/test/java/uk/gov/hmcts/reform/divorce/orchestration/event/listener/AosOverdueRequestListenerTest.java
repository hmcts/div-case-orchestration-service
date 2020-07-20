package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AosOverdueRequestListenerTest {

    @Mock
    private AosService aosService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AosOverdueRequestListener classUnderTest;

    @Test
    public void shouldCallServiceWhenRequestIsListened() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);

        classUnderTest.onApplicationEvent(new AosOverdueRequest(this, "123"));

        verify(aosService).makeCaseAosOverdue(AUTH_TOKEN, "123");
    }

}