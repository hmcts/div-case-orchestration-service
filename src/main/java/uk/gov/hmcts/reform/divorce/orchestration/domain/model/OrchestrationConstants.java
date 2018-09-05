package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

public class OrchestrationConstants {

    // Authentication
    public static final String ACCESS_CODE = "access_code";
    public static final String AUTH_TOKEN_JSON_KEY = "authToken";

    // Core Case Data
    public static final String CASE_DATA_JSON_KEY = "case_data";
    public static final String CASE_DETAILS_JSON_KEY = "case_details";
    public static final String CASE_EVENT_ID_JSON_KEY = "eventId";
    public static final String CASE_ID_JSON_KEY = "caseId";
    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String ID = "id";
    public static final String PIN = "pin";
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";
    public static final String SUCCESS_STATUS = "success";
    public static final String ERROR_STATUS = "error";

    // Document Generator
    public static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_INVITATION = "aosinvitation";
    public static final String DOCUMENT_TYPE_PETITION = "petition";
    public static final String INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    public static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    public static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";

    // Validation
    public static final String FORM_ID = "case-progression";
    public static final String VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
    public static final String EMAIL_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.EmailNotification_Error";
    public static final String SAVE_DRAFT_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.SaveToDraftStore_Error";
    public static final String DELETE_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft_Error";

}
