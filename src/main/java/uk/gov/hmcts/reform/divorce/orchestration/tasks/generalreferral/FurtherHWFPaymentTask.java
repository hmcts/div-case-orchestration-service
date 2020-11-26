package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_HWF_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_HWF_REFERENCE_NUMBERS;

@Component
@Slf4j
@AllArgsConstructor
public class FurtherHWFPaymentTask extends FurtherPaymentTask {

    @Override
    protected String getFurtherPaymentReferenceNumbersField() {
        return FURTHER_HWF_REFERENCE_NUMBERS;
    }

    @Override
    protected String getPaymentReferenceNumberField() {
        return FURTHER_HWF_REFERENCE;
    }
}
