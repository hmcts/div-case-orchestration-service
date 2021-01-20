package uk.gov.hmcts.reform.divorce.callback.nightly.pipeline;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class RemoveCaseFromListingTest extends CcdSubmissionSupport {

    private static final String BULK_CREATE_JSON_FILE = "bulk-create.json";
    private static final String BULK_UPDATE_JSON_FILE = "bulk-update-listing.json";
    private static final String SCHEDULE_FOR_LISTING_EVENT_ID = "scheduleForListing";
    private static final String BULK_HEARING_DATE_TIME_KEY = "hearingDate";
    private static final String REMOVE_FROM_BULK_LISTED_EVENT = "removeFromListed";

    private static final int  MAX_WAITING_TIME_IN_SECONDS = 90;
    private static final int  POOL_INTERVAL_IN_MILLIS = 1000;

    @Test
    public void whenScheduleBulkCaseForRemoval_thenIndividualCasesShouldBeUpdated() throws Exception {

        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        String caseId1 = createAwaitingPronouncementCase(user1).getId().toString();
        String caseId2 = createAwaitingPronouncementCase(user2).getId().toString();

        CollectionMember<CaseLink> caseLink1 = new CollectionMember<>();
        caseLink1.setValue(CaseLink.builder().caseReference(caseId1).build());
        CollectionMember<CaseLink> caseLink2 = new CollectionMember<>();
        caseLink2.setValue(CaseLink.builder().caseReference(caseId2).build());

        List<CollectionMember<CaseLink>> acceptedCases = asList(caseLink1, caseLink2);

        List<CollectionMember<Map<String, Object>>> caseList = asList(getCaseInfo(caseId1), getCaseInfo(caseId2));
        String bulkCaseId = submitBulkCase(BULK_CREATE_JSON_FILE, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, acceptedCases),
            Pair.of(CASE_LIST_KEY, caseList))
            .getId().toString();

        moveBulkCaseListed(bulkCaseId);
        waitForIndividualCasesToUpdate(createCaseWorkerUser(), caseId2);

        updateCase(bulkCaseId, REMOVE_FROM_BULK_LISTED_EVENT, true, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, Collections.singletonList(caseLink1)));

        CaseDetails bulkCase = retrieveCaseForCaseworker(createCaseWorkerUser(), bulkCaseId);
        String jsonResponse = objectToJson(bulkCase);

        assertThat(
            jsonResponse,
            hasJsonPath("$.case_data.D8DocumentsGenerated[0].value.DocumentFileName", is(CASE_LIST_FOR_PRONOUNCEMENT_FILE_NAME + bulkCaseId))
        );

        assertThat(
            jsonResponse,
            hasJsonPath("$.case_data.CaseList.length()", is(1)
        ));

        validateCaseWithAwaitingTime(createCaseWorkerUser(), caseId2);
    }

    private void moveBulkCaseListed(String bulkCaseId) {
        updateCase(bulkCaseId, CREATE_EVENT, true);
        updateCase(bulkCaseId, BULK_UPDATE_JSON_FILE, SCHEDULE_FOR_LISTING_EVENT_ID, true,
            Pair.of(BULK_HEARING_DATE_TIME_KEY, LocalDateTime.now().plusMonths(3).toString()),
            Pair.of(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_JUDGE_NAME));

        updateCase(bulkCaseId, LISTED_EVENT, true);
    }

    private CollectionMember<Map<String,Object>> getCaseInfo(String caseReference) {
        CollectionMember<Map<String,Object>> caseLink = new CollectionMember<>();
        caseLink.setValue(ImmutableMap.of(CASE_REFERENCE_FIELD, CaseLink.builder().caseReference(caseReference).build()));
        return caseLink;
    }

    private void waitForIndividualCasesToUpdate(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
            .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
            .untilAsserted(() -> Assertions.assertThat(
                    retrieveCaseForCaseworker(user, caseId).getData().get(COURT_NAME_CCD_FIELD)).isNotNull());
    }

    private void validateCaseWithAwaitingTime(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
            .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
            .untilAsserted(() -> Assertions.assertThat(
                retrieveCaseForCaseworker(user, caseId).getData().get(COURT_NAME_CCD_FIELD)).isNull());
    }
}
