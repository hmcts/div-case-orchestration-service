package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PHONE_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SPACE_SEPARATOR;

@RunWith(SpringRunner.class)
public class DnCourtTest {

    private static final String TEST_COURT_NAME    = "testCourt";
    private static final String TEST_COURT_ADDRESS = "testAddress\nLineTwo\nLineThree";
    private static final String TEST_COURT_EMAIL   = "testEmail@test.test";
    private static final String TEST_COURT_PHONE   = "01234567890";

    @Test
    public void givenDnCourtModel_whenGetFormattedContactDetails_thenReturnFormattedString() {
        DnCourt dnCourt = new DnCourt();
        dnCourt.setName(TEST_COURT_NAME);
        dnCourt.setAddress(TEST_COURT_ADDRESS);
        dnCourt.setEmail(TEST_COURT_EMAIL);
        dnCourt.setPhone(TEST_COURT_PHONE);

        String expectedFormat = TEST_COURT_ADDRESS + LINE_SEPARATOR
                + LINE_SEPARATOR + EMAIL_LABEL + SPACE_SEPARATOR + TEST_COURT_EMAIL
                + LINE_SEPARATOR + PHONE_LABEL + SPACE_SEPARATOR + TEST_COURT_PHONE;
        assertEquals(expectedFormat, dnCourt.getFormattedContactDetails());
    }
}
