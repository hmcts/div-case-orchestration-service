package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_PBA_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_PBA_REFERENCE_NUMBERS;

@Component
@Slf4j
@AllArgsConstructor
public class FurtherPBAPaymentTask extends FurtherPaymentTask {

    @Override
    protected String getFurtherPaymentReferenceNumbersField() {
        return FURTHER_PBA_REFERENCE_NUMBERS;
    }

    @Override
    protected String getPaymentReferenceNumberField() {
        return FURTHER_PBA_REFERENCE;
    }
}
