package uk.gov.hmcts.reform.divorce.orchestration.workflows;

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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.getPaymentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.isFeeAccountPayment;
import static uk.gov.hmcts.reform.divorce.orchestration.util.FurtherPaymentsHelper.isHelpWithFeePayment;

@Component
@Slf4j
@RequiredArgsConstructor
public class FurtherPaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final FurtherPBAPaymentTask furtherPBAPaymentTask;
    private final FurtherHWFPaymentTask furtherHWFPaymentTask;

    public Map<String, Object> run(CaseDetails caseDetails, String ccdPaymentTypeField) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {}. Further payment workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails, ccdPaymentTypeField),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails, String ccdPaymentTypeField) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        String paymentType = getPaymentType(caseData, ccdPaymentTypeField);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isFeeAccountPayment(paymentType)) {
            log.info("CaseID: {}. Further payment type of '{}' received", caseId, paymentType);
            tasks.add(furtherPBAPaymentTask);

        } else if (isHelpWithFeePayment(paymentType)) {
            log.info("CaseID: {}. Further payment type of '{}' received", caseId, paymentType);
            tasks.add(furtherHWFPaymentTask);

        } else {
            log.info("CaseID: {}. Further payment type of '{}' received. No other steps taken.", caseId, paymentType);
        }

        return tasks.toArray(new Task[] {});
    }

}
