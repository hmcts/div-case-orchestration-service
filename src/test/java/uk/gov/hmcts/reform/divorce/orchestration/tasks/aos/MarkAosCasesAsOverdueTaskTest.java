package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class MarkAosCasesAsOverdueTaskTest {

    private static final String DEFAULT_GRACE_PERIOD = "29";

    @Mock
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private MarkAosCasesAsOverdueTask classUnderTest;

    @Captor
    private ArgumentCaptor<AosOverdueRequest> argumentCaptor;

    private DefaultTaskContext context;
    private QueryBuilder[] queryBuilders;

    @Before
    public void setUp() {
        classUnderTest.init();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AOS_AWAITING);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery("data.dueDate").lt("now/d-" + DEFAULT_GRACE_PERIOD + "d");
        queryBuilders = new QueryBuilder[] {stateQuery, dateFilter};
    }

    @Test
    public void shouldPublishMessagesForEligibleCases() throws TaskException {
        when(cmsElasticSearchSupport.searchCMSCases(eq(AUTH_TOKEN), any(), any())).thenReturn(Stream.of(
            CaseDetails.builder().caseId("123").build(),
            CaseDetails.builder().caseId("456").build()
        ));

        classUnderTest.execute(context, null);

        verify(cmsElasticSearchSupport).searchCMSCases(AUTH_TOKEN, queryBuilders);
        verify(applicationEventPublisher, times(2)).publishEvent(argumentCaptor.capture());
        List<AosOverdueRequest> requestMessages = argumentCaptor.getAllValues();
        assertThat(requestMessages, hasSize(2));
        assertRequestMessage(requestMessages.get(0), "123");
        assertRequestMessage(requestMessages.get(1), "456");
    }

    private void assertRequestMessage(AosOverdueRequest requestMessage, String expectedCaseId) {
        assertThat(requestMessage, instanceOf(ApplicationEvent.class));
        assertThat(requestMessage.getCaseId(), is(expectedCaseId));
    }

}