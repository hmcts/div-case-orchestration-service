package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

public class OrchestrationConstants {

    public static final String LINE_SEPARATOR = System.lineSeparator();

    // Authentication
    public static final String ACCESS_CODE = "access_code";
    public static final String AUTH_TOKEN_JSON_KEY = "authToken";
    public static final String BASIC = "Basic ";
    public static final String PIN_PREFIX = "Pin ";
    public static final String CODE = "code";
    public static final String LOCATION_HEADER = "Location";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    //Issue Petition
    public  static final String GENERATE_AOS_INVITATION = "generateAosInvitation";

    // Core Case Data
    public static final String CHECK_CCD = "checkCcd";
    public static final String CASE_DETAILS_JSON_KEY = "case_details";
    public static final String CASE_EVENT_DATA_JSON_KEY = "eventData";
    public static final String CASE_EVENT_ID_JSON_KEY = "eventId";
    public static final String CASE_ID_JSON_KEY = "caseId";
    public static final String CASE_STATE_JSON_KEY = "state";
    public static final String CREATED_DATE_JSON_KEY = "createdDate";
    public static final String D_8_DIVORCE_UNIT = "D8DivorceUnit";
    public static final String D_8_CASE_REFERENCE = "D8caseReference";
    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D_8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String D_8_INFERRED_RESPONDENT_GENDER = "D8InferredRespondentGender";
    public static final String D_8_INFERRED_PETITIONER_GENDER = "D8InferredPetitionerGender";
    public static final String D_8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String CCD_CASE_DATA = "ccdCaseData";
    public static final String CCD_CASE_DATA_FIELD = "case_data";
    public static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    public static final String LINK_RESPONDENT_GENERIC_EVENT_ID = "linkRespondent";
    public static final String START_AOS_EVENT_ID = "startAos";
    public static final String AWAITING_DN_AOS_EVENT_ID = "aosSubmittedUndefended";
    public static final String AWAITING_ANSWER_AOS_EVENT_ID = "aosSubmittedDefended";
    public static final String COMPLETED_AOS_EVENT_ID = "aosReceivedNoAdConStarted";
    public static final String RESP_ADMIT_OR_CONSENT_TO_FACT = "RespAdmitOrConsentToFact";
    public static final String RESP_WILL_DEFEND_DIVORCE = "RespWillDefendDivorce";
    public static final String RESP_FIRST_NAME_CCD_FIELD = "D8RespondentFirstName";
    public static final String RESP_LAST_NAME_CCD_FIELD = "D8RespondentLastName";
    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";
    public static final String ISSUE_DATE = "IssueDate";
    public static final String DN_RECEIVED = "dnReceived";
    public static final String RECEIVED_AOS_FROM_RESP = "ReceivedAOSfromResp";
    public static final String RECEIVED_AOS_FROM_RESP_DATE = "ReceivedAOSfromRespDate";
    public static final String AOS_AWAITING = "AosAwaiting";
    public static final String AOS_OVERDUE = "AosOverdue";
    public static final String AWAITING_REISSUE = "AwaitingReissue";
    public static final String AOS_START_FROM_OVERDUE = "startAosFromOverdue";
    public static final String AOS_START_FROM_REISSUE = "startAosFromReissue";
    public static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CCD_DUE_DATE = "dueDate";
    public static final String D_8_PAYMENTS = "Payments";

    // Divorce Session
    public static final String DIVORCE_SESSION_EXISTING_PAYMENTS = "existingPayments";

    public static final String ID = "id";
    public static final String PIN = "pin";
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";
    public static final String SUCCESS_STATUS = "success";

    // Reasons For Divorce
    public static final String ADULTERY = "adultery";
    public static final String SEPARATION2YRS = "separation-2-years";

    //Case state
    public static final String AWAITING_PAYMENT = "AwaitingPayment";
    public static final String PAYMENT_MADE_EVENT = "paymentMade";

