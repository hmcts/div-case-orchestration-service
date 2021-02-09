package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDocLinkTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getLastServiceApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemedOrDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceHelper.isServedByAlternativeMethod;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceHelper.isServedByProcessServer;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorDnFetchDocWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateDocLinkTask populateDocLinkTask;

    public Map<String, Object> run(CaseDetails caseDetails, final String ccdDocumentType, final String docLinkFieldName) throws WorkflowException {

        final String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} Solicitor DN Fetch Document Workflow is going to be executed.", caseId);

        if (isRespondentAnswersRequested(docLinkFieldName)) {

            if (isRespondentAnswersNotRequired(caseData)) {
                log.info("CaseID: {} Respondent answers requested, but not required.", caseId);
                return caseData;
            } else {
                log.info("CaseID: {} Respondent answers required, proceeding to populateDocLink", caseId);
            }
        }

        log.info("CaseID: {} populateDocLink task is going to be executed.", caseId);

        return this.execute(
            new Task[] {populateDocLinkTask},
            caseData,
            ImmutablePair.of(DOCUMENT_TYPE, ccdDocumentType),
            ImmutablePair.of(DOCUMENT_DRAFT_LINK_FIELD, docLinkFieldName));
    }

    private boolean isRespondentAnswersRequested(String docLinkFieldName) {
        return RESP_ANSWERS_LINK.equals(docLinkFieldName);
    }

    private boolean isRespondentAnswersNotRequired(Map<String, Object> caseData) {
        return isValidServiceApplicationGranted(caseData) || isAlternativeService(caseData);
    }

    private boolean isValidServiceApplicationGranted(Map<String, Object> caseData) {
        DivorceServiceApplication serviceApplication = getLastServiceApplication(caseData);

        return isServiceApplicationGranted(serviceApplication)
            && isServiceApplicationDeemedOrDispensed(serviceApplication);
    }

    private boolean isAlternativeService(Map<String, Object> caseData) {
        return isValidServedByProcessServer(caseData)
            || isValidServedByAlternativeMethod(caseData);
    }

    private boolean isValidServedByProcessServer(Map<String, Object> caseData) {
        return isServedByProcessServer(caseData)
            && !isServedByAlternativeMethod(caseData);
    }

    private boolean isValidServedByAlternativeMethod(Map<String, Object> caseData) {
        return isServedByAlternativeMethod(caseData)
            && !isServedByProcessServer(caseData);
    }
}
