package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CaseOrchestrationValuesTest {

    @Autowired
    private CaseOrchestrationValues caseOrchestrationValues;

    @Test
    public void shouldLoadDefaultValues() {
        assertThat(caseOrchestrationValues.getAosOverdueGracePeriod(), is("0"));
    }

}