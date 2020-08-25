package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
public class DispensedApprovedEmailTask extends SendEmailTask {
    protected static String solicitorSubject = "%s vs %s: Dispense with service application has been approved";
    protected static String citizenSubject = "Your ‘dispense with service’ application has been approved";

    public DispensedApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? solicitorSubject : citizenSubject;
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? solicitorTemplateVariables(taskContext, caseData) : citizenTemplateVariables(caseData);
    }

    private ImmutableMap<String, String> citizenTemplateVariables(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
        );
    }

    private ImmutableMap<String, String> solicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? SOL_DISPENSED_APPROVED : CITIZEN_DISPENSED_APPROVED;
    }
}
