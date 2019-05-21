package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationForUndefendedDivorceEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "respondent submission notification email - undefended divorce";

    private static final String EMAIL_ADDRESS = "email address";
    private static final String CASE_NUMBER = "case number";
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final String HUSBAND_OR_WIFE = "husband or wife";
    private static final String RDC_NAME = "RDC name";

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
        String divorceUnitKey = getMandatoryPropertyValueAsString(caseDataPayload, DIVORCE_UNIT_JSON_KEY);
        Court court = taskCommons.getCourt(divorceUnitKey);

        String caseId = getMandatoryPropertyValueAsString(caseDataPayload, D_8_CASE_REFERENCE);
        templateFields.put(CASE_NUMBER, caseId);
        templateFields.put(EMAIL_ADDRESS, respondentEmailAddress);
        templateFields.put(FIRST_NAME, respondentFirstName);
        templateFields.put(LAST_NAME, respondentLastName);
        templateFields.put(HUSBAND_OR_WIFE, petitionerRelationshipToRespondent);
        templateFields.put(RDC_NAME, court.getIdentifiableCentreName());

        taskCommons.sendEmail(RESPONDENT_UNDEFENDED_AOS_SUBMISSION_NOTIFICATION,
                EMAIL_DESCRIPTION,
                respondentEmailAddress,
                templateFields);

        return caseDataPayload;
    }
}
