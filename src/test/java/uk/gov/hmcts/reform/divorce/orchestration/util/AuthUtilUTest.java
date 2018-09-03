package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthUtilUTest {

    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<AuthUtil> constructor = AuthUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void givenTokenIsNull_whenGetBearToken_thenReturnNull() {
        testGetBearToken(null, null);
    }

    @Test
    public void givenTokenIsBlank_whenGetBearToken_thenReturnBlank() {
        testGetBearToken(" ", " ");
    }

    @Test
    public void givenTokenDoesNotHaveBearer_whenGetBearToken_thenReturnWithBearer() {
        testGetBearToken("SomeToken", "Bearer SomeToken");
    }

    @Test
    public void givenTokenDoesHaveBearer_whenGetBearToken_thenReturnWithBearer() {
        testGetBearToken("Bearer SomeToken", "Bearer SomeToken");
    }

    private void testGetBearToken(String input, String expected) {
        assertEquals(AuthUtil.getBearToken(input), expected);
    }
}
