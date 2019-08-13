package uk.gov.hmcts.reform.divorce.callback;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class RemoveCaseFromListingTest extends CcdSubmissionSupport {

    private static final String BULK_CREATE_JSON_FILE = "bulk-create.json";
    private static final String BULK_UPDATE_JSON_FILE = "bulk-update-listing.json";
    private static final String SCHEDULE_CREATED_EVENT_ID = "create";
    private static final String SCHEDULE_FOR_LISTING_EVENT_ID = "scheduleForListing";
    private static final String BULK_CASE_ACCEPTED_LIST_KEY = "CaseAcceptedList";
    private static final String BULK_CASE_LIST_KEY = "CaseList";
    private static final String BULK_HEARING_DATE_TIME_KEY = "hearingDate";
    private static final String CASE_REFERENCE_FIELD = "CaseReference";
    private static final String BULK_LISTED_EVENT = "listed";
    private static final String REMOVE_FROM_BULK_LISTED_EVENT = "removeFromListed";

    @Test
    public void whenScheduleBulkCaseForListing_thenIndividualCasesShouldBeUpdated() throws Exception {

        //Given
        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        String caseId1 = createAwaitingPronouncementCase(user1).getId().toString();
        String caseId2 = createAwaitingPronouncementCase(user2).getId().toString();

        CollectionMember<CaseLink> caseLink1 = new CollectionMember<>();
        caseLink1.setValue(new CaseLink(caseId1));
        CollectionMember<CaseLink> caseLink2 = new CollectionMember<>();
        caseLink2.setValue(new CaseLink(caseId2));

        List<CollectionMember<CaseLink>> acceptedCases = //Arrays.asList(caseLink1);
            asList(caseLink1, caseLink2);

        List<CollectionMember<Map<String, Object>>> caseList = asList(getCaseInfo(caseId1), getCaseInfo(caseId2));
        String bulkCaseId = submitBulkCase(BULK_CREATE_JSON_FILE, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, acceptedCases),
            Pair.of(BULK_CASE_LIST_KEY, caseList))
            .getId().toString();

        moveBulkCaseListed(bulkCaseId);

        //when
        updateCase(bulkCaseId, REMOVE_FROM_BULK_LISTED_EVENT, true, Pair.of(BULK_CASE_ACCEPTED_LIST_KEY, asList(caseLink1)));

        CaseDetails bulkCase = retrieveCaseForCaseworker(createCaseWorkerUser(), bulkCaseId);

        String jsonResponse = objectToJson(bulkCase);

        //then
        assertThat(
            jsonResponse,
            hasJsonPath("$.case_data.D8DocumentsGenerated[0].value.DocumentFileName", is("caseListForPronouncement" + bulkCaseId))
        );

        assertThat(
            jsonResponse,
            hasJsonPath("$.case_data.CaseList.length()", is(1)
        ));

    }

    private void moveBulkCaseListed(String bulkCaseId) {
        updateCase(bulkCaseId, SCHEDULE_CREATED_EVENT_ID, true);
        updateCase(bulkCaseId, BULK_UPDATE_JSON_FILE, SCHEDULE_FOR_LISTING_EVENT_ID, true,
            Pair.of(BULK_HEARING_DATE_TIME_KEY, LocalDateTime.now().plusMonths(3).toString()),
            Pair.of(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_JUDGE_NAME));

        updateCase(bulkCaseId, BULK_LISTED_EVENT, true);
    }

    private CollectionMember<Map<String,Object>> getCaseInfo(String caseReference) {
        CollectionMember<Map<String,Object>> caseLink = new CollectionMember<>();
        caseLink.setValue(ImmutableMap.of(CASE_REFERENCE_FIELD, new CaseLink(caseReference)));
        return caseLink;
    }
}
