package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aos;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.AosOverdueCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OVERDUE_COVER_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService.wrapDocmosisTemplateVarsForDgs;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class AosNotReceivedPreparationForSubmissionTest extends MockedFunctionalTest {

    private static final String URI = "/prepare-aos-not-received-for-submission";

    private static final String TEST_PAYLOAD = "/jsonExamples/payloads/genericPetitionerData.json";

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldPrepareForAosNotReceivedSubmission() throws Exception {
        DocmosisTemplateVars expectedDocmosisTemplate = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder()
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .caseReference("0123-4567-8901-2345")
            .addressee(
                Addressee.builder()
                    .name("Ted Jones")
                    .formattedAddress("10 Jones's Close\nLondon")
                    .build()
            )
            .build();
        stubDocumentGeneratorService(AOS_OVERDUE_COVER_LETTER.getTemplateByLanguage(ENGLISH),
            wrapDocmosisTemplateVarsForDgs(expectedDocmosisTemplate),
            AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE);

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(TEST_PAYLOAD, CcdCallbackRequest.class);

        mockMvc.perform(post(URI).contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated",
                hasItem(hasJsonPath("value.DocumentType", is(AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE)))
            )));
    }

}
