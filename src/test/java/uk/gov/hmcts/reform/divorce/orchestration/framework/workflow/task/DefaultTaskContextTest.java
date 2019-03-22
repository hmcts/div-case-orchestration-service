package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class DefaultTaskContextTest {

    private DefaultTaskContext defaultTaskContext;

    @Before
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
    }

    @Test
    public void defaultContextTasFailedIsFalseByDefault() {
        assertThat(defaultTaskContext.hasTaskFailed(), is(false));
    }

    @Test
    public void defaultContextStatusIsTrueWhenSetFailedIsCalled() {
        defaultTaskContext.setTaskFailed(true);
        assertTrue(defaultTaskContext.hasTaskFailed());
    }

    @Test
    public void computesTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", null);
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "bar");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
    }

    @Test
    public void doesNotComputeTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", "bar");
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "baz");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
    }

}
