package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_AOS_RECEIVED_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationForDefendedDivorceEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "respondent submission notification email - defended divorce";

    private static final String EAST_MIDLANDS_DIVORCE_UNIT = "eastMidlands";
    private static final int LIMIT_IN_DAYS_FOR_FORM_SUBMISSION = 21;
    private static final DateTimeFormatter CLIENT_FACING_DATE_FORMAT = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.UK);

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataPayload) throws TaskException {
        String respondentEmailAddress = getMandatoryPropertyValueAsString(caseDataPayload, RESPONDENT_EMAIL_ADDRESS);

        Map<String, String> templateFields = new HashMap<>();
        String respondentFirstName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_LAST_NAME_CCD_FIELD);
        String petitionerInferredGender = getMandatoryPropertyValueAsString(caseDataPayload,
                D_8_INFERRED_PETITIONER_GENDER);
        String petitionerRelationshipToRespondent = getRelationshipTermByGender(petitionerInferredGender);
        String divorceUnit = EAST_MIDLANDS_DIVORCE_UNIT;
        Court court = taskCommons.getCourt(divorceUnit);

        String caseId = getCaseId(context);
        templateFields.put("case number", formatCaseIdToReferenceNumber(caseId));

        templateFields.put("email address", respondentEmailAddress);
        templateFields.put("first name", respondentFirstName);
        templateFields.put("last name", respondentLastName);
        templateFields.put("husband or wife", petitionerRelationshipToRespondent);
        templateFields.put("RDC name", court.getDivorceCentreName());
        templateFields.put("court address", court.getFormattedAddress());

        String formSubmissionDateLimit = getFormSubmissionDateLimit(caseDataPayload);
        templateFields.put("form submission date limit", formSubmissionDateLimit);

        taskCommons.sendEmail(RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION,
                EMAIL_DESCRIPTION,
                respondentEmailAddress,
                templateFields);

        return caseDataPayload;
    }

    private String getFormSubmissionDateLimit(Map<String, Object> payload) throws TaskException {
        String dateAsString = getMandatoryPropertyValueAsString(payload, DATE_AOS_RECEIVED_FROM_RESP);
        LocalDate dateAOSReceivedFromRespondent = LocalDate.parse(dateAsString);
        LocalDate limitDate = dateAOSReceivedFromRespondent.plusDays(LIMIT_IN_DAYS_FOR_FORM_SUBMISSION);
        return limitDate.format(CLIENT_FACING_DATE_FORMAT);
    }

}