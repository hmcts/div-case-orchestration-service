package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDocLink;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
public class SolicitorDnFetchDocWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateDocLink populateDocLink;

    @Autowired
    public SolicitorDnFetchDocWorkflow(PopulateDocLink populateDocLink) {
        this.populateDocLink = populateDocLink;
    }

    public Map<String, Object> run(CaseDetails caseDetails, final String documentType, final String docLinkFieldName) throws WorkflowException {
        if (isRespondentAnswersRequestedButNotRequired(caseDetails.getCaseData(), docLinkFieldName)) {
            return caseDetails.getCaseData();
        }

        return this.execute(new Task[] {
            populateDocLink
        }, caseDetails.getCaseData(),
            ImmutablePair.of(DOCUMENT_TYPE, documentType),
            ImmutablePair.of(DOCUMENT_DRAFT_LINK_FIELD, docLinkFieldName));
    }

    private boolean isRespondentAnswersRequestedButNotRequired(Map<String, Object> caseData, String docLinkFieldName) {
        boolean isRespondentAnswersRequested = RESP_ANSWERS_LINK.equals(docLinkFieldName);
        boolean isValidServiceApplicationGranted =
            isServiceApplicationGranted(caseData) && (isServiceApplicationDeemed(caseData) || isServiceApplicationDispensed(caseData));
        return isRespondentAnswersRequested && isValidServiceApplicationGranted;
    }
}
