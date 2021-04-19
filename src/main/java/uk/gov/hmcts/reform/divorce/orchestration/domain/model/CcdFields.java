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
    public static final String LAST_SERVICE_APPLICATION = "LastServiceApplication";
    public static final String LAST_SERVICE_APPLICATION_TYPE = "LastServiceApplicationType";
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

    public static final String RESPONDENT_FIRST_NAME = OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
    public static final String RESPONDENT_LAST_NAME = OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

    public static final String PBA_NUMBERS = "PbaNumbers";
    public static final String FURTHER_HWF_REFERENCE = "HelpWithFeesReferenceNumber";
    public static final String FURTHER_PBA_REFERENCE = "FeeAccountReferenceNumber";
    public static final String FURTHER_HWF_REFERENCE_NUMBERS = "FurtherHWFReferenceNumbers";
    public static final String FURTHER_PBA_REFERENCE_NUMBERS = "FurtherPBAReferenceNumbers";

    public static final String GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY = "generalApplicationWithoutNoticeFeeSummary";
    public static final String GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY = "generalReferralWithoutNoticeFeeSummary";

    public static final String SERVED_BY_PROCESS_SERVER = "ServedByProcessServer";
    public static final String SERVED_BY_ALTERNATIVE_METHOD = "ServedByAlternativeMethod";

    public static final String PETITIONER_SOLICITOR_ORGANISATION_POLICY = "PetitionerOrganisationPolicy";
    public static final String RESPONDENT_SOLICITOR_ORGANISATION_POLICY = "RespondentOrganisationPolicy";
    public static final String RESPONDENT_SOLICITOR_NAME = "D8RespondentSolicitorName";
    public static final String RESPONDENT_SOLICITOR_REFERENCE = "respondentSolicitorReference";
    public static final String RESPONDENT_SOLICITOR_PHONE = "D8RespondentSolicitorPhone";
    public static final String RESPONDENT_SOLICITOR_EMAIL = "D8RespondentSolicitorEmail";
    public static final String RESPONDENT_SOLICITOR_ADDRESS = "D8DerivedRespondentSolicitorAddr";
    public static final String PETITIONER_SOLICITOR_FIRM = "PetitionerSolicitorFirm";

    public static final String BAILIFF_APPLICATION_GRANTED = "BailiffApplicationGranted";
    public static final String CERTIFICATE_OF_SERVICE_DOCUMENT = "CertificateOfServiceDocument";
    public static final String CERTIFICATE_OF_SERVICE_DATE = "CertificateOfServiceDate";
    public static final String LOCAL_COURT_ADDRESS = "LocalCourtAddress";
    public static final String LOCAL_COURT_EMAIL = "LocalCourtEmail";
    public static final String REASON_FAILURE_TO_SERVE = "ReasonFailureToServe";
    public static final String BAILIFF_SERVICE_SUCCESSFUL = "SuccessfulServedByBailiff";

    public static final String NOTICE_OF_PROCEEDINGS_DIGITAL = "DigitalNoticeOfProceedings";
    public static final String NOTICE_OF_PROCEEDINGS_EMAIL = "NoticeOfProceedingsEmail";
    public static final String NOTICE_OF_PROCEEDINGS_FIRM = "NoticeOfProceedingsSolicitorFirm";

    public static final String JUDGE_COSTS_DECISION = "JudgeCostsDecision";

    public static final String RESPONDENT_SOLICITOR_DIGITAL = "RespSolDigital";
}
