package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getRelationshipTermByGender;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPetitionerAmendEmailTask implements Task<Map<String, Object>> {
    private static final String EMAIL_DESCRIPTION = "Petitioner - You can amend your application";

    private final EmailService emailService;

    public static final String PRE_STATE_AWAITING_HWF_DECISION = "AwaitingHWFDecision";
    public static final String PRE_STATE_SUBMITTED = "Submitted";
    public static final String PRE_STATE_ISSUED = "Issued";
    public static final String PRE_STATE_REJECTED = "Rejected";
    public static final String PRE_STATE_PENDING_REJECTION = "PendingRejection";
    public static final String PRE_STATE_SOLICITOR_AWAITING_PAYMENT_CONFIRMATION = "solicitorAwaitingPaymentConfirmation";
    public static final String PRE_STATE_AOS_AWAITING = "AosAwaiting";
    public static final String PRE_STATE_AOS_STARTED = "AosStarted";
    public static final String PRE_STATE_AOS_OVERDUE = "AosOverdue";
    public static final String PRE_STATE_AWAITING_REISSUE = "AwaitingReissue";
    public static final String PRE_STATE_AOS_COMPLETED = "AosCompleted";
    public static final String PRE_STATE_AOS_AWAITING_SOLICITOR = "AosAwaitingSol";
    public static final String PRE_STATE_AOS_PRE_SUBMITTED = "AosPreSubmit";
    public static final String PRE_STATE_AOS_DRAFTED = "AosDrafted";
    public static final String PRE_STATE_AWAITING_SERVICE = "AwaitingService";
    public static final String PRE_STATE_AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    public static final String PRE_STATE_AWAITING_DECREE_NISI = "AwaitingDecreeNisi";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return sendAmendApplicationEmailToPetitioner(context, caseData);
    }

    private Map<String, Object> sendAmendApplicationEmailToPetitioner(TaskContext context, Map<String, Object> payload) throws TaskException {
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        String petitionerEmail = getMandatoryStringValue(payload, D_8_PETITIONER_EMAIL);
        String caseId = getCaseId(context);
        String eventId = caseDetails.getState();

        logEvent(caseId, eventId);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.PETITIONER_AMEND_APPLICATION.name(),
            getPersonalisation(context, payload),
            EMAIL_DESCRIPTION
        );

        return payload;
    }

    private Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();

        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryStringValue(payload, D_8_CASE_REFERENCE));
        personalisation.put(NOTIFICATION_PET_NAME, getPetitionerFullName(payload));
        personalisation.put(NOTIFICATION_FEES_KEY, getFormattedFeeAmount(context));
        personalisation.put(NOTIFICATION_HUSBAND_OR_WIFE, getHusbandOrWife(payload));

        return personalisation;
    }

    public static String printPreviousState(String eventId) {
        String[] preStates = {
            PRE_STATE_AWAITING_HWF_DECISION,
            PRE_STATE_SUBMITTED,
            PRE_STATE_ISSUED,
            PRE_STATE_REJECTED,
            PRE_STATE_PENDING_REJECTION,
            PRE_STATE_SOLICITOR_AWAITING_PAYMENT_CONFIRMATION,
            PRE_STATE_SOLICITOR_AWAITING_PAYMENT_CONFIRMATION,
            PRE_STATE_AOS_AWAITING,
            PRE_STATE_AOS_STARTED,
            PRE_STATE_AOS_OVERDUE,
            PRE_STATE_AWAITING_REISSUE,
            PRE_STATE_AOS_COMPLETED,
            PRE_STATE_AOS_AWAITING_SOLICITOR,
            PRE_STATE_AOS_PRE_SUBMITTED,
            PRE_STATE_AOS_DRAFTED,
            PRE_STATE_AWAITING_SERVICE,
            PRE_STATE_AOS_SUBMITTED_AWAITING_ANSWER,
            PRE_STATE_AWAITING_DECREE_NISI
        };

        for (String state: preStates) {
            if (state.equals(eventId))
                return state;
        }
        return "Not valid state";
    }

    public void logEvent(String caseId, String eventId) {
        log.info("CaseId: {}. " + EMAIL_DESCRIPTION + " Previous state " + printPreviousState(eventId) + ". Task executed", caseId);
    }

    private String getFormattedFeeAmount(TaskContext context) {
        return ((FeeResponse) context.getTransientObject(AMEND_PETITION_FEE_JSON_KEY)).getFormattedFeeAmount();
    }

    private String getHusbandOrWife(Map<String, Object> payload) throws TaskException {
        return getRelationshipTermByGender(getMandatoryPropertyValueAsString(payload,
            D_8_INFERRED_PETITIONER_GENDER));
    }
}
