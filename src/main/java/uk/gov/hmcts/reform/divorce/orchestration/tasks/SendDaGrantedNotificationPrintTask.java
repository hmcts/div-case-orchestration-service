package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Component
@Slf4j
public class SendDaGrantedNotificationPrintTask implements Task<Map<String, Object>> {

    private static final String CO_RESP_LETTER_DESCRIPTION = "TEMP TEXT";
    private static final String SOL_LETTER_DESCRIPTION = "SOL TEMP TEXT";
    private  static final String ERROR_MESSAGE = "Error sending DA Granted notification letter to ";

    private final BulkPrintService bulkPrintService;

    @Autowired
    public SendDaGrantedNotificationPrintTask(BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        if (isCoRespondentRepresented(caseData)) {
            sendLetterToCoRespondentSolicitor(caseData);
        } else {
            sendLetterToCoRespondent(caseData);
        }

        return caseData;
    }

    private void sendLetterToCoRespondentSolicitor(Map<String, Object> caseData) throws TaskException {
        String role = "Co-Respondent Solicitor";
        try {
            sendLetter(
                caseData,
                getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE),
                getMandatoryPropertyValueAsString(caseData, getFirstName(CO_RESPONDENT_SOLICITOR_NAME)),
                getMandatoryPropertyValueAsString(caseData, getLastName(CO_RESPONDENT_SOLICITOR_NAME)),
                getMandatoryPropertyValueAsString(caseData, CO_RESPONDENT_SOLICITOR_ADDRESS)
            );
        } catch (NotificationClientException e) {
            log.error(ERROR_MESSAGE + role, e);
            throw new TaskException(e.getMessage(), e);
        }
    }

    private void sendLetterToCoRespondent(Map<String, Object> caseData) throws TaskException {
        String role = "Co-Respondent";
        try {
            sendLetter(
                caseData,
                getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE),
                getMandatoryPropertyValueAsString(caseData, getFirstName(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME)),
                getMandatoryPropertyValueAsString(caseData, getLastName(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME)),
                getMandatoryPropertyValueAsString(caseData, CO_RESPONDENT_SOLICITOR_ADDRESS)
            );
        } catch (NotificationClientException e) {
            log.error(ERROR_MESSAGE + role, e);
            throw new TaskException(e.getMessage(), e);
        }
    }

    private void sendLetter(Map<String, Object> caseData, String ccdReference, String firstName, String lastName, String address) throws NotificationClientException {

        String daGrantedDataCcdField = (String) caseData.get(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD);
        LocalDate daGrantedDate = LocalDateTime.parse(daGrantedDataCcdField).toLocalDate();

        if (StringUtils.isNotBlank(address)) {
            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, ccdReference);

            templateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, emailAddress);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);

            bulkPrintService.send(
                emailAddress,
                EmailTemplateNames.DA_GRANTED_NOTIFICATION.name(),
                templateVars,
                CO_RESP_LETTER_DESCRIPTION);
        }

        // final String authToken, final String caseId, final String letterType, final List<String> documents
    }

    private String getFirstName(String fullname) {
        return fullname.split(" ")[0];
    }

    private String getLastName(String fullname) {
        return fullname.split(" ")[1];
    }



}