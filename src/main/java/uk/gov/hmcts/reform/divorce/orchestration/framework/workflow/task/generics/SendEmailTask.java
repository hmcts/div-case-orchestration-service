package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCaseReference;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCaseReferenceOptional;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class SendEmailTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    protected abstract Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData);

    protected abstract EmailTemplateNames getTemplate();

    protected abstract String getRecipientEmail(Map<String, Object> caseData);

    /**
     * This method is only use to produce msg for log. It's not sent to external api
     * */
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        String caseId;
        try {
            caseId = getCaseId(context);
        } catch (Exception exception) {
            caseId = getCaseReferenceOptional(caseData);
        }

        return String.format("CaseId: %s, email template %s", caseId, getTemplate().name());
    }

    protected LanguagePreference getLanguage(Map<String, Object> caseData) {
        return CaseDataUtils.getLanguagePreference(caseData);
    }

    protected boolean canEmailBeSent(Map<String, Object> caseData) {
        return true;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String caseId = getCaseId(context);
        final String subject = getSubject(context, caseData);

        if (canEmailBeSent(caseData)) {
            log.info("CaseID: {} email {} is going to be sent.", caseId, subject);

            emailService.sendEmail(
                getRecipientEmail(caseData),
                getTemplate().name(),
                getPersonalisation(context, caseData),
                subject,
                getLanguage(caseData)
            );

            log.info("CaseID: {} email {} was sent.", caseId, getTemplate().name());
        } else {
            log.warn("CaseID: {} email {} will not be sent.", caseId, getTemplate().name());
        }

        return caseData;
    }
}

