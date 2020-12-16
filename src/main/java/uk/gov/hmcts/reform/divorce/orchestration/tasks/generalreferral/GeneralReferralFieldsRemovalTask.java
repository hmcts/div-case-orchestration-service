package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
public class GeneralReferralFieldsRemovalTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return asList(
            CcdFields.GENERAL_REFERRAL_FEE,
            CcdFields.GENERAL_REFERRAL_DECISION_DATE,
            CcdFields.GENERAL_REFERRAL_REASON,
            CcdFields.GENERAL_APPLICATION_ADDED_DATE,
            CcdFields.GENERAL_APPLICATION_FROM,
            CcdFields.GENERAL_APPLICATION_REFERRAL_DATE,
            CcdFields.GENERAL_REFERRAL_TYPE,
            CcdFields.GENERAL_REFERRAL_DETAILS,
            CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE,
            CcdFields.GENERAL_REFERRAL_DECISION,
            CcdFields.GENERAL_REFERRAL_DECISION_REASON,
            CcdFields.ALTERNATIVE_SERVICE_MEDIUM,
            CcdFields.FEE_AMOUNT_WITHOUT_NOTICE
        );
    }
}
