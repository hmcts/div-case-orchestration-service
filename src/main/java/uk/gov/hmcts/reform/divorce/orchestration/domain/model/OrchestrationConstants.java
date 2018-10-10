package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

public class OrchestrationConstants {

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
    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D_8_DIVORCE_UNIT = "D8DivorceUnit";
    public static final String D_8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String CCD_CASE_DATA = "ccdCaseData";
    public static final String CCD_CASE_DATA_FIELD = "case_data";
    public static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    public static final String START_AOS_EVENT_ID = "startAos";
    public static final String COMPLETE_AOS_EVENT_ID = "aosCompletedFromAosStarted";
    public static final String AWAITING_DN_AOS_EVENT_ID = "issueDecreeNisi";
    public static final String AWAITING_ANSWER_AOS_EVENT_ID = "aosCompletedAwaitingAnswerFromAosStarted";
    public static final String D_8_INFERRED_RESPONDENT_GENDER = "D8InferredRespondentGender";
    public static final String RESP_ADMIT_OR_CONSENT_CCD_FIELD = "RespAdmitOrConsentToFact";
    public static final String RESP_DEFENDS_DIVORCE_CCD_FIELD = "RespDefendsDivorce";
    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";
    public static final String DN_AWAITING_LEGAL_ADVISOR_REFERRAL = "AwaitingLegalAdvisorReferral";

    public static final String ID = "id";
    public static final String PIN = "pin";
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";
    public static final String SUCCESS_STATUS = "success";

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
    public static final String DIVORCE_UNIT_JSON_KEY = "D8DivorceUnit";
    public static final String DIVORCE_CENTRE_SITEID_JSON_KEY = "D8SelectedDivorceCentreSiteId";

    // Document Generator
    public static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_INVITATION = "aosinvitation";
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

    // Validation
    public static final String ERROR_STATUS = "error";
    public static final String FORM_ID = "case-progression";
    public static final String SOLICITOR_VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseData_Error";
    public static final String VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
    public static final String EMAIL_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification_Error";
}
