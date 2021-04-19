package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask extends SendRespondentSubmissionNotification {

    private static final String EMAIL_DESCRIPTION = "respondent submission notification email - undefended divorce";

    public SendRespondentSubmissionNotificationForUndefendedDivorceEmailTask(CcdUtil ccdUtil,
                                                                             TaskCommons taskCommons,
                                                                             TemplateConfigService templateConfigService) {
        super(ccdUtil, taskCommons, templateConfigService);
    }

    @Override
    public String getEmailDescription() {
        return EMAIL_DESCRIPTION;
    }

    @Override
    public EmailTemplateNames getEmailTemplateName() {
        return RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;
    }
}
