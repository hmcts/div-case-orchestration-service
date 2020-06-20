package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.formatDerivedCoRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.formatDerivedReasonForDivorceAdultery3rdAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Slf4j
@Component
public class CoRespondentAosDerivedAddressFormatterTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);

        if (isCoRespondentRepresented(caseData)) {
            caseData.put(CO_RESPONDENT_SOLICITOR_ADDRESS, formatDerivedCoRespondentSolicitorAddress(caseData));
            log.info("Derived CoRespondent Solicitor Address formatted for Case ID: {}", caseId);
        } else {
            caseData.put(CO_RESPONDENT_ADDRESS, formatDerivedReasonForDivorceAdultery3rdAddress(caseData));
            log.info("Derived CoRespondent Address formatted for Case ID: {}", caseId);
        }

        return caseData;
    }

}
