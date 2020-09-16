package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdFields {

    public static final String SERVICE_APPLICATION_TYPE = "ServiceApplicationType";
    public static final String SERVICE_APPLICATION_GRANTED = "ServiceApplicationGranted";
    public static final String SERVICE_APPLICATION_DECISION_DATE = "ServiceApplicationDecisionDate";
    public static final String RECEIVED_SERVICE_APPLICATION_DATE = "ReceivedServiceApplicationDate";
    public static final String SERVICE_REFUSAL_DRAFT = "ServiceRefusalDraft";
    public static final String SERVICE_APPLICATION_REFUSAL_REASON = "ServiceApplicationRefusalReason";
    public static final String HELP_WITH_FEES_REF_NUMBER = "D8HelpWithFeesReferenceNumber";

    public static final String GENERAL_ORDERS = "GeneralOrders";
    public static final String GENERAL_ORDER_DRAFT = "GeneralOrderDraft";

    public static final String JUDGE_TYPE = "JudgeType";
    public static final String JUDGE_NAME = "JudgeName";
    public static final String GENERAL_ORDER_DETAILS = "GeneralOrderDetails";
    public static final String GENERAL_ORDER_DATE = "GeneralOrderDate";
    public static final String GENERAL_ORDER_RECITALS = "GeneralOrderRecitals";
    public static final String GENERAL_EMAIL_DETAILS = "GeneralEmailDetails";
    public static final String CO_RESPONDENT_LINKED_TO_CASE = "CoRespLinkedToCase";
    public static final String CO_RESPONDENT_SOLICITOR_EMAIL_ADDRESS = "CoRespondentSolicitorEmail";
    public static final String CO_RESPONDENT_EMAIL_ADDRESS = "CoRespEmailAddress";

    public static final String GENERAL_EMAIL_OTHER_RECIPIENT_NAME = "GeneralEmailOtherRecipientName";
    public static final String GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL = "GeneralEmailOtherRecipientEmail";

    public static final String PETITIONER_FIRST_NAME = OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
    public static final String PETITIONER_LAST_NAME = OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
    public static final String PETITIONER_EMAIL = OrchestrationConstants.D_8_PETITIONER_EMAIL;
    public static final String GENERAL_EMAIL_PARTIES = "GeneralEmailParties";
}