    //Notification
    public static final String NOTIFICATION_EMAIL = "email_address";
    public static final String NOTIFICATION_TEMPLATE = "notification_template";
    public static final String NOTIFICATION_TEMPLATE_VARS = "notification_template_vars";
    public static final String NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY = "first name";
    public static final String NOTIFICATION_ADDRESSEE_LAST_NAME_KEY = "last name";
    public static final String NOTIFICATION_RELATIONSHIP_KEY = "relationship";
    public static final String NOTIFICATION_REFERENCE_KEY = "ref";

    public static final String IS_DRAFT_KEY =   "fetchedDraft";
    public static final String STATEMENT_OF_TRUTH = "D8StatementOfTruth";

    // Courts
    public static final String DIVORCE_CENTRE_SITEID_JSON_KEY = "D8SelectedDivorceCentreSiteId";
    public static final String DIVORCE_UNIT_JSON_KEY = "D8DivorceUnit";
    public static final String DIVORCE_UNIT_SERVICE_CENTRE = "serviceCentre";

    // Document Generator
    public static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_INVITATION = "aos";
    public static final String DOCUMENT_TYPE_PETITION = "petition";
    public static final String INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    public static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    public static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";

    // Fees
    public static final String CURRENCY = "GBP";
    public static final String FEE_PAY_BY_ACCOUNT = "feePayByAccount";
    public static final String PETITION_ISSUE_FEE_JSON_KEY = "petitionIssueFee";
    public static final String PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY = "solApplicationFeeOrderSummary";
    public static final String SERVICE = "DIVORCE";

    // Solicitors - Suggest refactoring into a common model library for all JSON keys
    public static final String SOLICITOR_HOW_TO_PAY_JSON_KEY = "SolPaymentHowToPay";
    public static final String SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY = "SolicitorFeeAccountNumber";
    public static final String SOLICITOR_FIRM_JSON_KEY = "PetitionerSolicitorFirm";
    public static final String SOLICITOR_REFERENCE_JSON_KEY = "D8SolicitorReference";
    public static final String SOLICITOR_STATEMENT_OF_TRUTH = "solSignStatementofTruth";

    //Payment
    public static final String PAYMENT = "payment";
    public static final String EXISTING_PAYMENTS = "existingPayments";
    public static final String PAYMENT_REFERENCE = "PaymentReference";
    public static final String PAYMENT_STATUS = "PaymentStatus";
    public static final String STATUS_FROM_PAYMENT = "status";
    public static final String PAYMENT_VALUE = "value";
    public static final String PAYMENT_SERVICE_AMOUNT_KEY = "amount";
    public static final String SUCCESS_PAYMENT_STATUS = "success";
    public static final String INITIATED_PAYMENT_STATUS = "Initiated";
    public static final String EXTERNAL_REFERENCE = "external_reference";

    public static final String PAYMENT_SERVICE_REFERENCE = "reference";
    public static final String PAYMENT_FEE_ID = "FEE0002";
    public static final String PAYMENT_CHANNEL = "online";
    public static final String PAYMENT_DATE_PATTERN = "ddMMyyyy";

    public static final String PAYMENT_CHANNEL_KEY = "PaymentChannel";
    public static final String PAYMENT_TRANSACTION_ID_KEY = "PaymentTransactionId";
    public static final String PAYMENT_REFERENCE_KEY = "PaymentReference";
    public static final String PAYMENT_DATE_KEY = "PaymentDate";
    public static final String PAYMENT_AMOUNT_KEY = "PaymentAmount";
    public static final String PAYMENT_STATUS_KEY = "PaymentStatus";
    public static final String PAYMENT_FEE_ID_KEY = "PaymentFeeId";
    public static final String PAYMENT_SITE_ID_KEY = "PaymentSiteId";

    // Validation
    public static final String ERROR_STATUS = "error";
    public static final String FORM_ID = "case-progression";
    public static final String UPDATE_REPONDENT_DATA_ERROR_KEY = "respondent.data.not.updated_Error";
    public static final String SOLICITOR_VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseData_Error";
    public static final String VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
    public static final String BULK_PRINT_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.BulkPrinter_Error";
    public static final String EMAIL_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification_Error";

}
