package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.elasticsearch.index.query.QueryBuilders;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCases.buildCoolOffPeriodInDNBoundary;

@RunWith(MockitoJUnitRunner.class)
public class SearchDNPronouncedCasesTest {

    private static String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    @Mock
    private CMSElasticSearchSupport mockCmsElasticSearchSupport;

    @InjectMocks
    private SearchDNPronouncedCases classUnderTest;

    private int pageSize;
    private int start;

    private static final String TWO_MINUTES = "2m";
    private String timeSinceDNWasPronounced = TWO_MINUTES;

    private DefaultTaskContext contextBeingModified;

    @Before
    public void setupTaskContextWithSearchSettings() {
        pageSize = 10;
        start = 0;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {
            {
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
                put(AWAITING_DA_PERIOD_KEY, timeSinceDNWasPronounced);
            }
        };

        contextBeingModified = new DefaultTaskContext();
        contextBeingModified.setTransientObjects(searchSettings);
    }

    @Test
    public void shouldReturnNoResultIfNoneIsReturnedFromElasticSearchSupport() throws TaskException {
        final List<String> expectedCaseIdsInTheContext = Collections.emptyList();
        when(mockCmsElasticSearchSupport.searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN), any(), any())).thenReturn(Stream.empty());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(mockCmsElasticSearchSupport).searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN),
            eq(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED)),
            eq(QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodInDNBoundary(timeSinceDNWasPronounced))));
    }

    @Test
    public void shouldReturnResults() throws TaskException {
        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5");
        when(mockCmsElasticSearchSupport.searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN), any(), any())).thenReturn(buildCases(5));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(mockCmsElasticSearchSupport).searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN),
            eq(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED)),
            eq(QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodInDNBoundary(timeSinceDNWasPronounced))));
    }

    @Test
    public void shouldRethrowFeignException() throws TaskException {
        when(mockCmsElasticSearchSupport.searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN), any(), any()))
            .thenThrow(new FeignException.BadRequest("Bad test request", "".getBytes()));

        Map<String, Object> actualResult = null;
        try {
            actualResult = classUnderTest.execute(contextBeingModified, null);
        } catch (FeignException fException) {
            assertThat(fException.status(), CoreMatchers.is(HttpStatus.BAD_REQUEST.value()));
            assertThat(fException.getMessage(), CoreMatchers.is("Bad test request"));
        }

        assertNull(actualResult);
        verify(mockCmsElasticSearchSupport).searchCMSCases(eq(start), eq(pageSize), eq(AUTH_TOKEN),
            eq(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED)),
            eq(QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodInDNBoundary(timeSinceDNWasPronounced))));
    }

    private Stream<CaseDetails> buildCases(int caseCount) {
        Stream.Builder<CaseDetails> streamBuilder = Stream.builder();

        for (int i = 1; i <= caseCount; i++) {
            streamBuilder.add(CaseDetails.builder().caseId(String.valueOf(i)).build());
        }

        return streamBuilder.build();
    }

}
