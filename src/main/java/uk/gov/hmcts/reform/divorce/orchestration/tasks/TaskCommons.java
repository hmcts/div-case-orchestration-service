package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Component
@Slf4j
public class TaskCommons {

    @Autowired
    private CourtLookupService courtLookupService;

    @Autowired
    private EmailService emailService;

    public Court getCourt(String divorceUnitKey) throws TaskException {
        try {
            return courtLookupService.getCourtByKey(divorceUnitKey);
        } catch (CourtDetailsNotFound courtDetailsNotFound) {
            throw new TaskException(courtDetailsNotFound);
        }
    }

    public DnCourt getDnCourt(String courtId) throws CourtDetailsNotFound {
        return courtLookupService.getDnCourtByKey(courtId);
    }

    /**
     * Please use GenericEmailNotification as it's a self contained task, instead of this "Common" task.
     */
    public void sendEmail(EmailTemplateNames emailTemplate,
                          String emailDescription,
                          String destinationEmailAddress,
                          Map<String, String> templateParameters,
                          LanguagePreference languagePreference) throws TaskException {
        try {
            emailService.sendEmailAndReturnExceptionIfFails(destinationEmailAddress,
                emailTemplate.name(),
                templateParameters,
                emailDescription,
                languagePreference);
        } catch (NotificationClientException e) {
            log.error(e.getMessage(), e);
            throw new TaskException("Failed to send e-mail", e);
        }
    }
}
