package uk.gov.hmcts.reform.divorce.orchestration;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.util.Map;

import static java.util.Collections.singletonMap;

public class TestConstants {
    public static final String TEST_BULK_CASE_ID = "test.bulk.case.id";
    public static final String TEST_CASE_ID = "test.case.id";
    public static final String TEST_CASE_FAMILY_MAN_ID = "test.family.man.id";
    public static final String TEST_STATE = "test.state";
    public static final String TEST_PIN = "abcd1234";
    public static final String TEST_EXPECTED_DUE_DATE = "2020-10-20";
    public static final String TEST_EXPECTED_DUE_DATE_FORMATTED = "20 October 2020";
    public static final String TEST_FORM_WESLH_SUBMISSION_DUE_DATE = "20 Hydref 2020";
    public static final String TEST_WESLH_DATE = "20 Hydref 2020";
    public static final String TEST_DECREE_ABSOLUTE_GRANTED_DATE = "2019-06-30T10:00:00.000";
    public static final String TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE = "2021-10-20";
    public static final String TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE = "30 June 2020";
    public static final String TEST_USER_ID = "test.user.id";
    public static final String TEST_COURT = "serviceCentre";
    public static final String TEST_COURT_ID = "birmingham";
    public static final String TEST_ERROR = "test.error";
    public static final String TEST_ERROR_CONTENT = "test.error.content";
    public static final String TEST_PIN_CODE = "test.pin.code";
    public static final String TEST_LETTER_HOLDER_ID_CODE = "test.letter.holder.id";
    public static final String TEST_AOS_INVITATION_FILE_NAME = "aosinvitation" + TEST_CASE_ID;
    public static final String TEST_CO_RESPONDENT_AOS_FILE_NAME = "co-respondentaosinvitation" + TEST_CASE_ID;
    public static final String TEST_USER_EMAIL = "test@email.com";
    public static final Double TEST_FEE_AMOUNT = 550d;
    public static final String TEST_FEE_CODE = "FEE000";
    public static final Integer TEST_FEE_VERSION = 3;
    public static final String TEST_FEE_DESCRIPTION = "Test Fee";
    public static final String TEST_HWF_REF = "HWF-123-456";
    public static final String TEST_PBA_REF = "PBA089786";
    public static final String TEST_SOLICITOR_ACCOUNT_NUMBER = "test.solicitor.account";
    public static final String TEST_SOLICITOR_FIRM_NAME = "test.solicitor.firm";
    public static final String TEST_SOLICITOR_COMPANY = "Awesome Solicitors LLP";
    public static final String TEST_SOLICITOR_REFERENCE = "test.solicitor.reference";
    public static final String TEST_SOLICITOR_PHONE = "test.solicitor.phone";
    public static final String TEST_SOLICITOR_ADDRESS = "123 Solicitor Str\nSolicitor\nCounty\nRE3 P0T";
    public static final String TEST_SERVICE_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
        + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlBldGVyIEdyaWZmaW4iLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTU2NjU5MTQyMn0."
        + "49IlIqUnUYNx0_8lmAXcZ9epzpNHJS9CpRvHFeESDvg";
    public static final String TEST_SOLICITOR_NAME = "Solicitor name";
    public static final String TEST_SOLICITOR_EMAIL = "testSolicitor@email.com";
    public static final String TEST_RESP_SOLICITOR_NAME = "Respondent Solicitor name";
    public static final String TEST_RESP_SOLICITOR_EMAIL = "testRespondentSolicitor@email.com";
    public static final String TEST_SERVICE_TOKEN = "testServiceToken";
    public static final String TEST_FIRST_NAME = "First";
    public static final String TEST_LAST_NAME = "Last";
    public static final String TEST_PETITIONER_EMAIL = "testPetitioner@email.com";
    public static final String TEST_PRONOUNCEMENT_JUDGE = "District Judge";
    public static final String TEST_USER_FIRST_NAME = "user first name";
    public static final String TEST_USER_LAST_NAME = "user last name";
    public static final String TEST_RELATIONSHIP = "wife";
    public static final String TEST_RELATIONSHIP_HUSBAND = "husband";
    public static final String TEST_WELSH_FEMALE_GENDER_IN_RELATION = "gwraig";
    public static final String TEST_WELSH_MALE_GENDER_IN_RELATION = "gŵr";
    public static final String TEST_INFERRED_GENDER = "female";
    public static final String TEST_INFERRED_MALE_GENDER = "male";
    public static final String TEST_RESPONDENT_EMAIL = "testRespondent@email.com";
    public static final String TEST_RESPONDENT_SOLICITOR_EMAIL = "testRespondentSolicitor@example.com";
    public static final String TEST_RESPONDENT_SOLICITOR_NAME = "Saul Goodman";
    public static final String TEST_REASON_ADULTERY = "adultery";
    public static final String TEST_REASON_2_YEAR_SEP = "separation-2-years";
    public static final String TEST_REASON_UNREASONABLE_BEHAVIOUR = "unreasonable-behaviour";
    public static final String AOS_AWAITING_STATE = "AosAwaiting";
    public static final String AWAITING_CONSIDERATION_GENERAL_APPLICATION = "AwaitingConsiderationGeneralApplication";
    public static final String D8_CASE_ID = "LV17D80101";
    public static final String UNFORMATTED_CASE_ID = "0123456789";
    public static final String TEST_TOKEN = "test.token";
    public static final String TEST_EVENT_ID = "test.event.id";
    public static final String AUTH_TOKEN = "test.auth.token";
    public static final String BEARER_AUTH_TOKEN = "Bearer test.auth.token";
    public static final String CASEWORKER_AUTH_TOKEN = "caseworker.auth.token";
    public static final String AUTH_TOKEN_1 = "test.auth.token1";
    public static final String TEST_EMAIL = "test.email";
    public static final String BEARER_AUTH_TOKEN_1 = "Bearer test.auth.token1";
    public static final String TEST_CODE = "test.code";
    public static final String TEST_JUDGE_NAME = "Judge name";
    public static final Map<String, Object> DUMMY_CASE_DATA = ImmutableMap.of("someKey", "someValue");
    public static final String TEMPLATE_ID = "testTemplateId";
    public static final String DOCUMENT_TYPE = "testDocumentType";
    public static final String FILE_NAME = "testFileName";
    public static final String SOL_SERVICE_METHOD_CCD_FIELD = "SolServiceMethod";
    public static final String PERSONAL_SERVICE_VALUE = "personalService";
    public static final String AUTH_CLIENT_ID = "authClientId";
    public static final String AUTH_CLIENT_SECRET = "authClientSecret";
    public static final String AUTH_REDIRECT_URL = "authRedirectUrl";
    public static final String TEST_D8_DERIVED_3RD_PARTY_ADDRESS = "456 CoRespondent Str\nCoRespondent\nCounty\nRE5 P0N";
    public static final String TEST_D8_CASE_REFERENCE = "LV17D80102";
    public static final String TEST_CO_RESPONDENT_SOLICITOR_NAME = "CoResp Solicitor";
    public static final String TEST_CO_RESPONDENT_SOLICITOR_ADDRESS = "789 CoRespondent Solicitor Str\nCoRespondent\nCounty\nRE5 P0N";
    public static final String TEST_CO_RESPONDENT_SOLICITOR_EMAIL = "corespondentsolicitor@email.com";
    public static final String TEST_CO_RESPONDENT_EMAIL = "corespondent@email.com";
    public static final String TEST_OTHER_PARTY_EMAIL = "OtherPartyEmail@address.com";
    public static final String TEST_OTHER_PARTY_NAME = "Otto Martie";

