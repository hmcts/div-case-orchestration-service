package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class CourtsMatcher extends BaseMatcher<Object> {

    private static final String EXPECTED_COURTS_JSON = "{\n" +
        "    \"eastMidlands\":{\n" +
        "        \"divorceCentre\":\"East Midlands Regional Divorce Centre\",\n" +
        "        \"courtCity\":\"Nottingham\",\n" +
        "        \"poBox\":\"PO Box 10447\",\n" +
        "        \"postCode\":\"NG2 9QN\",\n" +
        "        \"openingHours\":\"Telephone Enquiries from: 8.30am to 5pm\",\n" +
        "        \"email\":\"eastmidlandsdivorce@hmcts.gsi.gov.uk\",\n" +
        "        \"phoneNumber\":\"0300 303 0642\",\n" +
        "        \"siteId\":\"AA01\"\n" +
        "    },\n" +
        "    \"westMidlands\":{\n" +
        "        \"divorceCentre\":\"West Midlands Regional Divorce Centre\",\n" +
        "        \"courtCity\":\"Stoke-on-Trent\",\n" +
        "        \"poBox\":\"PO Box 3650\",\n" +
        "        \"postCode\":\"ST4 9NH\",\n" +
        "        \"openingHours\":\"Telephone Enquiries from: 8.30am to 5pm\",\n" +
        "        \"email\":\"westmidlandsdivorce@hmcts.gsi.gov.uk\",\n" +
        "        \"phoneNumber\":\"0300 303 0642\",\n" +
        "        \"siteId\":\"AA02\"\n" +
        "    },\n" +
        "    \"southWest\":{\n" +
        "        \"divorceCentre\":\"South West Regional Divorce Centre\",\n" +
        "        \"courtCity\":\"Southampton\",\n" +
        "        \"poBox\":\"PO Box 1792\",\n" +
        "        \"postCode\":\"SO15 9GG\",\n" +
        "        \"openingHours\":\"Telephone Enquiries from: 8.30am to 5pm\",\n" +
        "        \"email\":\"sw-region-divorce@hmcts.gsi.gov.uk\",\n" +
        "        \"phoneNumber\":\"0300 303 0642\",\n" +
        "        \"siteId\":\"AA03\"\n" +
        "    },\n" +
        "    \"northWest\":{\n" +
        "        \"divorceCentre\":\"North West Regional Divorce Centre\",\n" +
        "        \"divorceCentreAddressName\":\"Liverpool Civil & Family Court\",\n" +
        "        \"courtCity\":\"Liverpool\",\n" +
        "        \"street\":\"35 Vernon Street\",\n" +
        "        \"postCode\":\"L2 2BX\",\n" +
        "        \"openingHours\":\"Telephone Enquiries from: 8.30am to 5pm\",\n" +
        "        \"email\":\"family@liverpool.countycourt.gsi.gov.uk\",\n" +
        "        \"phoneNumber\":\"0300 303 0642\",\n" +
        "        \"siteId\":\"AA04\"\n" +
        "    },\n" +
        "    \"serviceCentre\": {\n" +
        "        \"serviceCentreName\": \"Courts and Tribunals Service Centre\",\n" +
        "        \"divorceCentre\": \"HMCTS Digital Divorce\",\n" +
        "        \"courtCity\": \"Harlow\",\n" +
        "        \"poBox\": \"PO Box 12706\",\n" +
        "        \"postCode\": \"CM20 9QT\",\n" +
        "        \"openingHours\": \"Telephone Enquiries from: 8.30am to 5pm\",\n" +
        "        \"email\": \"divorcecase@justice.gov.uk\",\n" +
        "        \"phoneNumber\": \"0300 303 0642\",\n" +
        "        \"siteId\": \"AA07\"\n" +
        "    }\n" +
        "}";

    private String errorMessage;

    public static CourtsMatcher isExpectedCourtsList() {
        return new CourtsMatcher();
    }

    @Override
    public boolean matches(Object actual) {
        try {
            String actualJsonString = convertObjectToJsonString(actual);
            JSONAssert.assertEquals(EXPECTED_COURTS_JSON, actualJsonString, LENIENT);
            return true;
        } catch (AssertionError assertionError) {
            errorMessage = assertionError.getMessage();
            return false;
        }
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText(errorMessage);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("should match " + EXPECTED_COURTS_JSON);
    }

}