package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendDaGrantedDocumentsToBulkPrintTask implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_DA_GRANTED_PACK = "da-granted-pack"; //TODO: verify this is correct
    private static final List<String> DOCUMENT_TYPES_IN_ORDER = asList(DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE);

    private final BulkPrinter bulkPrinter;

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        String caseId = (String) caseData.get(CASE_ID_JSON_KEY);

        log.debug("Sending DA Granted documents {} for case {} to bulk print", StringUtils.join(DOCUMENT_TYPES_IN_ORDER, " and "), caseId);
        Map<String, Object> responseData = bulkPrinter.printSpecifiedDocument(context, caseData, LETTER_TYPE_DA_GRANTED_PACK, DOCUMENT_TYPES_IN_ORDER);
        log.debug("Returned from bulk print for for case {}", caseData);

        return responseData;
    }
}
