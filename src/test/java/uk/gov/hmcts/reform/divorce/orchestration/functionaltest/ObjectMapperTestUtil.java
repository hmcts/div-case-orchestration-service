package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectMapperTestUtil {

    static <T> T convertStringToObject(String data, Class<T> type) {
        try {
            return new ObjectMapper().readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String convertObjectToJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
