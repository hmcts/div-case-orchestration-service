package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getPetitionerSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class SolicitorDeemedApprovedEmailTask extends SendEmailTask {

    private static final String SUBJECT = "%s vs %s: Deemed service application has been approved";

    public SolicitorDeemedApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return format(SUBJECT, getPetitionerFullName(caseData), getRespondentFullName(caseData));
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getPetitionerSolicitorEmail(caseData);
    }

    @Override
    protected boolean canEmailBeSent(Map<String, Object> caseData) {
        /* this should always be sent as pet is represented so solicitor email must be there */
        return true;
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.PET_SOL_DEEMED_APPROVED;
    }
}
