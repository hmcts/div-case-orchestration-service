package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.REMOVED_CASE_LIST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getCaseLinkValue;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getElementFromCollection;

@Slf4j
@Component
public class SyncBulkCaseListTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> bulkCaseData) {
        List<Map<String, Object>> acceptedCasesList = (List<Map<String, Object>>) bulkCaseData.get(BULK_CASE_ACCEPTED_LIST_KEY);
        List<Map<String, Object>> caseList =  (List<Map<String, Object>>) bulkCaseData.get(CASE_LIST_KEY);

        List<String> acceptedCaseIdList =  acceptedCasesList.stream()
            .map(this::getCaseIdFromAcceptedCase)
            .collect(Collectors.toList());

        List<Map<String, Object>> finalCases = caseList.stream()
            .filter(caseData -> {
                String caseReference = getCaseIdFromBulkCaseData(caseData);
                return acceptedCaseIdList.contains(caseReference);
            })
            .collect(Collectors.toList());

        List<String> removedCases = caseList.stream()
            .map(this::getCaseIdFromBulkCaseData)
            .filter(caseReference -> !acceptedCaseIdList.contains(caseReference))
            .collect(Collectors.toList());

        context.setTransientObject(REMOVED_CASE_LIST, removedCases);
        bulkCaseData.put(CASE_LIST_KEY, finalCases);
        return bulkCaseData;
    }

    private String getCaseIdFromBulkCaseData(Map<String, Object> bulkCaseData) {
        return getCaseLinkValue(getElementFromCollection(bulkCaseData), CASE_REFERENCE_FIELD);
    }

    private String getCaseIdFromAcceptedCase(Map<String, Object> bulkCaseData) {
        return getCaseLinkValue(bulkCaseData, VALUE_KEY);
    }
}
