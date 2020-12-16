package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdStates {
    public static final String SERVICE_APPLICATION_NOT_APPROVED = "ServiceApplicationNotApproved";
    public static final String AWAITING_BAILIFF_SERVICE = "AwaitingBailiffService";
    public static final String AWAITING_PAYMENT = "AwaitingPayment";
    public static final String AWAITING_HWF_DECISION = "AwaitingHWFDecision";
    public static final String BO_WELSH_RESPONSE_AWAITING_REVIEW = "WelshResponseAwaitingReview";
    public static final String AOS_AWAITING = "AosAwaiting";
    public static final String AOS_AWAITING_SOLICITOR = "AosAwaitingSol";
    public static final String AOS_COMPLETED = "AosCompleted";
    public static final String AOS_OVERDUE = "AosOverdue";
    public static final String AOS_STARTED = "AosStarted";
    public static final String AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    public static final String AWAITING_DECREE_NISI = "AwaitingDecreeNisi";
    public static final String AWAITING_LEGAL_ADVISOR_REFERRAL = "AwaitingLegalAdvisorReferral";
    public static final String AWAITING_PRONOUNCEMENT = "AwaitingPronouncement";
    public static final String AWAITING_CLARIFICATION = "AwaitingClarification";
    public static final String AWAITING_SERVICE_CONSIDERATION = "AwaitingServiceConsideration";
    public static final String AWAITING_REISSUE = "AwaitingReissue";
    public static final String DEFENDED = "DefendedDivorce";
    public static final String DN_PRONOUNCED = "DNPronounced";
    public static final String AWAITING_DA = "AwaitingDecreeAbsolute";
    public static final String DN_REFUSED = "DNisRefused";
    public static final String WELSH_LA_DECISION = "WelshLADecision";
    public static final String AWAITING_ADMIN_CLARIFICATION = "AwaitingAdminClarification";
    public static final String DA_REQUESTED = "DARequested";
    public static final String DIVORCE_GRANTED = "DivorceGranted";
    public static final String SUBMITTED = "Submitted";
    public static final String SOLICITOR_AWAITING_PAYMENT_CONFIRMATION = "solicitorAwaitingPaymentConfirmation";
    public static final String AWAITING_GENERAL_REFERRAL_PAYMENT = "AwaitingGeneralReferralPayment";
    public static final String AWAITING_GENERAL_CONSIDERATION = "AwaitingGeneralConsideration";
    public static final String GENERAL_CONSIDERATION_COMPLETE = "GeneralConsiderationComplete";
    public static final String AWAITING_ALTERNATIVE_SERVICE = "AwaitingAlternativeService";
    public static final String AWAITING_PROCESS_SERVER_SERVICE = "AwaitingProcessServerService";
    public static final String AWAITING_DWP_RESPONSE = "AwaitingDWPResponse";
}
