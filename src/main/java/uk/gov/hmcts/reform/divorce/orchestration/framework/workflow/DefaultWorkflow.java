//package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;
//
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Payload;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskData;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskReport;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskState;
//import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TransformationException;
//
//import java.util.List;
//
//public class DefaultWorkflow<X, Y, Z, S, T> implements Workflow<List<Task>, Y, Z, S, T> {
//
//    @Override
//    public TaskReport<Y, S, Z> execute(TaskData<T> payload, TaskReport<List<Task>, Y, Z> request)
//        throws TaskException {
//        if(request == null) {
//            throw new IllegalArgumentException("Payload cannot be null");
//        }
//
//        TaskReport output = null;
//        TaskReport<T, Y, Z> initialReport = TaskReport.builder().taskRequest(payload).build();
//
//        for (Task task: request.getTaskReport().getTaskRequest().getBody()) {
//            output =  task.execute(payload, intialReport);
//
//            if(output.getTaskState() == TaskState.Failed) {
//                return output;
//            }
//        }
//
//        return output;
//    }
//
//
////    private static class PayloadWrapper<T> extends Payload<T> {
////
////        PayloadWrapper(Payload<T> payload) {
////            super(payload.getData(), payload.isHaltOnError());
////        }
////
////        void complete(Payload<T> payload) {
////            this.setPayloadAfterExecution(payload);
////            this.setStatus(TaskState.Succeeded);
////        }
////
////        Payload executeTransform() throws TransformationException {
////            if(this.getTransformer() == null) {
////                return this;
////            }
////
////            return this.getTransformer().transform(getPayloadAfterExecution());
////        }
////
////        static <T> PayloadWrapper start(Payload<T> payload) {
////            PayloadWrapper<T> payloadWrapper = new PayloadWrapper<>(payload);
////            payloadWrapper.setStatus(TaskState.Processing);
////            return payloadWrapper;
////        }
////    }
//}
