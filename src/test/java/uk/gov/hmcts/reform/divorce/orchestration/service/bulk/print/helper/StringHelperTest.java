package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.buildFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.formatFilename;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.notNull;

public class StringHelperTest {

    private static final String F_NAME = "f";
    private static final String L_NAME = "l";

    @Test
    public void formatFilenameReturnsValidString() {
        assertThat(formatFilename("1", "my-file"), is("my-file1"));
        assertThat(formatFilename("", "my-file"), is("my-file"));
        assertThat(formatFilename("1", ""), is("1"));
        assertThat(formatFilename("", ""), is(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatFilenameThrowsExceptionWhenCaseIdIsNull() {
        formatFilename(null, "my-file");
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatFilenameThrowsExceptionWhenFilenameIsNull() {
        formatFilename("1", null);
    }

    @Test
    public void notNullReturnsString() {
        assertThat(notNull("value"), is("value"));
        assertThat(notNull(""), is(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNullThrowsException() {
        notNull(null);
    }

    @Test
    public void buildFullNameShouldBuildFullName() {
        assertThat(buildFullName(fullName("Pit", "Smith"), F_NAME, L_NAME), is("Pit Smith"));
        assertThat(buildFullName(fullName("", "Smith"), F_NAME, L_NAME), is("Smith"));
        assertThat(buildFullName(fullName("Pit Adam", "Smith"), F_NAME, L_NAME), is("Pit Adam Smith"));
        assertThat(buildFullName(fullName("Pit", "Smith-Johnson"), F_NAME, L_NAME), is("Pit Smith-Johnson"));
        assertThat(buildFullName(fullName("Pit JK", "Smith"), F_NAME, L_NAME), is("Pit JK Smith"));
        assertThat(buildFullName(fullName("Pit", ""), F_NAME, L_NAME), is("Pit"));
        assertThat(buildFullName(fullName("", ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName(null, ""), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("", null), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("     ", "    "), F_NAME, L_NAME), is(""));
        assertThat(buildFullName(fullName("    Pit   ", "     Smith    "), F_NAME, L_NAME), is("Pit Smith"));
    }

    private static Map<String, Object> fullName(String firstName, String lastName) {
        Map<String, Object> fullNameMap = new HashMap<>();
        fullNameMap.put(F_NAME, firstName);
        fullNameMap.put(L_NAME, lastName);

        return fullNameMap;
    }
}
