package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationEmailTest;

import java.io.IOException;
import java.io.InputStream;

public class ObjectMapperTestUtil {

    public static String convertObjectToJsonString(final Object object) {
        try {
            return getCommonObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getJsonFromResourceFile(String filePath, Class<T> clazz) throws IOException {
        try (InputStream resourceAsStream =
                     SendRespondentSubmissionNotificationEmailTest.class.getResourceAsStream(filePath)) {
            return getCommonObjectMapper().readValue(resourceAsStream, clazz);
        }
    }

    private static ObjectMapper getCommonObjectMapper() {
        return new ObjectMapper();
    }

}