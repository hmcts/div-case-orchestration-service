package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import uk.gov.hmcts.reform.bsp.common.utils.LetterAddressHelper;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

public class CoRespondentAosDerivedAddressFormatterTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        if (isCoRespondentRepresented(caseData)) {
            caseData.put(CO_RESPONDENT_SOLICITOR_ADDRESS, formatDerivedCoRespondentSolicitorAddr(caseData));
        } else {
            caseData.put(CO_RESPONDENT_ADDRESS, formatDerivedReasonForDivorceAdultery3rdAddress(caseData));
        }

        return caseData;
    }


    String formatDerivedCoRespondentSolicitorAddr(Map<String, Object> caseData) {
        return Optional.ofNullable((Map<String, Object>) caseData.get(D8_CO_RESPONDENT_SOLICITOR_ADDRESS))
            .map(address -> LetterAddressHelper.formatAddressForLetterPrinting(address))
            .orElse(null);
    }

    String formatDerivedReasonForDivorceAdultery3rdAddress(Map<String, Object> caseData) {
        return Optional.ofNullable((Map<String, Object>) caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS))
            .map(address -> LetterAddressHelper.formatAddressForLetterPrinting(address))
            .orElse(null);
    }

}
