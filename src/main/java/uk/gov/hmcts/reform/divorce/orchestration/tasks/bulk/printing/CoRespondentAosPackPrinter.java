package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

@Slf4j
@Component
public class CoRespondentAosPackPrinter implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_CO_RESPONDENT_PACK = "co-respondent-aos-pack";
    private static final List<String> DOCUMENT_TYPES_TO_PRINT = asList(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION, DOCUMENT_TYPE_PETITION);

    private final BulkPrinterTask bulkPrinter;

    @Autowired
    public CoRespondentAosPackPrinter(final BulkPrinterTask bulkPrinter) {
        this.bulkPrinter = bulkPrinter;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> payload) {
        return bulkPrinter.printSpecifiedDocument(context, payload, LETTER_TYPE_CO_RESPONDENT_PACK, DOCUMENT_TYPES_TO_PRINT);
    }

}