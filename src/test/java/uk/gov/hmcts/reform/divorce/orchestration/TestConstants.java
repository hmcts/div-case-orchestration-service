package uk.gov.hmcts.reform.divorce.orchestration;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class TestConstants {
    public static final String TEST_BULK_CASE_ID = "test.bulk.case.id";
    public static final String TEST_CASE_ID = "test.case.id";
    public static final String TEST_CASE_FAMILY_MAN_ID = "test.family.man.id";
    public static final String TEST_STATE = "test.state";
    public static final String TEST_PIN = "abcd1234";
    public static final String TEST_EXPECTED_DUE_DATE = "2020-10-20";
    public static final String TEST_EXPECTED_DUE_DATE_FORMATTED = "20 October 2020";
    public static final String TEST_DECREE_ABSOLUTE_GRANTED_DATE = "2019-06-30T10:00:00.000";
    public static final String TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE = "2021-10-20";
    public static final String TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE = "30 June 2020";
    public static final String TEST_USER_ID = "test.user.id";
    public static final String TEST_COURT = "serviceCentre";
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
    public static final String TEST_SOLICITOR_ACCOUNT_NUMBER = "test.solicitor.account";
    public static final String TEST_SOLICITOR_FIRM_NAME = "test.solicitor.firm";
    public static final String TEST_SOLICITOR_REFERENCE = "test.solicitor.reference";
    public static final String TEST_SERVICE_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
            + ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    public static final String TEST_SOLICITOR_NAME = "Solcitor name";
    public static final String TEST_SERVICE_TOKEN = "testServiceToken";
    public static final String TEST_PETITIONER_FIRST_NAME = "First";
    public static final String TEST_PETITIONER_LAST_NAME = "Last";
    public static final String TEST_PETITIONER_EMAIL = "testPetitioner@email.com";
    public static final String TEST_PRONOUNCEMENT_JUDGE = "District Judge";
    public static final String TEST_USER_FIRST_NAME = "user first name";
    public static final String TEST_USER_LAST_NAME = "user last name";
    public static final String TEST_RELATIONSHIP = "wife";
    public static final String TEST_RESPONDENT_FIRST_NAME = "First";
    public static final String TEST_RESPONDENT_LAST_NAME = "Last";
    public static final String TEST_RESPONDENT_EMAIL = "testRespondent@email.com";
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
    public static final Map<String, Object>  DUMMY_CASE_DATA = ImmutableMap.of("someKey", "someValue");
}
