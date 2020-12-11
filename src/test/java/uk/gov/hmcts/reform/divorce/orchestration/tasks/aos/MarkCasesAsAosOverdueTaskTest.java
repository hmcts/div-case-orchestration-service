package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.elasticsearch.index.query.Operator;
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
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForAlternativeMethodCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForProcessServerCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseOrchestrationValues;
import uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport;
import uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchUtils;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class MarkCasesAsAosOverdueTaskTest {

    private static final String TEST_GRACE_PERIOD = "12";
    private static final String TWO_STATES_ELASTIC_SEARCH_OR_STATEMENT = AOS_AWAITING + " " + AOS_STARTED;

    @Mock
    private CMSElasticSearchSupport mockCmsElasticSearchSupport;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CaseOrchestrationValues caseOrchestrationValues;

    @InjectMocks
    private MarkCasesAsAosOverdueTask classUnderTest;

    @Captor
    private ArgumentCaptor<ApplicationEvent> argumentCaptor;

    private DefaultTaskContext context;
    private QueryBuilder query;

    @Before
    public void setUp() {
        when(caseOrchestrationValues.getAosOverdueGracePeriod()).thenReturn(TEST_GRACE_PERIOD);

        classUnderTest.init();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        query = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, TWO_STATES_ELASTIC_SEARCH_OR_STATEMENT).operator(Operator.OR))
            .filter(QueryBuilders.rangeQuery("data.dueDate").lt("now/d-" + TEST_GRACE_PERIOD + "d"));
    }

    @Test
    public void shouldPublishMessagesForEligibleCases() throws TaskException {
        CMSElasticSearchUtils.mockCMSElasticSearchSupportToProduceIteratorWithCaseDetailsBatches(mockCmsElasticSearchSupport, AUTH_TOKEN, query,
            asList(
                CaseDetails.builder().state(AOS_STARTED).caseId("1").build(),
                CaseDetails.builder().state(AOS_AWAITING).caseId("2").build()
            ),
            asList(
                CaseDetails.builder().state(AOS_AWAITING).caseId("3").caseData(Map.of(SERVED_BY_PROCESS_SERVER, YES_VALUE)).build(),
                CaseDetails.builder().state(AOS_STARTED).caseId("4").caseData(Map.of(SERVED_BY_PROCESS_SERVER, YES_VALUE)).build()
            ),
            asList(
                CaseDetails.builder().state(AOS_AWAITING).caseId("5").caseData(Map.of(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)).build(),
                CaseDetails.builder().state(AOS_STARTED).caseId("6").caseData(Map.of(SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE)).build()
            )
        );

        classUnderTest.execute(context, null);

        int amountOfEventsExpectedToBePublished = 5;
        verify(applicationEventPublisher, times(amountOfEventsExpectedToBePublished)).publishEvent(argumentCaptor.capture());
        List<ApplicationEvent> publishedEvents = argumentCaptor.getAllValues();
        assertThat(publishedEvents, hasSize(amountOfEventsExpectedToBePublished));
        assertPublishedEvent(publishedEvents.get(0), "2", AosOverdueEvent.class);
        assertPublishedEvent(publishedEvents.get(1), "3", AosOverdueForProcessServerCaseEvent.class);
        assertPublishedEvent(publishedEvents.get(2), "4", AosOverdueForProcessServerCaseEvent.class);
        assertPublishedEvent(publishedEvents.get(3), "5", AosOverdueForAlternativeMethodCaseEvent.class);
        assertPublishedEvent(publishedEvents.get(4), "6", AosOverdueForAlternativeMethodCaseEvent.class);
    }

    private void assertPublishedEvent(ApplicationEvent requestMessage, String expectedCaseId, Class<? extends ApplicationEvent> expectedEventType) {
        assertThat(requestMessage.getClass().getName(), is(expectedEventType.getName()));
        assertThat(requestMessage, hasProperty("caseId", is(expectedCaseId)));
    }

}