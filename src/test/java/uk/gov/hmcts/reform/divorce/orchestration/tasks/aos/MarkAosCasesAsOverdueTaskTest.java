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
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseOrchestrationValues;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class MarkAosCasesAsOverdueTaskTest {

    private static final String TEST_GRACE_PERIOD = "12";

    @Mock
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CaseOrchestrationValues caseOrchestrationValues;

    @InjectMocks
    private MarkAosCasesAsOverdueTask classUnderTest;

    @Captor
    private ArgumentCaptor<AosOverdueRequest> argumentCaptor;

    private DefaultTaskContext context;
    private QueryBuilder query;

    @Before
    public void setUp() {
        when(caseOrchestrationValues.getAosOverdueGracePeriod()).thenReturn(TEST_GRACE_PERIOD);

        classUnderTest.init();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        query = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AOS_AWAITING))
            .filter(QueryBuilders.rangeQuery("data.dueDate").lt("now/d-" + TEST_GRACE_PERIOD + "d"))
            .mustNot(QueryBuilders.matchQuery("data.ServedByProcessServer", YES_VALUE));
    }

    @Test
    public void shouldPublishMessagesForEligibleCases() throws TaskException {
        when(cmsElasticSearchSupport.searchCMSCasesWithSingleQuery(eq(AUTH_TOKEN), any())).thenReturn(Stream.of(
            CaseDetails.builder().caseId("123").build(),
            CaseDetails.builder().caseId("456").build()
        ));

        classUnderTest.execute(context, null);

        verify(cmsElasticSearchSupport).searchCMSCasesWithSingleQuery(AUTH_TOKEN, query);
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