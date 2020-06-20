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
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.formatDerivedRespondentCorrespondenceAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.formatDerivedRespondentHomeAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.formatDerivedRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.isRespondentCorrespondenceAddressPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Slf4j
@Component
public class RespondentAosDerivedAddressFormatterTask implements Task<Map<String, Object>> {

    private static final String FORMAT_TEMPLATE = "{} formatted for Case ID {}";
    private static final String UPDATE_TEMPLATE = "{} for Case ID {} set to {}";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);

        if (isRespondentRepresented(caseData)) {
            caseData.put(D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS, formatDerivedRespondentSolicitorAddress(caseData));
            logFormatting(caseId, D8_DERIVED_RESPONDENT_SOLICITOR_ADDRESS);
        } else {
            caseData.put(D8_DERIVED_RESPONDENT_HOME_ADDRESS, formatDerivedRespondentHomeAddress(caseData));
            logFormatting(caseId, D8_DERIVED_RESPONDENT_HOME_ADDRESS);
        }

        if (isRespondentCorrespondenceAddressPopulated(caseData)) {
            caseData.put(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS, NO_VALUE);
            logUpdate(caseId, NO_VALUE);
        } else {
            caseData.put(D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS, YES_VALUE);
            logUpdate(caseId, YES_VALUE);
        }

        caseData.put(D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS, formatDerivedRespondentCorrespondenceAddress(caseData));
        logFormatting(caseId, D8_DERIVED_RESPONDENT_CORRESPONDENCE_ADDRESS);

        return caseData;
    }

    private void logFormatting(String caseId, String d8DerivedRespondentHomeAddress) {
        log.info(FORMAT_TEMPLATE, d8DerivedRespondentHomeAddress, caseId);
    }

    private void logUpdate(String caseId, String yesValue) {
        log.info(UPDATE_TEMPLATE, D8_RESPONDENT_CORRESPONDENCE_USE_HOME_ADDRESS, caseId, yesValue);
    }
}
