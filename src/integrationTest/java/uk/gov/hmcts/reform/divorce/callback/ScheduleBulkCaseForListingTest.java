package uk.gov.hmcts.reform.divorce.callback;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleBulkCaseForListingTest extends CcdSubmissionSupport {

    private static final String BULK_CREATE_JSON_FILE = "bulk-create.json";
    private static final String BULK_UPDATE_JSON_FILE = "bulk-update-listing.json";
    private static final String SCHEDULE_FOR_LISTING_EVENT_ID = "scheduleForListing";
    private static final String BULK_CASE_ACCEPTED_LIST_KEY = "CaseAcceptedList";
    private static final String BULK_HEARING_DATE_TIME_KEY = "hearingDate";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void whenScheduleBulkCaseForListing_thenIndividualCasesShouldBeUpdated() throws Exception {
        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        CaseDetails case1 = createAwaitingPronouncementCase(user1);
        CaseDetails case2 = createAwaitingPronouncementCase(user2);
        waitToProcess();

        CollectionMember<CaseLink> caseLink1 = new CollectionMember<>();
        caseLink1.setValue(new CaseLink(case1.getId().toString()));
        CollectionMember<CaseLink> caseLink2 = new CollectionMember<>();
        caseLink2.setValue(new CaseLink(case2.getId().toString()));

        List<CollectionMember<CaseLink>> acceptedCases = new ArrayList<>();
        acceptedCases.add(caseLink1);
        acceptedCases.add(caseLink2);

        Long bulkCaseId = submitBulkCase(BULK_CREATE_JSON_FILE, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, acceptedCases)).getId();

        updateCase(bulkCaseId.toString(), BULK_UPDATE_JSON_FILE, SCHEDULE_FOR_LISTING_EVENT_ID, true,
                Pair.of(BULK_HEARING_DATE_TIME_KEY, LocalDateTime.now().plusMonths(3).toString()));

        waitToProcess();
    }
}
