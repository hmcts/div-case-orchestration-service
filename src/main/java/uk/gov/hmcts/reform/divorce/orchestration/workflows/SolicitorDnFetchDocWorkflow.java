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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDocLink;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getLastServiceApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemedOrDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorDnFetchDocWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateDocLink populateDocLink;

    public Map<String, Object> run(CaseDetails caseDetails, final String ccdDocumentType, final String docLinkFieldName) throws WorkflowException {

        final String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Solicitor DN Fetch Document Workflow is going to be executed.", caseId);

        if (isRespondentAnswersRequestedButNotRequired(caseDetails.getCaseData(), docLinkFieldName)) {
            log.info("CaseID: {} respondent answers requested, but not required.", caseId);
            return caseDetails.getCaseData();
        }

        log.info("CaseID: {} populateDocLink task is going to be executed.", caseId);

        return this.execute(
            new Task[] {populateDocLink},
            caseDetails.getCaseData(),
            ImmutablePair.of(DOCUMENT_TYPE, ccdDocumentType),
            ImmutablePair.of(DOCUMENT_DRAFT_LINK_FIELD, docLinkFieldName));
    }

    private boolean isRespondentAnswersRequestedButNotRequired(Map<String, Object> caseData, String docLinkFieldName) {
        DivorceServiceApplication serviceApplication = getLastServiceApplication(caseData);

        boolean isRespondentAnswersRequested = RESP_ANSWERS_LINK.equals(docLinkFieldName);

        boolean isValidServiceApplicationGranted = isServiceApplicationGranted(serviceApplication)
            && isServiceApplicationDeemedOrDispensed(serviceApplication);

        return isRespondentAnswersRequested && isValidServiceApplicationGranted;
    }
}
