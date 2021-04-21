package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationForDefendedDivorceEmailTask extends SendRespondentSubmissionNotification {

    private static final String EMAIL_DESCRIPTION = "respondent submission notification email - defended divorce";

    public SendRespondentSubmissionNotificationForDefendedDivorceEmailTask(CcdUtil ccdUtil,
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
        return RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;
    }

    @Override
    protected Map<String, String> getAdditionalTemplateFields(Court court, Map<String, Object> caseDataPayload) {
        Map<String, String> templateFields = new HashMap<>();
        templateFields.put(NOTIFICATION_COURT_ADDRESS_KEY, court.getFormattedAddress());

        String formSubmissionDateLimit = ccdUtil.getFormattedDueDate(caseDataPayload, CcdFields.DUE_DATE);
        templateFields.put(NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, formSubmissionDateLimit);

        String welshFormSubmissionDateLimit = ccdUtil.getWelshFormattedDate(caseDataPayload, CcdFields.DUE_DATE);
        templateFields.put(OrchestrationConstants.NOTIFICATION_WELSH_FORM_SUBMISSION_DATE_LIMIT_KEY, welshFormSubmissionDateLimit);

        return templateFields;
    }
}