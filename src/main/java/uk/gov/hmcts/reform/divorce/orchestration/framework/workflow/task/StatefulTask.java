package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

public abstract class StatefulTask<T, X> implements Task<T> {
    private X state;

    public final void setState(X state) {
        this.state = state;
    }

    protected final X getState() {
        return state;
    }
}
