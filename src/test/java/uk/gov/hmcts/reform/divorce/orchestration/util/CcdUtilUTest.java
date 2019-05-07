package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;

public class CcdUtilUTest {
    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<CcdUtil> constructor = CcdUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void whenGiveDateAsYyyyMmDd_thenReturnFormattedDate() throws TaskException {

        Map<String, Object> testCaseData = new HashMap<>();
        testCaseData.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);

        assertEquals(TEST_EXPECTED_DUE_DATE_FORMATTED, CcdUtil.getFormattedDueDate(testCaseData, CO_RESPONDENT_DUE_DATE));
    }

}