package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTaskTest;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_PBA_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_PBA_REFERENCE_NUMBERS;

@RunWith(MockitoJUnitRunner.class)
public class FurtherPBAPaymentTaskTest extends FurtherPaymentTaskTest {

    @InjectMocks
    private FurtherPBAPaymentTask furtherPBAPaymentTask;

    @Override
    protected FurtherPaymentTask getTask() {
        return furtherPBAPaymentTask;
    }

    @Override
    protected String getFurtherPaymentReferenceNumbersField() {
        return FURTHER_PBA_REFERENCE_NUMBERS;
    }

    @Override
    protected String getPaymentReferenceNumberField() {
        return FURTHER_PBA_REFERENCE;
    }

    @Override
    protected String getPaymentType() {
        return FEE_ACCOUNT_TYPE;
    }

}