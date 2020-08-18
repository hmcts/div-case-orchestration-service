package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;

@Component
@Slf4j
public class DispensedApprovedEmailTask extends SendEmailTask {

    private static final String SUBJECT = "Your ‘dispense with service’ application has been approved\n";

    public DispensedApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject() {
        return SUBJECT;
    }

    @Override
    protected Map<String, String> getPersonalisation(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;
    }

    @Override
    protected LanguagePreference getLanguage(Map<String, Object> caseData) {
        return LanguagePreference.ENGLISH;
    }
}
