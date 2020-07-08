package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import java.util.stream.Stream;

public class PreviousAmendPetitionStateLoggerHelper {
    public static final String INVALID_STATE = "Invalid state";
    public static final String AWAITING_HWF_DECISION = "AwaitingHWFDecision";
    public static final String SUBMITTED = "Submitted";
    public static final String ISSUED = "Issued";
    public static final String REJECTED = "Rejected";
    public static final String PENDING_REJECTION = "PendingRejection";
    public static final String SOLICITOR_AWAITING_PAYMENT_CONFIRMATION = "solicitorAwaitingPaymentConfirmation";
    public static final String AOS_AWAITING = "AosAwaiting";
    public static final String AOS_STARTED = "AosStarted";
    public static final String AOS_OVERDUE = "AosOverdue";
    public static final String AWAITING_REISSUE = "AwaitingReissue";
    public static final String AOS_COMPLETED = "AosCompleted";
    public static final String AOS_AWAITING_SOLICITOR = "AosAwaitingSol";
    public static final String AOS_PRE_SUBMITTED = "AosPreSubmit";
    public static final String AOS_DRAFTED = "AosDrafted";
    public static final String AWAITING_SERVICE = "AwaitingService";
    public static final String AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    public static final String AWAITING_DECREE_NISI = "AwaitingDecreeNisi";

    public static String getAmendPetitionPreviousState(String stateId) {
        return getAmendPetitionPreStates()
            .filter(state -> state.equalsIgnoreCase(stateId))
            .findFirst()
            .orElse(INVALID_STATE);
    }

    private static Stream<String> getAmendPetitionPreStates() {
        return Stream.of(
            AWAITING_HWF_DECISION,
            SUBMITTED,
            ISSUED,
            REJECTED,
            PENDING_REJECTION,
            SOLICITOR_AWAITING_PAYMENT_CONFIRMATION,
            SOLICITOR_AWAITING_PAYMENT_CONFIRMATION,
            AOS_AWAITING,
            AOS_STARTED,
            AOS_OVERDUE,
            AWAITING_REISSUE,
            AOS_COMPLETED,
            AOS_AWAITING_SOLICITOR,
            AOS_PRE_SUBMITTED,
            AOS_DRAFTED,
            AWAITING_SERVICE,
            AOS_SUBMITTED_AWAITING_ANSWER,
            AWAITING_DECREE_NISI
        );
    }
}
