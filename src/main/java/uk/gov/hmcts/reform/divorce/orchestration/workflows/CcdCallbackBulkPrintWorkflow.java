package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.BulkPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class CcdCallbackBulkPrintWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final BulkPrinter bulkPrinter;

    private final ModifyDueDate modifyDueDate;

    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Autowired
    public CcdCallbackBulkPrintWorkflow(FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore, BulkPrinter bulkPrinter,
                                        ModifyDueDate modifyDueDate) {
        this.fetchPrintDocsFromDmStore = fetchPrintDocsFromDmStore;
        this.bulkPrinter = bulkPrinter;
        this.modifyDueDate = modifyDueDate;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken) throws WorkflowException {
        return this.execute(
            new Task[] {
                fetchPrintDocsFromDmStore,
                bulkPrinter,
                modifyDueDate
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails())
        );
    }
}
