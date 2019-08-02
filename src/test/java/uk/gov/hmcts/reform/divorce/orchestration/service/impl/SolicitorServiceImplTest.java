package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssuePersonalServicePackWorkflow;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorServiceImplTest {

    @Mock
    IssuePersonalServicePackWorkflow issuePersonalServicePack;

    @InjectMocks
    SolicitorServiceImpl solicitorService;

    @Test
    public void testIssuePersonalServicePack() {

        solicitorService.issuePersonalServicePack(Collections.emptyMap(), "token", "123");

        verify(issuePersonalServicePack).run();
    }
}