    public static final String TEST_PETITIONER_FIRST_NAME = "Clark";
    public static final String TEST_PETITIONER_LAST_NAME = "Kent";
    public static final String TEST_PETITIONER_FULL_NAME = TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME;
    public static final String TEST_RESPONDENT_FIRST_NAME = "Diana";
    public static final String TEST_RESPONDENT_LAST_NAME = "Prince";
    public static final String TEST_RESPONDENT_FULL_NAME = TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME;
    public static final String TEST_CO_RESPONDENT_FIRST_NAME = "Bruce";
    public static final String TEST_CO_RESPONDENT_LAST_NAME = "Wayne";
    public static final String TEST_CO_RESPONDENT_FULL_NAME = TEST_CO_RESPONDENT_FIRST_NAME + " " + TEST_CO_RESPONDENT_LAST_NAME;

    public static final String TEST_RECEIVED_DATE = "2020-05-05";
    public static final String TEST_DECISION_DATE = "2030-10-10";
    public static final String TEST_ADDED_DATE = "2000-01-01";
    public static final String TEST_ADDED_DATE_FORMATTED = DateUtils.formatDateWithCustomerFacingFormat(TEST_ADDED_DATE);
    public static final String TEST_SERVICE_APPLICATION_PAYMENT = "feeAccount";

    public static final String TEST_MY_REASON = "this is my reason";

    public static final String TEST_GENERAL_EMAIL_DETAILS = "Leverage agile frameworks to provide a robust synopsis for high level overviews.";

    public static final Map<String, Object> TEST_INCOMING_PAYLOAD = singletonMap("incomingKey", "incomingValue");
    public static final CaseDetails TEST_INCOMING_CASE_DETAILS = CaseDetails.builder().caseData(TEST_INCOMING_PAYLOAD).build();
    public static final Map<String, Object> TEST_PAYLOAD_TO_RETURN = singletonMap("returnedKey", "returnedValue");

}