package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class DefaultTaskContextTest {

    private DefaultTaskContext defaultTaskContext;

    @Before
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
    }

    @Test
    public void defaultContextStatusIsFalseByDefault() {
        assertFalse(defaultTaskContext.getStatus());
    }

    @Test
    public void defaultContextStatusIsTrueWhenSetFailedIsCalled() {
        defaultTaskContext.setTaskFailed(true);
        assertTrue(defaultTaskContext.getStatus());
    }

}
