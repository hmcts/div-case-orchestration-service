package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdFields {

    public static final String DUE_DATE = "dueDate";

    public static final String SERVICE_APPLICATION_TYPE = "ServiceApplicationType";
    public static final String SERVICE_APPLICATION_PAYMENT = "ServiceApplicationPayment";
    public static final String SERVICE_APPLICATION_GRANTED = "ServiceApplicationGranted";
    public static final String SERVICE_APPLICATION_DECISION_DATE = "ServiceApplicationDecisionDate";
    public static final String RECEIVED_SERVICE_APPLICATION_DATE = "ReceivedServiceApplicationDate";
    public static final String RECEIVED_SERVICE_ADDED_DATE = "ReceivedServiceAddedDate";
    public static final String SERVICE_REFUSAL_DRAFT = "ServiceRefusalDraft";
    public static final String SERVICE_APPLICATIONS = "ServiceApplications";
    public static final String SERVICE_APPLICATION_DOCUMENTS = "ServiceApplicationDocuments";
    public static final String SERVICE_APPLICATION_REFUSAL_REASON = "ServiceApplicationRefusalReason";
    public static final String HELP_WITH_FEES_REF_NUMBER = "D8HelpWithFeesReferenceNumber";

    public static final String JUDGE_TYPE = "JudgeType";
    public static final String JUDGE_NAME = "JudgeName";

    public static final String GENERAL_ORDERS = "GeneralOrders";
    public static final String GENERAL_ORDER_DRAFT = "GeneralOrderDraft";
    public static final String GENERAL_ORDER_DETAILS = "GeneralOrderDetails";
    public static final String GENERAL_ORDER_DATE = "GeneralOrderDate";
    public static final String GENERAL_ORDER_RECITALS = "GeneralOrderRecitals";
    public static final String GENERAL_ORDER_PARTIES = "GeneralOrderParties";

    public static final String CO_RESPONDENT_LINKED_TO_CASE = OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
    public static final String CO_RESPONDENT_SOLICITOR_EMAIL_ADDRESS = "CoRespondentSolicitorEmail";
    public static final String CO_RESPONDENT_EMAIL_ADDRESS = OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;

    public static final String GENERAL_EMAIL_DETAILS = "GeneralEmailDetails";
    public static final String GENERAL_EMAIL_OTHER_RECIPIENT_NAME = "GeneralEmailOtherRecipientName";
    public static final String GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL = "GeneralEmailOtherRecipientEmail";
    public static final String GENERAL_EMAIL_PARTIES = "GeneralEmailParties";

    public static final String GENERAL_REFERRALS = "GeneralReferrals";
    public static final String GENERAL_REFERRAL_FEE = "GeneralReferralFee";
    public static final String GENERAL_REFERRAL_DECISION_DATE = "GeneralReferralDecisionDate";
    public static final String GENERAL_REFERRAL_REASON = "GeneralReferralReason";
    public static final String GENERAL_REFERRAL_TYPE = "GeneralReferralType";
    public static final String GENERAL_REFERRAL_DETAILS = "GeneralReferralDetails";
    public static final String GENERAL_REFERRAL_PAYMENT_TYPE = "GeneralReferralPaymentType";
    public static final String GENERAL_REFERRAL_DECISION = "GeneralReferralDecision";
    public static final String GENERAL_REFERRAL_DECISION_REASON = "GeneralReferralDecisionReason";
    public static final String GENERAL_APPLICATION_ADDED_DATE = "GeneralApplicationAddedDate";
    public static final String GENERAL_APPLICATION_FROM = "GeneralApplicationFrom";
    public static final String GENERAL_APPLICATION_REFERRAL = "generalApplicationReferral";
    public static final String GENERAL_APPLICATION_REFERRAL_DATE = "GeneralApplicationReferralDate";
    public static final String ALTERNATIVE_SERVICE_APPLICATION = "alternativeServiceApplication";
    public static final String ALTERNATIVE_SERVICE_MEDIUM = "AlternativeServiceMedium";
    public static final String FEE_AMOUNT_WITHOUT_NOTICE = "FeeAmountWithoutNotice";
    public static final String GENERAL_REFERRAL_PREVIOUS_CASE_STATE = "StateBeforeGeneralReferral";

    public static final String PETITIONER_FIRST_NAME = OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
    public static final String PETITIONER_LAST_NAME = OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
    public static final String PETITIONER_EMAIL = OrchestrationConstants.D_8_PETITIONER_EMAIL;

    public static final String PBA_NUMBERS = "PbaNumbers";
    public static final String FURTHER_HWF_REFERENCE = "HelpWithFeesReferenceNumber";
    public static final String FURTHER_PBA_REFERENCE = "FeeAccountReferenceNumber";
    public static final String FURTHER_HWF_REFERENCE_NUMBERS = "FurtherHWFReferenceNumbers";
    public static final String FURTHER_PBA_REFERENCE_NUMBERS = "FurtherPBAReferenceNumbers";

    public static final String GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY = "generalApplicationWithoutNoticeFeeSummary";
    public static final String GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY = "generalReferralWithoutNoticeFeeSummary";

    public static final String SERVED_BY_PROCESS_SERVER = "ServedByProcessServer";
    public static final String SERVED_BY_ALTERNATIVE_METHOD = "ServedByAlternativeMethod";

}