package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public class PropertiesUtilTest {

    @Test
    public void getExistingValue() {
        String propertyValue = PropertiesUtil.getMandatoryPropertyValueAsStringOrThrowGivenException(Map.of("myName", "myValue"), "myName", null);
        assertThat(propertyValue, is("myValue"));
    }

    @Test
    public void throwCustomExceptionForNonExistentValue() {
        assertThrows(NullPointerException.class, () -> PropertiesUtil.getMandatoryPropertyValueAsStringOrThrowGivenException(Map.of(), "myName", () -> new NullPointerException()));
    }

    @Test
    public void throwDefaultExceptionForNonExistentValue() {
        assertThrows(NoSuchElementException.class, () -> PropertiesUtil.getMandatoryPropertyValueAsStringOrThrowGivenException(Map.of(), "myName", null));
    }

}