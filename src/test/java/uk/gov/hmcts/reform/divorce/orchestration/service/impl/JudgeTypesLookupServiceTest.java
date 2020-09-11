package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JudgeTypesLookupServiceTest {

    public static final String EXISTING_JUDGE_CODE = "hishonourjudge";
    public static final String VALID_JUDGE_TYPE = "His Honour Judge";
    @Autowired
    private JudgeTypesLookupService judgeTypesLookupService;

    @Test
    public void getJudgeTypeByCodeReturnsJudgeTypeByCode() throws JudgeTypeNotFoundException {
        assertThat(judgeTypesLookupService.getJudgeTypeByCode(EXISTING_JUDGE_CODE), is(VALID_JUDGE_TYPE));
    }

    @Test(expected = JudgeTypeNotFoundException.class)
    public void getJudgeTypeByCodeThrowsJudgeTypeNotFoundException() throws JudgeTypeNotFoundException {
        judgeTypesLookupService.getJudgeTypeByCode("This type of judge does not exist");
    }
}
