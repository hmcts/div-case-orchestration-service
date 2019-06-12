package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UpdateBulkCaseDnPronouncementDateTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void whenCaseLinkedForHearingIsCalledBack_thenReturnAOSData() {
        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(Collections.singletonMap("hearingDate", "2000-01-01T10:20:55.000"))
                .build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        Map<String, Object> response = cosApiClient.bulkPronouncement(ccdCallbackRequest);

        Map<String, Object> responseData = (Map<String, Object>) response.get(DATA);
        assertEquals(responseData.get("DecreeNisiGrantedDate"), "2000-01-01");
        assertEquals(responseData.get("DAEligibleFromDate"), "2000-02-13");
    }
}