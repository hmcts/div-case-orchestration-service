package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPbaPaymentFeatureToggledOnTaskAbstractTest extends ProcessPbaPaymentTaskAbstractTest {

    @Before
    public void setup() {
        this.setupForToggleOn();
    }

}
