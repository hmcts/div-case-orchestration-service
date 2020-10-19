package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"feature-toggle.toggle.pay_by_account=true"})
public class ProcessPbaPaymentFeatureSwitchedOnIAbstractITest extends ProcessPbaPaymentAbstractITest {

    @Before
    public void setup() {
        this.setupForToggleOn();
    }
}
