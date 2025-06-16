package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchForCaseByReferenceTest {

    protected static final String CASEWORKER_TOKEN = "caseworkerToken";
    protected static final String CASE_REFERENCE = "1234432112244321";
    @InjectMocks
    SearchForCaseByReference searchForCaseByReference;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;
    @Mock
    private AuthUtil authUtil;

    @Captor
    ArgumentCaptor<String> tokenCaptor;
    @Captor
    ArgumentCaptor<String> queryCaptor;

    @Before
    public void setUpTest() {
        searchForCaseByReference = new SearchForCaseByReference(new SearchForCase(caseMaintenanceClient, authUtil));
    }

    @Test
    public void shouldSearchCaseByEmailAndReturnCaseDetailsWhenFound() {

        when(authUtil.getCaseworkerToken()).thenReturn(CASEWORKER_TOKEN);
        when(caseMaintenanceClient.searchCases(anyString(), anyString()))
            .thenReturn(SearchResult.builder().cases(Collections.singletonList(CaseDetails.builder().caseId("caseId").build())).build());
        Optional<List<CaseDetails>> caseDetails = searchForCaseByReference.searchCasesByCaseReference(CASE_REFERENCE);
        assertThat(caseDetails.isPresent(), equalTo(Boolean.TRUE));
        verify(caseMaintenanceClient).searchCases(tokenCaptor.capture(), queryCaptor.capture());
        assertThat(tokenCaptor.getValue(), equalTo(CASEWORKER_TOKEN));
        assertThat(queryCaptor.getValue(), equalTo("{\"query\":{\"term\":{ \"reference\":\"1234432112244321\"}}}"));

    }

    @Test
    public void shouldSearchCaseByEmailAndReturnOptionalEmptyWhenNotFound() {

        when(authUtil.getCaseworkerToken()).thenReturn(CASEWORKER_TOKEN);
        when(caseMaintenanceClient.searchCases(anyString(), anyString())).thenReturn(SearchResult.builder().cases(Collections.emptyList()).build());

        Optional<List<CaseDetails>> caseDetails = searchForCaseByReference.searchCasesByCaseReference(CASE_REFERENCE);
        assertThat(caseDetails.isEmpty(), equalTo(Boolean.TRUE));
        verify(caseMaintenanceClient).searchCases(tokenCaptor.capture(), queryCaptor.capture());
        assertThat(tokenCaptor.getValue(), equalTo(CASEWORKER_TOKEN));
        assertThat(queryCaptor.getValue(), equalTo("{\"query\":{\"term\":{ \"reference\":\"1234432112244321\"}}}"));
    }
}