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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_DOCUMENT_LINK_FIELD;

@Component
public class SolicitorDnFetchDocWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateDocLink populateDocLink;

    @Autowired
    public SolicitorDnFetchDocWorkflow(PopulateDocLink populateDocLink) {
        this.populateDocLink = populateDocLink;
    }

    public Map<String, Object> run(CaseDetails caseDetails, final String documentType, final String docLinkFieldName) throws WorkflowException {
        return this.execute(new Task[] {
            populateDocLink
        }, caseDetails.getCaseData(),
            ImmutablePair.of(DOCUMENT_TYPE, documentType),
            ImmutablePair.of(SOL_DOCUMENT_LINK_FIELD, docLinkFieldName));
    }
}
