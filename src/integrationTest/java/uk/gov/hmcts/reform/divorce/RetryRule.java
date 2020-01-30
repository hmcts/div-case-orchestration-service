package uk.gov.hmcts.reform.divorce;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@Slf4j
public class RetryRule implements TestRule {
    private int retryCount;

    private RetryRule(int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        log.error("{} run {} failed. - {}", description.getDisplayName(), (i + 1), t.getMessage());
                    }
                }
                log.error("{}: giving up after {} failures.", description.getDisplayName(), retryCount);
                throw caughtThrowable;
            }
        };
    }
}
