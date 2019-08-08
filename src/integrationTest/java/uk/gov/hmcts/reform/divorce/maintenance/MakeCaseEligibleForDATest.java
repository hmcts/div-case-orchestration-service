package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ES_CASE_ID_KEY;

import static uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory.buildCMSBooleanSearchSource;

@Slf4j
public class MakeCaseEligibleForDATest extends RetrieveCaseSupport {

    private static final String TEST_DN_PRONOUNCED = "testDNPronounced";
    private static final String CMS_URL_SEARCH = "/casemaintenance/version/1/search";
    private static final String STATE_KEY = "state";

    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String NO_STATE_CHANGE_EVENT_ID = "paymentReferenceGenerated";

    private static final String DECREE_NISI_GRANTED_DATE_KEY = "DecreeNisiGrantedDate";
    private static final String DECREE_NISI_GRANTED_DATE = "2019-03-31";

    @Value("${case.orchestration.jobScheduler.make-case-eligible-for-da.context-path}")
    private String jobSchedulerContextPath;

    @Value("${case_maintenance.api.url}")
    private String cmsBaseUrl;

    @Autowired
    private CmsClientSupport cmsClientSupport;

    @Test
    public void givenCaseIsInDNPronounced_WhenMakeCaseEligibleForDAIsCalled_CaseStateIsAwaitingDecreeAbsolute() {
        final UserDetails citizenUser = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, citizenUser,
            Pair.of(D_8_PETITIONER_EMAIL, citizenUser.getEmailAddress()));

        final String caseId = String.valueOf(caseDetails.getId());
        log.debug("Case " + caseId + " created.");

        updateCase(caseId, null, NO_STATE_CHANGE_EVENT_ID,
            ImmutablePair.of(DECREE_NISI_GRANTED_DATE_KEY, DECREE_NISI_GRANTED_DATE));
        log.debug("{}={} updated in the case {}",  DECREE_NISI_GRANTED_DATE_KEY, DECREE_NISI_GRANTED_DATE, caseId);

        updateCaseForCitizen(caseId, null, TEST_DN_PRONOUNCED, citizenUser);
        log.debug("Case {} moved to DNPronounced.", caseId);

        ensureCaseIsSearchable(caseId, citizenUser.getAuthToken());

        assertCaseStateIsAsExpected(DN_PRONOUNCED, citizenUser.getAuthToken());

        final UserDetails caseWorkerUser = createCaseWorkerUser();
        makeCasesEligibleForDa(caseWorkerUser.getAuthToken());

        assertCaseStateIsAsExpected(AWAITING_DA, citizenUser.getAuthToken());
    }

    private void ensureCaseIsSearchable(final String caseId, final String authToken) {
        await().pollInterval(fibonacci(SECONDS)).atMost(40, SECONDS).untilAsserted(() -> {
            List<CaseDetails> foundCases = searchCasesWithElasticSearch(caseId, authToken);
            assertThat("The number of cases found by ElasticSearch was not expected",
                        foundCases.size(), is(1));
        });
    }

    private List<CaseDetails> searchCasesWithElasticSearch(final String caseId, final String authToken) {

        QueryBuilder caseIdFilter = QueryBuilders.matchQuery(ES_CASE_ID_KEY, caseId);
        QueryBuilder stateFilter = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED);

        SearchSourceBuilder searchSourceBuilder =
                            buildCMSBooleanSearchSource(0, 10, caseIdFilter, stateFilter);

        return cmsClientSupport.searchCases(searchSourceBuilder.toString(), authToken);
    }

    private void assertCaseStateIsAsExpected(final String expectedState, final String authToken) {
        await().pollInterval(fibonacci(SECONDS)).atMost(55, SECONDS).untilAsserted(() -> {
            final Response retrievedCase = retrieveCase(authToken);
            log.debug("Retrieved case {} with state {}",
                        retrievedCase.path("caseId"), retrievedCase.path("state"));
            String msgForFailedAssertion = format("Case %s was not in expected state %s",
                                                            retrievedCase.path("caseId"), expectedState);
            assertThat(msgForFailedAssertion, retrievedCase.path(STATE_KEY), equalTo(expectedState));
        });
    }

    private void makeCasesEligibleForDa(final String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        Response response = RestUtil.postToRestService(
            serverUrl + jobSchedulerContextPath,
            headers,
            null,
            emptyMap()
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }
}
