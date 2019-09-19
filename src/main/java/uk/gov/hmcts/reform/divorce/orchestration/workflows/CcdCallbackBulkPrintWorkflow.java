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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentAosPackPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAosPackPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CcdCallbackBulkPrintWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ServiceMethodValidationTask serviceMethodValidationTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final RespondentAosPackPrinter respondentAosPackPrinter;
    private final CoRespondentAosPackPrinter coRespondentAosPackPrinter;
    private final ModifyDueDate modifyDueDate;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Value("${feature-toggle.toggle.feature_resp_solicitor_details}")
    private boolean featureToggleRespSolicitor;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        final List<Task> tasks = new ArrayList<>();

        tasks.add(serviceMethodValidationTask);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.addAll(getRespondentAosCommunicationTasks(caseDetails.getCaseData()));
        tasks.add(coRespondentAosPackPrinter);
        tasks.add(modifyDueDate);

        return this.execute(tasks.toArray(new Task[tasks.size()]),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }

    private List<Task> getRespondentAosCommunicationTasks(final Map<String, Object> caseData) {
        if (featureToggleRespSolicitor && usingRespondentSolicitor(caseData)) {
            return asList(
                respondentAosPackPrinter,
                caseFormatterAddDocuments);
        }
        return singletonList(respondentAosPackPrinter);
    }

    private boolean usingRespondentSolicitor(Map<String, Object> caseData) {
        // TODO - temporary fix until we implement setting respondentSolicitorRepresented in CCD for Resp Sol cases
        final String respondentSolicitorRepresented = (String) caseData.get(RESP_SOL_REPRESENTED);
        final String respondentSolicitorName = (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME);
        final String respondentSolicitorCompany = (String) caseData.get(D8_RESPONDENT_SOLICITOR_COMPANY);

        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented)
            || respondentSolicitorName != null && respondentSolicitorCompany != null;
    }
}
