package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMATTER;

public class CoRespondentReceivedCallbackTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @SuppressWarnings("unchecked")
    @Test
    public void givenCase_whenSubmitAOS_thenReturnAOSData() {
        CcdCallbackRequest caseRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TestConstants.TEST_CASE_ID)
                    .caseData(Collections.EMPTY_MAP)
                    .build()
            ).build();

        Map<String, Object> response = cosApiClient.coRespAnswerReceived("", caseRequest);

        assertNotNull(response.get(DATA));
        assertNull(response.get(ERRORS));
        assertEquals(YES_VALUE,  ((Map<String, Object>) response.get(DATA)).get(CO_RESPONDENT_ANSWER_RECEIVED));
        assertEquals(LocalDate.now().format(CCD_DATE_FORMATTER),  ((Map<String, Object>) response.get(DATA)).get(CO_RESPONDENT_ANSWER_RECEIVED_DATE));

    }

}
