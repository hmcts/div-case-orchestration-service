package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;

@SpringBootTest(properties = {
    "feature-toggle.toggle.represented_respondent_journey=true",
    "feature-toggle.toggle.pba_case_type=true"})
public class ProcessPbaPaymentRepRespJourneyWithPbaUsingCaseTypeEnabled extends ProcessPbaPaymentRepRespJourneyTest {

    @Override
    protected void setSiteIdOrCaseType(CreditAccountPaymentRequest request) {
        request.setCaseType(CASE_TYPE_ID);
    }
}
