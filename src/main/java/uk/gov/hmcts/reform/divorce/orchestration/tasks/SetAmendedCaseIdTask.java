package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMENDED_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetAmendedCaseIdTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String oldCaseId = context.getTransientObject(CASE_ID_JSON_KEY);
        String newCaseId = caseData.get(ID).toString();
        log.info("Updating old case id {} to add a link to the new case id {}", oldCaseId, newCaseId);

        Map<String, Object> oldCase = context.getTransientObject(CCD_CASE_DATA);

        oldCase.put(AMENDED_CASE_ID_CCD_KEY, CaseLink.builder().caseReference(newCaseId).build());

        return caseData;
    }

}