package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.FurtherHWFPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.FurtherPBAPaymentTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmServicePaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final FurtherPBAPaymentTask furtherPBAPaymentTask;
    private final FurtherHWFPaymentTask furtherHWFPaymentTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Confirm service payment workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        String paymentType = getPaymentType(caseData);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (FEE_ACCOUNT_TYPE.equals(paymentType)) {
            log.info("CaseID: {}. Confirm service payment type of '{}' received", caseId, paymentType);
            tasks.add(furtherPBAPaymentTask);
        } else if (HELP_WITH_FEE_TYPE.equals(paymentType)) {
            log.info("CaseID: {}. Confirm service payment type of '{}' received", caseId, paymentType);
            tasks.add(furtherHWFPaymentTask);
        } else {
            log.info("CaseID: {}. Confirm service payment type of '{}' received. No further steps taken", caseId, paymentType);
        }

        return tasks.toArray(new Task[] {});
    }

    private String getPaymentType(Map<String, Object> caseData) {
        return Optional.ofNullable((String) caseData.get(SERVICE_APPLICATION_PAYMENT))
            .orElseGet(() -> EMPTY_STRING);
    }


}
