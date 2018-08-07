//package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;
//
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter(value = AccessLevel.PROTECTED)
//@Setter(value = AccessLevel.PROTECTED)
//class ExecutionContext<X, Y> {
//    private Payload<X> payloadOriginal;
//    private Payload<Y> payloadAfterExecution;
//    private TaskState status = TaskState.Initialized;
//
//    public boolean isFailed() {
//        return this.status == TaskState.Failed;
//    }
//}
