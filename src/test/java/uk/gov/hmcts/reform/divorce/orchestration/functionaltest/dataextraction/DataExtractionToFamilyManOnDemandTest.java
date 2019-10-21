package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.dataextraction;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test is very similar to its parent class, but it uses the REST endpoint (like we'd use if we wanted to run the data extraction on demand).
 */
public class DataExtractionToFamilyManOnDemandTest extends DataExtractionToFamilyManTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void checkEmailIsSentOnDemandForGivenDateAndStatus() throws Exception {
        mockMvc.perform(post("/cases/data-extraction/family-man/status/{status}/lastModifiedDate/{lastModifiedDate}", "DA", "2019-08-12"))
            .andExpect(status().isOk());

        verify(mockEmailClient).sendEmailWithAttachment(eq("da-extraction@divorce.gov.uk"), eq("DA_12082019000000.csv"), notNull());
    }

    @Test
    public void shouldReplyWithBadRequest_WhenPassingInvalidStatus() throws Exception {
        mockMvc.perform(post("/cases/data-extraction/family-man/status/{status}/lastModifiedDate/{lastModifiedDate}", "INVALID_STATUS", "2019-08-12"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReplyWithBadRequest_WhenDateIsMalformed() throws Exception {
        mockMvc.perform(post("/cases/data-extraction/family-man/status/{status}/lastModifiedDate/{lastModifiedDate}", "DA", "abc"))
            .andExpect(status().isBadRequest());
    }

}