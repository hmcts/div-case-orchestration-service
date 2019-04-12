package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
    public static final String GENERATE_AOS_INVITATION = "generateAosInvitation";

    // Linking Case
    public static final String IS_RESPONDENT = "isRespondent";

    // Core Case Data
    public static final String CASE_DETAILS_JSON_KEY = "case_details";
    public static final String CASE_EVENT_DATA_JSON_KEY = "eventData";
    public static final String CASE_EVENT_ID_JSON_KEY = "eventId";
    public static final String D_8_DIVORCE_UNIT = "D8DivorceUnit";
    public static final String D_8_CASE_REFERENCE = "D8caseReference";
    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String D_8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String D_8_INFERRED_RESPONDENT_GENDER = "D8InferredRespondentGender";
    public static final String D_8_INFERRED_PETITIONER_GENDER = "D8InferredPetitionerGender";
    public static final String D_8_DIVORCED_WHO = "D8DivorceWho";
    public static final String D_8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String D_8_CO_RESPONDENT_NAMED = "D8ReasonForDivorceAdulteryWishToName";
    public static final String CCD_CASE_DATA = "ccdCaseData";
    public static final String CCD_CASE_DATA_FIELD = "case_data";
    public static final String NOT_RECEIVED_AOS_EVENT_ID = "aosNotReceived";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID = "co-RespAOSReceivedAwaiting";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID = "co-RespAOSReceivedStarted";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID = "co-RespAOSReceivedAwaitingAnswer";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID = "co-RespAOSReceivedOverdue";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID = "co-RespAOSReceivedDefended";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID = "co-RespAOSCompleted";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID = "co-RespAwaitingDN";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID = "co-RespAwaitingLAReferral";
    public static final String RESP_ADMIT_OR_CONSENT_TO_FACT = "RespAdmitOrConsentToFact";
    public static final String RESP_WILL_DEFEND_DIVORCE = "RespWillDefendDivorce";
    public static final String RESP_FIRST_NAME_CCD_FIELD = "D8RespondentFirstName";
    public static final String RESP_LAST_NAME_CCD_FIELD = "D8RespondentLastName";
    public static final String DIVORCE_COSTS_CLAIM_CCD_FIELD = "D8DivorceCostsClaim";
    public static final String DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD = "CostsClaimGranted";
    public static final String DATE_OF_HEARING_CCD_FIELD = "DateOfHearing";
    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";
    public static final String ISSUE_DATE = "IssueDate";
    public static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CCD_DUE_DATE = "dueDate";
    public static final String D_8_PAYMENTS = "Payments";

    // CCD Events
    public static final String DN_RECEIVED = "dnReceived";
    public static final String DN_RECEIVED_AOS_COMPLETE = "dnReceivedAosCompleted";
    public static final String AMEND_PETITION_EVENT = "amendPetition";
    public static final String AOS_START_FROM_OVERDUE = "startAosFromOverdue";
    public static final String AOS_START_FROM_REISSUE = "startAosFromReissue";
    public static final String AWAITING_DN_AOS_EVENT_ID = "aosSubmittedUndefended";
    public static final String AWAITING_ANSWER_AOS_EVENT_ID = "aosSubmittedDefended";
    public static final String COMPLETED_AOS_EVENT_ID = "aosReceivedNoAdConStarted";
    public static final String LINK_RESPONDENT_GENERIC_EVENT_ID = "linkRespondent";
    public static final String START_AOS_EVENT_ID = "startAos";

    // CCD Case States
    public static final String AOS_AWAITING = "AosAwaiting";
    public static final String AOS_STARTED = "AosStarted";
    public static final String AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    public static final String AOS_OVERDUE = "AosOverdue";
    public static final String AOS_COMPLETED = "AosCompleted";
    public static final String DEFENDED = "DefendedDivorce";
    public static final String AWAITING_DECREE_NISI = "AwaitingDecreeNisi";
    public static final String DN_AWAITING = "DNAwaiting";
    public static final String AWAITING_REISSUE = "AwaitingReissue";
    public static final String AWAITING_LEGAL_ADVISOR_REFERRAL = "AwaitingLegalAdvisorReferral";


    // CCD Respondent Fields
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";
    public static final String RECEIVED_AOS_FROM_RESP = "ReceivedAOSfromResp";
    public static final String RECEIVED_AOS_FROM_RESP_DATE = "ReceivedAOSfromRespDate";
    public static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";

    // CCD Co-Respondent Fields
    public static final String CO_RESP_LINKED_TO_CASE = "CoRespLinkedToCase";
    public static final String CO_RESP_LINKED_TO_CASE_DATE = "CoRespLinkedToCaseDate";
    public static final String CO_RESPONDENT_LETTER_HOLDER_ID = "CoRespLetterHolderId";
    public static final String CO_RESP_EMAIL_ADDRESS = "CoRespEmailAddress";
    public static final String CO_RESPONDENT_DEFENDS_DIVORCE = "CoRespDefendsDivorce";
    public static final String RECEIVED_AOS_FROM_CO_RESP = "ReceivedAosFromCoResp";
    public static final String RECEIVED_AOS_FROM_CO_RESP_DATE = "ReceivedAosFromCoRespDate";
    public static final String D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME = "D8ReasonForDivorceAdultery3rdPartyFName";
    public static final String D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME = "D8ReasonForDivorceAdultery3rdPartyLName";
    public static final String CO_RESPONDENT_DUE_DATE = "DueDateCoResp";

    // Divorce Session
    public static final String DIVORCE_SESSION_EXISTING_PAYMENTS = "existingPayments";
    public static final String DIVORCE_SESSION_PETITIONER_EMAIL = "petitionerEmail";
    public static final String CASE_ID_JSON_KEY = "caseId";
    public static final String PREVIOUS_CASE_ID_JSON_KEY = "previousCaseId";
    public static final String NEW_AMENDED_PETITION_DRAFT_KEY = "newAmendedPetitionDraft";
    public static final String CASE_STATE_JSON_KEY = "state";
    public static final String CREATED_DATE_JSON_KEY = "createdDate";
    public static final String ID = "id";
    public static final String PIN = "pin";
    public static final String RESPONDENT_PIN = "pin";
    public static final String CO_RESPONDENT_PIN = "coRespondentPin";
    public static final String SUCCESS_STATUS = "success";

    // Reasons For Divorce
    public static final String ADULTERY = "adultery";
    public static final String SEPARATION_2YRS = "separation-2-years";
    public static final String UNREASONABLE_BEHAVIOUR = "unreasonable-behaviour";

    // Case state
    public static final String AWAITING_PAYMENT = "AwaitingPayment";
    public static final String AWAITING_HWF_DECISION = "AwaitingHWFDecision";
    public static final String PAYMENT_MADE_EVENT = "paymentMade";

    // Notification
    public static final String NOTIFICATION_EMAIL = "email_address";
    public static final String NOTIFICATION_SEND_EMAIL = "send_email";
    public static final String NOTIFICATION_TEMPLATE = "notification_template";
    public static final String NOTIFICATION_TEMPLATE_VARS = "notification_template_vars";
    public static final String NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY = "first name";
    public static final String NOTIFICATION_ADDRESSEE_LAST_NAME_KEY = "last name";
    public static final String NOTIFICATION_RELATIONSHIP_KEY = "relationship";
    public static final String NOTIFICATION_REFERENCE_KEY = "ref";
    public static final String NOTIFICATION_CASE_NUMBER_KEY = "case number";
    public static final String NOTIFICATION_RDC_NAME_KEY = "RDC name";
    public static final String NOTIFICATION_COURT_ADDRESS_KEY = "court address";
    public static final String NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY = "form submission date limit";
    public static final String IS_DRAFT_KEY =   "fetchedDraft";
    public static final String STATEMENT_OF_TRUTH = "D8StatementOfTruth";

    // Courts
    public static final String DIVORCE_CENTRE_SITEID_JSON_KEY = "D8SelectedDivorceCentreSiteId";
    public static final String DIVORCE_UNIT_JSON_KEY = "D8DivorceUnit";
    public static final String DIVORCE_UNIT_SERVICE_CENTRE = "serviceCentre";

    // Document Generator
    public static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_RESPONDENT_INVITATION = "aos";
    public static final String DOCUMENT_TYPE_PETITION = "petition";
    public static final String RESPONDENT_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    public static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    public static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";
    public static final String DOCUMENT_TYPE_CO_RESPONDENT_INVITATION = "aoscr";
    public static final String CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT = "co-respondentaosinvitation%s";
    public static final String CO_RESPONDENT_INVITATION_TEMPLATE_NAME = "co-respondentinvitation";
    public static final String PETITION_ISSUE_FEE_FOR_LETTER = "petitionIssueFee";
    public static final String DOCUMENT_COLLECTION = "documentCollection";


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

    // Payment
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
    public static final String UPDATE_RESPONDENT_DATA_ERROR_KEY = "respondent.data.not.updated_Error";
    public static final String SOLICITOR_VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseData_Error";
    public static final String VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
    public static final String BULK_PRINT_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.BulkPrinter_Error";
    public static final String EMAIL_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification_Error";

}
