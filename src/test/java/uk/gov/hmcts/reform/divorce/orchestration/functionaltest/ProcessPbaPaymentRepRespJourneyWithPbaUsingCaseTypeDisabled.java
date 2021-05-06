package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;

@SpringBootTest(properties = {
    "feature-toggle.toggle.represented_respondent_journey=true",
    "feature-toggle.toggle.pba_case_type=false"})
public class ProcessPbaPaymentRepRespJourneyWithPbaUsingCaseTypeDisabled extends ProcessPbaPaymentRepRespJourneyTest {

    @Override
    protected void setSiteIdOrCaseType(CreditAccountPaymentRequest request) {
        request.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
    }
}
