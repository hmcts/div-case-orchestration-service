package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class DefaultTaskContextTest {

    private DefaultTaskContext defaultTaskContext;

    @Before
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
    }

    @Test
    public void defaultContextStatusIsFalseByDefault() {
        assertEquals(false, defaultTaskContext.getStatus());
    }

    @Test
    public void defaultContextStatusIsTrueWhenSetFailedIsCalled() {
        defaultTaskContext.setTaskFailed(true);
        assertEquals(true, defaultTaskContext.getStatus());
    }

}
