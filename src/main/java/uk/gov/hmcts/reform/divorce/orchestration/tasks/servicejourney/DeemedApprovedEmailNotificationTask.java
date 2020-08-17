package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeemedApprovedEmailNotificationTask implements Task<Map<String, Object>> {

    public static final String SUBJECT = "Your ‘deemed service’ application has been approved";

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String caseId = getCaseId(context);

        log.info("CaseId: {} Deemed Approved email is going to be sent", caseId);

        Map<String, String> notificationTemplateVars = ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
        );

        emailService.sendEmail(
            CaseDataExtractor.getPetitionerEmail(caseData),
            EmailTemplateNames.CITIZEN_DEEMED_APPROVED.name(),
            notificationTemplateVars,
            SUBJECT,
            LanguagePreference.ENGLISH
        );

        log.info("CaseId: {} Deemed Approved email was sent", caseId);

        return caseData;
    }
}
