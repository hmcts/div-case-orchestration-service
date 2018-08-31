package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

public class OrchestrationConstants {
    public static final String ID = "id";
    public static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    public static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";
    public static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    public static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    public static final String RESPONDENT_LETTER_HOLDER_ID = "AosLetterHolderId";
    public static final String PIN = "pin";
    public static final String CASE_DETAILS_JSON_KEY = "caseDetails";
    public static final String DOCUMENT_TYPE_PETITION = "petition";
    public static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    public static final String DOCUMENT_TYPE_INVITATION = "aosinvitation";
    public static final String INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    public static final String ACCESS_CODE = "access_code";
    public static final String FORM_ID = "case-progression";
    public static final String VALIDATION_ERROR_KEY
            = "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
}
