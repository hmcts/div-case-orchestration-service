package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"feature-toggle.toggle.pay_by_account=true"})
public class ProcessPbaPaymentFeatureSwitchedOnITest extends ProcessPbaPaymentAbstractITest {

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndPbaToggleIsOn_thenMakePaymentAndReturn_PetitionerNewCase() throws Exception {
        setupForToggleOn();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerNewCase();
    }

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndToggleIsOn_thenMakePaymentAndReturn_PetitionerAmendedCase() throws Exception {
        setupForToggleOn();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_PetitionerAmendedCase();
    }

    @Test
    public void givenCaseData_whenProcessPbaPaymentAndPbaToggleIsOn_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase() throws Exception {
        setupForToggleOn();
        givenCaseData_whenProcessPbaPayment_thenMakePaymentAndReturn_SolicitorPetitionerAmendedCase();
    }

    @Test
    public void givenInvalidCaseData_whenProcessPbaPaymentAndPbaToggleIsOn_thenReturnErrors() throws Exception {
        setupForToggleOn();
        givenInvalidCaseData_whenProcessPbaPayment_thenReturnErrors();
    }
}
