package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_AGREES_EMAIL_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_DERIVED_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;

@Component
@RequiredArgsConstructor
public class CopyPetitionerSolicitorDetailsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Map<String, Object> oldCaseData = context.getTransientObject(CCD_CASE_DATA);

        copyFrom(PETITIONER_SOLICITOR_NAME, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_FIRM, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_DERIVED_ADDRESS, oldCaseData, caseData);
        copyFrom(SOLICITOR_REFERENCE_JSON_KEY, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_PHONE, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_EMAIL, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_AGREES_EMAIL_CONTACT, oldCaseData, caseData);
        copyFrom(PETITIONER_SOLICITOR_ORGANISATION_POLICY, oldCaseData, caseData);

        return caseData;
    }

    private void copyFrom(String caseField, Map<String, Object> oldCaseData, Map<String, Object> newCaseData) {
        newCaseData.put(caseField, oldCaseData.get(caseField));
    }

}