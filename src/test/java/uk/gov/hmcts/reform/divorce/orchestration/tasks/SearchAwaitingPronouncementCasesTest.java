package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class SearchAwaitingPronouncementCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchAwaitingPronouncementCases classUnderTest;

    @Test
    public void givenCasesExists_whenSearchCases_thenReturnExpectedOutput() throws TaskException {
        String query = "{\"from\":0,\"size\":0,\"query\":{\"bool\":{\"must\":[{\"match\":{\"state\":{\"query\":\"AwaitingPronouncement\",\"operator\":\"OR\",\"prefix_length\":0,\"max_expansions\":50,\"fuzzy_transpositions\":true,\"lenient\":false,\"zero_terms_query\":\"NONE\",\"auto_generate_synonyms_phrase_query\":true,\"boost\":1.0}}}],\"must_not\":[{\"exists\":{\"field\":\"data.hearingDate\",\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}";
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);

        final SearchResult cmsSearchResponse =
                SearchResult.builder()
                    .build();

        Mockito.when(caseMaintenanceClient.searchCases(AUTH_TOKEN, query)).thenReturn(cmsSearchResponse);

        SearchResult actual = classUnderTest.execute(context, null);

        assertEquals(cmsSearchResponse, actual);

        verify(caseMaintenanceClient).searchCases(AUTH_TOKEN, query);
    }

}
