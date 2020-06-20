package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.formatDerivedRespondentCorrespondenceAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.formatDerivedRespondentHomeAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.formatDerivedRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.AosPackOfflineDerivedAddressFormatterHelper.isRespondentCorrespondenceAddressPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Slf4j
@Component
public class RespondentAosDerivedAddressFormatterTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);

        if (isRespondentRepresented(caseData)) {
            caseData.put(D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS, formatDerivedRespondentSolicitorAddress(caseData));
            log.info("{} formatted for Case ID {}", D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS, caseId);
        } else {
            caseData.put(D8_DERIVED_RESPONDENT_HOME_ADDRESS, formatDerivedRespondentHomeAddress(caseData));
            log.info("{} formatted for Case ID {}", D8_DERIVED_RESPONDENT_HOME_ADDRESS, caseId);
        }

        if (isRespondentCorrespondenceAddressPopulated(caseData)) {
            caseData.put(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS, NO_VALUE);
        } else {
            caseData.put(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS, YES_VALUE);
        }

        caseData.put(D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS, formatDerivedRespondentCorrespondenceAddress(caseData));
        log.info("{} formatted for Case ID {}", D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS, caseId);

        return caseData;
    }
}
