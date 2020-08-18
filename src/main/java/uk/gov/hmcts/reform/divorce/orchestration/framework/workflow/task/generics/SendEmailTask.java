package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public abstract class SendEmailTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    public SendEmailTask(EmailService emailService) {
        this.emailService = emailService;
    }

    protected abstract String getSubject();

    protected abstract Map<String, String> getPersonalisation(Map<String, Object> caseData);

    protected abstract EmailTemplateNames getTemplate();

    protected String getRecipientEmail(Map<String, Object> caseData) {
        return CaseDataExtractor.getPetitionerEmail(caseData);
    }

    protected LanguagePreference getLanguage(Map<String, Object> caseData) {
        return CaseDataUtils.getLanguagePreference(caseData);
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String caseId = getCaseId(context);
        final String subject = getSubject();

        log.info("CaseId: {} email {} is going to be sent.", caseId, subject);

        emailService.sendEmail(
            getRecipientEmail(caseData),
            getTemplate().name(),
            getPersonalisation(caseData),
            subject,
            LanguagePreference.ENGLISH
        );

        log.info("CaseId: {} email {} was sent.", caseId, subject);

        return caseData;
    }
}
