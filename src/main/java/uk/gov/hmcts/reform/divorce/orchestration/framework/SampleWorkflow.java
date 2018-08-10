package uk.gov.hmcts.reform.divorce.orchestration.framework;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Workflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

@Slf4j
public class SampleWorkflow {

    public static void main(String[] args) throws WorkflowException {

        Workflow<CaseDetails> workflow = new DefaultWorkflow<>();

        CaseDetails caseDetails = CaseDetails.builder().caseId("1").build();
        caseDetails = workflow.execute(new Task[] {new ValidateTask(), new CCDClient()}, caseDetails);
        log.info(caseDetails.toString());


        caseDetails = CaseDetails.builder().caseId("2").build();
        caseDetails = workflow.execute(new Task[] {new ValidateTask(), new CCDClient()}, caseDetails);
        log.info(caseDetails.toString());

        String simpleOutput = new DefaultWorkflow<String>().execute(new Task[] {
            (context, pay) -> pay + " Workflow",
            (context, pay) -> pay + " World"},
            "Hello");

        log.info(simpleOutput);
    }
}

@Slf4j
class ValidateTask implements Task<CaseDetails> {

    @Override
    public CaseDetails execute(TaskContext context, CaseDetails payLoad) throws TaskException {
        if (payLoad.getCaseId().equals("1")) {
            log.info("Validation success ");
            context.setTransientObject("validationurl", "http://www.valid.url");
            payLoad.setState("VALID");
        }else {
            log.info("Validation failed");
            payLoad.setState("FAILED");
            context.setTaskFailed(true);
        }
        return payLoad;
    }
}

@Slf4j
class CCDClient implements Task<CaseDetails> {

    @Override
    public CaseDetails execute(TaskContext context, CaseDetails payLoad) throws TaskException {
        log.info("sent case to ccd. ");
        payLoad.setState("SENT " + context.getTransientObject("validationurl"));
        return payLoad;
    }
}
