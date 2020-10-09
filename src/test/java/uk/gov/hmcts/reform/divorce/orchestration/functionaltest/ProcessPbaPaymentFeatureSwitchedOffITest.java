package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = {"feature-toggle.toggle.pay_by_account=false"})
public class ProcessPbaPaymentFeatureSwitchedOffITest extends ProcessPbaPaymentAbstractITest {

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndPbaToggleIsOff_thenMakePaymentAndReturn_PetitionerNewCase() throws Exception {
        setupForToggleOff();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerNewCase();
    }

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndPbaToggleIsOff_thenMakePaymentAndReturn_PetitionerAmendedCase() throws Exception {
        setupForToggleOff();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerAmendedCase();
    }

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndPbaToggleIsOff_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase() throws Exception {
        setupForToggleOff();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase();
    }

    @Test
    public void givenInvalidCaseData_whenProcessPbaPaymentAndPbaToggleIsOff_thenReturnErrors() throws Exception {
        setupForToggleOff();
        givenInvalidCaseData_whenProcessPbaPayment_thenReturnErrors();
    }
}
