package uk.gov.hmcts.reform.divorce.orchestration.framework;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
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


        System.out.println( "***************************************************** \n\n");

        caseDetails = CaseDetails.builder().caseId("2").build();
        caseDetails = workflow.execute(new Task[] {new ValidateTask(), new CCDClient()}, caseDetails);
        log.info(caseDetails.toString());

        System.out.println( "***************************************************** \n\n");

        String simpleOutput = new DefaultWorkflow<String>().execute(new Task[] {
            (context, pay) -> pay + " Workflow",
            (context, pay) -> pay + " World"},
            "Hello");

        log.info(simpleOutput);
    }

}


class ValidateCaseData implements Task<CaseDetails> {

    private static final String FORM_ID = "case-progression";

    private CaseValidationClient caseValidationClient;

    public ValidateCaseData(CaseValidationClient caseValidationClient) {
        this.caseValidationClient = caseValidationClient;
    }

    @Override
    public CaseDetails execute(TaskContext context, CaseDetails caseDetails) throws TaskException {
        ValidationResponse validationResponse =
            caseValidationClient.validate(
                ValidationRequest.builder()
                    .data(caseDetails.getCaseData())
                    .formId(FORM_ID)
                    .build());

        if (!validationResponse.isValid()) {
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName()+"_Error", validationResponse);
        }
        return caseDetails;
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
