package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

public abstract class ThreadSafeStatefulTask<T, X> implements Task<T> {
    private ThreadLocal<X> state;

    public final void setState(X state) {
        this.state = new ThreadLocal<>();
        this.state.set(state);
    }

    protected final X getState() {
        return state.get();
    }
}
