package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ObjectMapperTestUtil {

    private static ObjectMapper objectMapper;

    public static String convertObjectToJsonString(final Object object) {
        try {
            return getCommonObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getJsonFromResourceFile(String filePath, Class<T> clazz) throws IOException {
        try (InputStream resourceAsStream =
                     ObjectMapperTestUtil.class.getResourceAsStream(filePath)) {
            return getCommonObjectMapper().readValue(resourceAsStream, clazz);
        }
    }

    private static ObjectMapper getCommonObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        return objectMapper;
    }

}