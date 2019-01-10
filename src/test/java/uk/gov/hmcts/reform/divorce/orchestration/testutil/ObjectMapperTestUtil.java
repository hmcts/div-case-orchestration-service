package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ObjectMapperTestUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String convertObjectToJsonString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getJsonFromResourceFile(String filePath, Class<T> clazz) throws IOException {
        try (InputStream resourceAsStream =
                     ObjectMapperTestUtil.class.getResourceAsStream(filePath)) {
            return objectMapper.readValue(resourceAsStream, clazz);
        }
    }

    public static <T> T getJsonFromString(String string, TypeReference<T> clazz) throws IOException {
        return objectMapper.readValue(string, clazz);
    }
}