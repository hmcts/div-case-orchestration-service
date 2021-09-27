package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Slf4j
@Component
public class RespondentAosPackPrinterTask implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";

    private final BulkPrinterTask bulkPrinter;
    private List<String> documentTypesInOrder;

    @Autowired
    public RespondentAosPackPrinterTask(final BulkPrinterTask bulkPrinter) {
        this.bulkPrinter = bulkPrinter;
        this.documentTypesInOrder = asList(DOCUMENT_TYPE_RESPONDENT_INVITATION, DOCUMENT_TYPE_PETITION);
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        return bulkPrinter.printSpecifiedDocument(context, payload, LETTER_TYPE_RESPONDENT_PACK, documentTypesInOrder);
    }

    public void setDocumentTypesToPrint(List<String> documentTypes) {
        this.documentTypesInOrder = documentTypes;
    }
}