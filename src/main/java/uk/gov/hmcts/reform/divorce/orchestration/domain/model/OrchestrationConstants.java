package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Period;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrchestrationConstants {

    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String SPACE_SEPARATOR = " ";
    public static final String EMPTY_STRING = "";

    // Task context properties
    public static final String CCD_CASE_DATA = "ccdCaseData";
    public static final String DN_COURT_DETAILS = "dnCourtDetails";
    public static final String BULK_LINK_CASE_ID =  "bulkLinkCaseId";

    // Authentication
    public static final String ACCESS_CODE = "access_code";
    public static final String AUTH_TOKEN_JSON_KEY = "authToken";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String BASIC = "Basic ";
    public static final String PIN_PREFIX = "Pin ";
    public static final String CODE = "code";
    public static final String LOCATION_HEADER = "Location";
    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    //Issue Petition
    public static final String GENERATE_AOS_INVITATION = "generateAosInvitation";

    // Linking Case
    public static final String IS_RESPONDENT = "isRespondent";

    // Core Case Data
    public static final String STATE_CCD_FIELD = "state";
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
    public static final String D_8_CO_RESPONDENT_NAMED_OLD = "D8ReasonForDivorceAdulteryIsNamed";
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
    public static final String DECREE_ABSOLUTE_REQUESTED_EVENT_ID = "RequestDA";
    public static final String RESP_ADMIT_OR_CONSENT_TO_FACT = "RespAdmitOrConsentToFact";
    public static final String RESP_WILL_DEFEND_DIVORCE = "RespWillDefendDivorce";
    public static final String RESP_FIRST_NAME_CCD_FIELD = "D8RespondentFirstName";
    public static final String RESP_LAST_NAME_CCD_FIELD = "D8RespondentLastName";
    public static final String DN_COSTS_OPTIONS_CCD_FIELD = "DivorceCostsOptionDN";
    public static final String DN_COSTS_ENDCLAIM_VALUE = "endClaim";
    public static final String DIVORCE_COSTS_CLAIM_CCD_FIELD = "D8DivorceCostsClaim";
    public static final String DN_COSTS_CLAIM_CCD_FIELD = "DivorceCostsOptionDN";
    public static final String DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD = "CostsClaimGranted";
    public static final String DATETIME_OF_HEARING_CCD_FIELD = "DateAndTimeOfHearing";
    public static final String DECREE_NISI_GRANTED_CCD_FIELD = "DecreeNisiGranted";
    public static final String DATE_OF_HEARING_CCD_FIELD = "DateOfHearing";
    public static final String TIME_OF_HEARING_CCD_FIELD = "TimeOfHearing";
    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";
    public static final String ISSUE_DATE = "IssueDate";
    public static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CCD_DUE_DATE = "dueDate";
    public static final String D_8_PAYMENTS = "Payments";
    public static final String BULK_LISTING_CASE_ID_FIELD = "BulkListingCaseId";
    public static final String D_8_REASON_FOR_DIVORCE_DESERTION_DAIE = "D8ReasonForDivorceDesertionDate";
    public static final String D_8_REASON_FOR_DIVORCE_SEP_DATE = "D8ReasonForDivorceSeperationDate";
    public static final String D_8_MENTAL_SEP_DATE = "D8MentalSeparationDate";
    public static final String D_8_PHYSICAL_SEP_DAIE = "D8PhysicalSeparationDate";
    public static final String D_8_SEP_REF_DATE = "D8SeparationReferenceDate";
    public static final String SEP_YEARS = "SepYears";
    public static final String D_8_SEP_TIME_TOGETHER_PERMITTED = "D8SeparationTimeTogetherPermitted";
    public static final String D_8_DESERTION_TIME_TOGETHER_PERMITTED = "D8DesertionTimeTogetherPermitted";
    public static final String UPDATE_COURT_HEARING_DETAILS_EVENT = "updateBulkCaseHearingDetails";
    public static final String UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT = "dnPronouncedBulk";
    public static final String WHO_PAYS_COSTS_CCD_FIELD = "WhoPaysCosts";
    public static final String WHO_PAYS_CCD_CODE_FOR_RESPONDENT = "respondent";
    public static final String DECREE_NISI_GRANTED_DATE_CCD_FIELD = "DecreeNisiGrantedDate";
    public static final String DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD = "DAEligibleFromDate";
    public static final String DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD = "DecreeAbsoluteGrantedDate";
    public static final String PRONOUNCEMENT_JUDGE_CCD_FIELD = "PronouncementJudge";
    public static final String DATE_RESPONDENT_ELIGIBLE_FOR_DA_CCD_FIELD = "DateRespondentEligibleForDA";
    public static final String DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA_CCD_FIELD = "DateCaseNoLongerEligibleForDA";

    public static final String PET_SOL_EMAIL = "PetitionerSolicitorEmail";
    public static final String PET_SOL_NAME = "PetitionerSolicitorName";

    //CCD DN fields
    public static final String DN_OUTCOME_FLAG_CCD_FIELD = "DnOutcomeCase";

    // CCD Events
    public static final String DN_RECEIVED = "dnReceived";
    public static final String DN_RECEIVED_AOS_COMPLETE = "dnReceivedAosCompleted";
    public static final String AMEND_PETITION_EVENT = "amendPetition";
    public static final String AOS_START_FROM_OVERDUE = "startAosFromOverdue";
    public static final String AOS_START_FROM_REISSUE = "startAosFromReissue";
    public static final String AWAITING_DN_AOS_EVENT_ID = "aosSubmittedUndefended";
    public static final String AWAITING_ANSWER_AOS_EVENT_ID = "aosSubmittedDefended";
    public static final String COMPLETED_AOS_EVENT_ID = "aosReceivedNoAdConStarted";
    public static final String AOS_NOMINATE_SOLICITOR = "aosNominateSol";
    public static final String LINK_RESPONDENT_GENERIC_EVENT_ID = "linkRespondent";
    public static final String START_AOS_EVENT_ID = "startAos";
    public static final String PAYMENT_MADE_EVENT = "paymentMade";
    public static final String MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID = "MakeEligibleForDA_Petitioner";
    public static final String AWAITING_DECREE_ABSOLUTE = "AwaitingDecreeAbsolute";

    // CCD Case States
    public static final String AOS_AWAITING = "AosAwaiting";
    public static final String AOS_COMPLETED = "AosCompleted";
    public static final String AOS_OVERDUE = "AosOverdue";
    public static final String AOS_STARTED = "AosStarted";
    public static final String AOS_SUBMITTED_AWAITING_ANSWER = "AosSubmittedAwaitingAnswer";
    public static final String AWAITING_DECREE_NISI = "AwaitingDecreeNisi";
    public static final String AWAITING_LEGAL_ADVISOR_REFERRAL = "AwaitingLegalAdvisorReferral";
    public static final String AWAITING_PRONOUNCEMENT = "AwaitingPronouncement";
    public static final String AWAITING_CLARIFICATION = "AwaitingClarification";
    public static final String AWAITING_REISSUE = "AwaitingReissue";
    public static final String DEFENDED = "DefendedDivorce";
    public static final String DN_AWAITING = "DNAwaiting";
    public static final String DN_PRONOUNCED = "DNPronounced";
    public static final String AWAITING_DA = "AwaitingDecreeAbsolute";
    public static final String DA_REQUESTED = "DARequested";
    public static final String DIVROCE_GRANTED = "DivorceGranted";

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
    public static final String CO_RESPONDENT_ANSWER_RECEIVED = "ReceivedAnswerFromCoResp";
    public static final String CO_RESPONDENT_ANSWER_RECEIVED_DATE = "ReceivedAnswerFromCoRespDate";
    public static final String DN_DECISION_DATE_FIELD = "DNApprovalDate";

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

    // Hearing
    public static final String COSTS_CLAIM_GRANTED = "costs claim granted";
    public static final String COSTS_CLAIM_NOT_GRANTED = "costs claim not granted";
    public static final String DATE_OF_HEARING = "date of hearing";
    public static final String LIMIT_DATE_TO_CONTACT_COURT = "limit date to contact court";
    public static final Period PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT = Period.ofWeeks(2);


    // Reasons For Divorce
    public static final String ADULTERY = "adultery";
    public static final String SEPARATION_2YRS = "separation-2-years";
    public static final String UNREASONABLE_BEHAVIOUR = "unreasonable-behaviour";
    public static final String SEPARATION_5YRS = "separation-5-years";
    public static final String DESERTION = "desertion";

    // Case state
    public static final String AWAITING_PAYMENT = "AwaitingPayment";
    public static final String AWAITING_HWF_DECISION = "AwaitingHWFDecision";

    // Notification
    public static final String NOTIFICATION_EMAIL = "email address";
    public static final String NOTIFICATION_SEND_EMAIL = "send_email";
    public static final String NOTIFICATION_TEMPLATE = "notification_template";
    public static final String NOTIFICATION_TEMPLATE_VARS = "notification_template_vars";
    public static final String NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY = "first name";
    public static final String NOTIFICATION_ADDRESSEE_LAST_NAME_KEY = "last name";
    public static final String NOTIFICATION_EMAIL_ADDRESS_KEY = "email address";
    public static final String NOTIFICATION_HUSBAND_OR_WIFE = "husband or wife";
    public static final String NOTIFICATION_RELATIONSHIP_KEY = "relationship";
    public static final String NOTIFICATION_CCD_REFERENCE_KEY = "CCD reference";
    public static final String NOTIFICATION_REFERENCE_KEY = "ref";
    public static final String NOTIFICATION_CASE_NUMBER_KEY = "case number";
    public static final String NOTIFICATION_RDC_NAME_KEY = "RDC name";
    public static final String NOTIFICATION_COURT_ADDRESS_KEY = "court address";
    public static final String NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY = "form submission date limit";
    public static final String IS_DRAFT_KEY = "fetchedDraft";
    public static final String STATEMENT_OF_TRUTH = "D8StatementOfTruth";
    public static final String NOTIFICATION_OPTIONAL_TEXT_YES_VALUE = "yes";
    public static final String NOTIFICATION_OPTIONAL_TEXT_NO_VALUE = "no";
    public static final String NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE = "limit date to download certificate";
    public static final String NOTIFICATION_PET_NAME = "petitioner name";
    public static final String NOTIFICATION_RESP_NAME = "respondent name";
    public static final String NOTIFICATION_SOLICITOR_NAME = "solicitor name";

    // CCD Respondent Fields
    public static final String RECEIVED_AOS_FROM_RESP = "ReceivedAOSfromResp";
    public static final String RECEIVED_AOS_FROM_RESP_DATE = "ReceivedAOSfromRespDate";
    public static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";

    // Court Data Keys
    public static final String DIVORCE_CENTRE_SITEID_JSON_KEY = "D8SelectedDivorceCentreSiteId";
    public static final String DIVORCE_UNIT_JSON_KEY = "D8DivorceUnit";
    public static final String DIVORCE_UNIT_SERVICE_CENTRE = "serviceCentre";
    public static final String COURT_CONTACT_JSON_KEY = "CourtContactDetails";
    public static final String EMAIL_LABEL = "Email:";
    public static final String PHONE_LABEL = "Phone:";

    // Document Generator
    public static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_RESPONDENT_INVITATION = "aos";
    public static final String DOCUMENT_TYPE_RESPONDENT_ANSWERS = "respondentAnswers";
    public static final String DOCUMENT_TYPE_PETITION = "petition";
    public static final String RESPONDENT_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    public static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    public static final String DRAFT_MINI_PETITION_TEMPLATE_NAME = "divorcedraftminipetition";
    public static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";
    public static final String DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS = "coRespondentAnswers";
    public static final String DOCUMENT_TYPE_CO_RESPONDENT_INVITATION = "aoscr";
    public static final String CO_RESPONDENT_ANSWERS_TEMPLATE_NAME = "co-respondent-answers";
    public static final String CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT = "co-respondentaosinvitation%s";
    public static final String CO_RESPONDENT_INVITATION_TEMPLATE_NAME = "co-respondentinvitation";
    public static final String PETITION_ISSUE_FEE_FOR_LETTER = "petitionIssueFee";
    public static final String DOCUMENT_COLLECTION = "documentCollection";
    public static final String DOCUMENT_TYPE = "documentType";
    public static final String DOCUMENT_TEMPLATE_ID = "documentTemplateId";
    public static final String DOCUMENT_FILENAME = "documentFilename";
    public static final String DOCUMENT_FILENAME_FMT = "%s%s";
    public static final String DOCUMENTS_GENERATED = "DocumentsGenerated";
    public static final String COSTS_ORDER_DOCUMENT_TYPE = "costsOrder";
    public static final String COSTS_ORDER_TEMPLATE_ID = "FL-DIV-DEC-ENG-00060.docx";
    public static final String DECREE_NISI_DOCUMENT_TYPE = "dnGranted";
    public static final String DECREE_NISI_FILENAME = "decreeNisi";
    public static final String DECREE_NISI_TEMPLATE_ID = "FL-DIV-GNO-ENG-00021.docx";
    public static final String DECREE_ABSOLUTE_DOCUMENT_TYPE = "daGranted";
    public static final String DECREE_ABSOLUTE_FILENAME = "decreeAbsolute";
    public static final String DECREE_ABSOLUTE_TEMPLATE_ID = "FL-DIV-GOR-ENG-00062.docx";


    public static final String SOL_DOCUMENT_LINK_FIELD = "solDocumentLinkFieldName";
    public static final String MINI_PETITION_LINK = "minipetitionlink";
    public static final String RESP_ANSWERS_LINK = "respondentanswerslink";
    public static final String CO_RESP_ANSWERS_LINK = "corespondentanswerslink";
    public static final String SOLICITOR_LINKED_EMAIL = "RespSolLinkedEmail";


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
    public static final String D8_RESPONDENT_SOLICITOR_EMAIL = "D8RespondentSolicitorEmail";
    public static final String D8_RESPONDENT_SOLICITOR_NAME = "D8RespondentSolicitorName";
    public static final String D8DOCUMENTS_GENERATED = "D8DocumentsGenerated";

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

    public static final String SEARCH_PAGE_KEY = "SEARCH_PAGE";

    // DA related
    public static final String AWAITING_DA_PERIOD_KEY = "awaitingDAPeriod";
    public static final String CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT = "casesEligibleForDAProcessedCount";


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
