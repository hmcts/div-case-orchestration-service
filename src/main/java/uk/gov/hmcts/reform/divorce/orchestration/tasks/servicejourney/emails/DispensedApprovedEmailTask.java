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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
public class DispensedApprovedEmailTask extends SendEmailTask {
    protected static String SOLICITOR_SUBJECT = "%s vs %s: Dispense with service application has been approved";
    protected static String CITIZEN_SUBJECT = "Your ‘dispense with service’ application has been approved";

    public DispensedApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        if (isPetitionerRepresented(caseData)) {
            return SOLICITOR_SUBJECT;
        } else {
            return CITIZEN_SUBJECT;
        }
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        if (isPetitionerRepresented(caseData)) {
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
                NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
                NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
                NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
            );
        } else {
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
            );
        }
    }

    @Override
    protected EmailTemplateNames getTemplate(Map<String, Object> caseData) {
        if (isPetitionerRepresented(caseData)) {
            return EmailTemplateNames.SOL_DISPENSED_APPROVED;
        } else {
            return EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;
        }
    }
}
