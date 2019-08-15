package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_MENTAL_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PHYSICAL_SEP_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_REF_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_TIME_TOGETHER_PERMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_5YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEP_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class CalculateSeparationFieldsITest extends MockedFunctionalTest {
    private static final String API_URL = "/calculate-separation-fields";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenCalSepFields_thenReturnPayloadWithCalcFields() throws Exception {

        String pastDate5Yrs8Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(8));
        String pastDate5Yrs9Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(9));
        String pastDate5Yrs6Mnths = DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().minusYears(5).minusMonths(6));

        Map<String, Object> testCaseData = ImmutableMap.of(D_8_REASON_FOR_DIVORCE, SEPARATION_5YRS,
            D_8_MENTAL_SEP_DATE, pastDate5Yrs8Mnths,
            D_8_PHYSICAL_SEP_DAIE, pastDate5Yrs9Mnths);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.putAll(testCaseData);
        expectedData.put(D_8_SEP_TIME_TOGETHER_PERMITTED, "6 months");
        expectedData.put(D_8_REASON_FOR_DIVORCE_SEP_DATE, pastDate5Yrs8Mnths);
        expectedData.put(D_8_SEP_REF_DATE, pastDate5Yrs6Mnths);
        expectedData.put(SEP_YEARS, "5");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(testCaseData)
                .build())
            .build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }
}
