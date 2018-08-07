//package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;
//
//import lombok.Builder;
//
//public class Payload<T> extends ExecutionContext {
//    private T data;
//    private boolean haltOnError = true;
//    private Transform transformer;
//
//    @Builder
//    public Payload(T data, boolean haltOnError) {
//        this.data = data;
//        this.haltOnError = haltOnError;
//        this.setPayloadOriginal(this);
//    }
//
//    @Builder
//    public Payload(T data) {
//        this.data = data;
//        this.setPayloadOriginal(this);
//    }
//
//    public T getData() {
//        return data;
//    }
//
//    public boolean isHaltOnError() {
//        return haltOnError;
//    }
//
//    protected Transform getTransformer() {
//        return transformer;
//    }
//
//    public Payload markFailed(Payload payload) {
//        this.setPayloadAfterExecution(payload);
//        this.setStatus(TaskState.Failed);
//
//        return this;
//    }
//
//    public void transform(Transform transformer) {
//        this.transformer = transformer;
//    }
//}
