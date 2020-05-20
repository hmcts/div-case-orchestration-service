package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isAdulteryCaseWithNamedCoRespondent;

@Component
@RequiredArgsConstructor
@Slf4j
public class CcdCallbackBulkPrintWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ServiceMethodValidationTask serviceMethodValidationTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final RespondentAosPackPrinterTask respondentAosPackPrinterTask;
    private final CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;
    private final ModifyDueDate modifyDueDate;

    @Value("${feature-toggle.toggle.feature_resp_solicitor_details}")
    private boolean featureToggleRespSolicitor;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        final List<Task> tasks = new ArrayList<>();

        tasks.add(serviceMethodValidationTask);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.add(respondentAosPackPrinterTask);
        if (isAdulteryCaseWithNamedCoRespondent(caseDetails.getCaseData())) {
            tasks.add(coRespondentAosPackPrinterTask);
        }
        tasks.add(modifyDueDate);

        return this.execute(tasks.toArray(new Task[0]),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(CASE_STATE_JSON_KEY, caseDetails.getState())
        );
    }
}
