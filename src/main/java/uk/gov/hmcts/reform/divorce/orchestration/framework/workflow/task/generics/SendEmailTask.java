package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class SendEmailTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    protected abstract String getSubject(Map<String, Object> caseData);

    protected abstract Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData);

    protected abstract EmailTemplateNames getTemplate(Map<String, Object> caseData);

    protected String getRecipientEmail(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData)
            ? CaseDataExtractor.getPetitionerSolicitorEmail(caseData)
            : CaseDataExtractor.getPetitionerEmail(caseData);
    }

    protected LanguagePreference getLanguage(Map<String, Object> caseData) {
        return CaseDataUtils.getLanguagePreference(caseData);
    }

    protected boolean canEmailBeSent(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? true : isPetitionerEmailPopulated(caseData);
    }

    protected boolean isPetitionerEmailPopulated(Map<String, Object> caseData) {
        return !CaseDataExtractor.getPetitionerEmailOrEmpty(caseData).isEmpty();
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String caseId = getCaseId(context);
        final String subject = getSubject(caseData);

        if (canEmailBeSent(caseData)) {
            log.info("CaseID: {} email {} is going to be sent.", caseId, subject);

            emailService.sendEmail(
                getRecipientEmail(caseData),
                getTemplate(caseData).name(),
                getPersonalisation(context, caseData),
                subject,
                getLanguage(caseData)
            );

            log.info("CaseID: {} email {} was sent.", caseId, getTemplate(caseData).name());
        } else {
            log.warn("CaseID: {} recipient email is empty! Email {} not sent.", caseId, getTemplate(caseData).name());
        }

        return caseData;
    }
}
