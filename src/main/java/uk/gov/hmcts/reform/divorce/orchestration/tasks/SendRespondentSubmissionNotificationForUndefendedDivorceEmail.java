package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationForUndefendedDivorceEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "respondent submission notification email - undefended divorce";

    @Autowired
    private EmailService emailService;

    @Autowired
    private CourtLookupService courtLookupService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataPayload) throws TaskException {
        String respondentEmailAddress = (String) caseDataPayload.get(RESPONDENT_EMAIL_ADDRESS);

        Map<String, String> templateFields = new HashMap<>();
        String respondentFirstName = (String) caseDataPayload.get(RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = (String) caseDataPayload.get(RESP_LAST_NAME_CCD_FIELD);
        String petitionerInferredGender = (String) caseDataPayload.get(D_8_INFERRED_PETITIONER_GENDER);
        String petitionerRelationshipToRespondent = getRelationshipTermByGender(petitionerInferredGender);
        Court court = getCourt((String) caseDataPayload.get(DIVORCE_UNIT_JSON_KEY));

        templateFields.put("email address", respondentEmailAddress);
        templateFields.put("first name", respondentFirstName);
        templateFields.put("last name", respondentLastName);
        templateFields.put("husband or wife", petitionerRelationshipToRespondent);
        templateFields.put("RDC name", court.getDivorceCentreName());

        try {
            emailService.sendEmail(RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION,
                    EMAIL_DESCRIPTION,
                    respondentEmailAddress,
                    templateFields);
        } catch (NotificationClientException e) {
            TaskException taskException = new TaskException("Failed to send e-mail", e);
            log.error(taskException.getMessage(), e);
            throw taskException;
        }

        return caseDataPayload;
    }

    private Court getCourt(String divorceUnitKey) throws TaskException {
        try {
            return courtLookupService.getCourtByKey(divorceUnitKey);
        } catch (CourtDetailsNotFound courtDetailsNotFound) {
            throw new TaskException(courtDetailsNotFound);
        }
    }

}