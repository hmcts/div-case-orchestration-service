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
public class RespondentAosPackPrinter implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final List<String> DOCUMENT_TYPES_IN_ORDER = asList(DOCUMENT_TYPE_RESPONDENT_INVITATION, DOCUMENT_TYPE_PETITION);

    private final BulkPrinter bulkPrinter;

    @Autowired
    public RespondentAosPackPrinter(final BulkPrinter bulkPrinter) {
        this.bulkPrinter = bulkPrinter;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        return bulkPrinter.printSpecifiedDocument(context, payload, LETTER_TYPE_RESPONDENT_PACK, DOCUMENT_TYPES_IN_ORDER);
    }

}