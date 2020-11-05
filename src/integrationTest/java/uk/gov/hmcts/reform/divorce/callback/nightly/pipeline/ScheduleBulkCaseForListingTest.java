package uk.gov.hmcts.reform.divorce.callback.nightly.pipeline;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ScheduleBulkCaseForListingTest extends CcdSubmissionSupport {

    private static final String BULK_CREATE_JSON_FILE = "bulk-create.json";
    private static final String BULK_UPDATE_JSON_FILE = "bulk-update-listing.json";
    private static final String SCHEDULE_CREATED_EVENT_ID = "create";
    private static final String SCHEDULE_FOR_LISTING_EVENT_ID = "scheduleForListing";
    private static final String BULK_CASE_ACCEPTED_LIST_KEY = "CaseAcceptedList";
    private static final String BULK_HEARING_DATE_TIME_KEY = "hearingDate";
    private static final String COURT_NAME_FIELD_KEY = "CourtName";
    private static final String BULK_LISTED_STATE = "Listed";

    private static final int  MAX_WAITING_TIME_IN_SECONDS = 90;
    private static final int  POOL_INTERVAL_IN_MILLIS = 1000;

    @Test
    public void whenScheduleBulkCaseForListing_thenIndividualCasesShouldBeUpdated() throws Exception {
        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        String caseId1 = createAwaitingPronouncementCase(user1).getId().toString();
        String caseId2 = createAwaitingPronouncementCase(user2).getId().toString();

        CollectionMember<CaseLink> caseLink1 = new CollectionMember<>();
        caseLink1.setValue(CaseLink.builder().caseReference(caseId1).build());
        CollectionMember<CaseLink> caseLink2 = new CollectionMember<>();
        caseLink2.setValue(CaseLink.builder().caseReference(caseId2).build());

        List<CollectionMember<CaseLink>> acceptedCases = ImmutableList.of(caseLink1, caseLink2);

        String bulkCaseId = submitBulkCase(BULK_CREATE_JSON_FILE, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, acceptedCases))
            .getId().toString();

        UserDetails caseWorkerUser = createCaseWorkerUser();
        updateCase(bulkCaseId, null, SCHEDULE_CREATED_EVENT_ID, caseWorkerUser, true);

        updateCase(bulkCaseId, BULK_UPDATE_JSON_FILE, SCHEDULE_FOR_LISTING_EVENT_ID, caseWorkerUser, true,
            Pair.of(BULK_HEARING_DATE_TIME_KEY, LocalDateTime.now().plusMonths(3).toString()));

        validateCaseWithAwaitingTime(caseWorkerUser, caseId1);
        validateCaseWithAwaitingTime(caseWorkerUser, caseId2);
        validateBulkCaseWithAwaitingTime(caseWorkerUser, bulkCaseId);
    }

    private void validateCaseWithAwaitingTime(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
                .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
                .untilAsserted(() -> assertThat(retrieveCaseForCaseworker(user, caseId).getData().get(COURT_NAME_FIELD_KEY)).isNotNull());
    }

    private void validateBulkCaseWithAwaitingTime(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
                .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
                .untilAsserted(() -> assertThat(retrieveCaseForCaseworker(user, caseId).getState()).isEqualTo(BULK_LISTED_STATE));
    }
}
