package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;


import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FurtherPaymentTaskTest;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_HWF_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FURTHER_HWF_REFERENCE_NUMBERS;

@RunWith(MockitoJUnitRunner.class)
public class FurtherHWFPaymentTaskTest extends FurtherPaymentTaskTest {

    @InjectMocks
    private FurtherHWFPaymentTask furtherHWFPaymentTask;

    @Override
    protected FurtherPaymentTask getTask() {
        return furtherHWFPaymentTask;
    }

    @Override
    protected String getFurtherPaymentReferenceNumbersField() {
        return FURTHER_HWF_REFERENCE_NUMBERS;
    }

    @Override
    protected String getPaymentReferenceNumberField() {
        return FURTHER_HWF_REFERENCE;
    }

    @Override
    protected String getPaymentType() {
        return HELP_WITH_FEE_TYPE;
    }
}