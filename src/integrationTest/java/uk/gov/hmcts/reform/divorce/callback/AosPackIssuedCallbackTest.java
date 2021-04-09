package uk.gov.hmcts.reform.divorce.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;

import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.events.CcdTestEvents.TEST_AOS_AWAITING_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.getCollectionMembersOrEmptyList;

@Slf4j
public class AosPackIssuedCallbackTest extends RetrieveCaseSupport {

    @Autowired
    private CosApiClient cosApiClient;

    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";

    private UserDetails citizenUser;
    private UserDetails caseworkerUser;

    @Before
    public void setUp() {
        citizenUser = createCitizenUser();
        caseworkerUser = createCaseWorkerUser();
    }

    @Test
    public void shouldReturnAppropriateResponseWhenAosPackIssuedIsCalled() {
        CaseDetails caseDetails = createCaseAndTriggerTestEvent();

        String caseId = String.valueOf(caseDetails.getId());
        cosApiClient.aosPackIssued(AUTH_TOKEN, CcdCallbackRequest.builder().eventId("issueAOS").caseDetails(
            uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails.builder()
                .caseId(caseId)
                .caseData(caseDetails.getData())
                .build()
        ).build());

        await().atMost(90, SECONDS).pollInterval(2, SECONDS).untilAsserted(() -> {
            CaseDetails retrievedCase = retrieveCaseForCaseworker(caseworkerUser, caseId);
            Map<String, Object> caseData = retrievedCase.getData();
            List<CollectionMember<Document>> generatedDocuments =
                getCollectionMembersOrEmptyList(getObjectMapperInstance(), caseData, D8DOCUMENTS_GENERATED);
            assertThat(generatedDocuments, hasSize(2));
            List<String> documentTypes = generatedDocuments.stream().map(CollectionMember::getValue).map(Document::getDocumentType).collect(toList());
            assertThat(documentTypes, hasItems("aosinvitationletter-offline-resp", "behaviour-desertion-aos-form"));
        });
    }

    private CaseDetails createCaseAndTriggerTestEvent() {
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, citizenUser, Pair.of(RESP_SOL_REPRESENTED, YES_VALUE));
        String caseId = String.valueOf(caseDetails.getId());
        log.debug("Created case id {}", caseId);
        return updateCase(caseId, null, TEST_AOS_AWAITING_EVENT, caseworkerUser);
    }

}