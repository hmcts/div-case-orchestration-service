package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class CourtsMatcher extends BaseMatcher<Object> {

    private static final String EXPECTED_COURTS_JSON;

    private String errorMessage;

    public static CourtsMatcher isExpectedCourtsList() {
        return new CourtsMatcher();
    }

    static {
        try {
            EXPECTED_COURTS_JSON = Files.lines(Paths.get(
                CourtsMatcher.class.getResource("/jsonExamples/expectedCourts.json").toURI()
            )).collect(Collectors.joining());
        } catch (IOException | URISyntaxException exception) {
            throw new RuntimeException("Could not load expected courts json file.", exception);
        }
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