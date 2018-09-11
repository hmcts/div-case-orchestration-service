package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTestUtil {

    public static String convertObjectToJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
