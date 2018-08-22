package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PetitionIssuedCallBackServiceImplUTest {
    private static final String FORM_ID =
        (String)ReflectionTestUtils.getField(PetitionIssuedCallBackServiceImpl.class, "FORM_ID");
    private static final String MINI_PETITION_TEMPLATE_NAME =
        (String)ReflectionTestUtils.getField(PetitionIssuedCallBackServiceImpl.class, "MINI_PETITION_TEMPLATE_NAME");
    private static final String CASE_DETAILS_JSON_KEY =
        (String)ReflectionTestUtils.getField(PetitionIssuedCallBackServiceImpl.class, "CASE_DETAILS_JSON_KEY");
    private static final String DOCUMENT_TYPE_PETITION =
        (String)ReflectionTestUtils.getField(PetitionIssuedCallBackServiceImpl.class, "DOCUMENT_TYPE_PETITION");
    private static final String MINI_PETITION_FILE_NAME_FORMAT =
        (String)ReflectionTestUtils.getField(PetitionIssuedCallBackServiceImpl.class, "MINI_PETITION_FILE_NAME_FORMAT");

    private static final String AUTH_TOKEN = "someToken";
    private static final String CASE_ID = "12345";
    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();

    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(CASE_ID)
            .build();

    private static final ValidationRequest VALIDATION_REQUEST =
        ValidationRequest.builder()
            .data(CASE_DATA)
            .formId(FORM_ID)
            .build();

    private static final ValidationResponse VALIDATION_VALID_RESPONSE = ValidationResponse.builder().build();
    private static final GenerateDocumentRequest GENERATE_DOCUMENT_REQUEST =
        GenerateDocumentRequest.builder()
            .template(MINI_PETITION_TEMPLATE_NAME)
            .values(Collections.singletonMap(CASE_DETAILS_JSON_KEY, CASE_DETAILS))
            .build();

    private static final GeneratedDocumentInfo GENERATE_DOCUMENT_INFO =
        GeneratedDocumentInfo.builder()
            .documentType(DOCUMENT_TYPE_PETITION)
            .fileName(MINI_PETITION_FILE_NAME_FORMAT == null ? null :
                String.format(MINI_PETITION_FILE_NAME_FORMAT, CASE_ID))
            .build();

    private static final DocumentUpdateRequest DOCUMENT_UPDATE_REQUEST =
        DocumentUpdateRequest.builder()
            .documents(Collections.singletonList(GENERATE_DOCUMENT_INFO))
            .caseData(CASE_DATA).build();
    private static final Map<String, Object> FORMATTED_CASE_DATA = Collections.emptyMap();

    @Mock
    private CaseValidationClient caseValidationClient;

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private PetitionIssuedCallBackServiceImpl classUnderTest;

    @Test
    public void givenCaseDataNotValid_whenIssuePetition_thenReturnErrorResponse() {
        final List<String> errors = Arrays.asList("error1", "error2");
        final List<String> warnings = Arrays.asList("warning1", "warning2");

        final ValidationResponse validationResponse =
            ValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .build();

        when(caseValidationClient.validate(VALIDATION_REQUEST)).thenReturn(validationResponse);

        CCDCallbackResponse actual = classUnderTest.issuePetition(CASE_DETAILS, AUTH_TOKEN);

        assertEquals(CASE_DATA, actual.getData());
        assertEquals(errors, actual.getErrors());
        assertEquals(warnings, actual.getWarnings());

        verify(caseValidationClient).validate(VALIDATION_REQUEST);
    }

    @Test
    public void givenCaseDataValid_whenIssuePetition_thenProceedAsExpected() {
        when(caseValidationClient.validate(VALIDATION_REQUEST)).thenReturn(VALIDATION_VALID_RESPONSE);
        when(documentGeneratorClient.generatePDF(GENERATE_DOCUMENT_REQUEST, AUTH_TOKEN))
            .thenReturn(GENERATE_DOCUMENT_INFO);
        when(caseFormatterClient.addDocuments(DOCUMENT_UPDATE_REQUEST)).thenReturn(FORMATTED_CASE_DATA);

        CCDCallbackResponse actual = classUnderTest.issuePetition(CASE_DETAILS, AUTH_TOKEN);

        assertEquals(FORMATTED_CASE_DATA, actual.getData());

        verify(caseValidationClient).validate(VALIDATION_REQUEST);
        verify(documentGeneratorClient).generatePDF(GENERATE_DOCUMENT_REQUEST, AUTH_TOKEN);
        verify(caseFormatterClient).addDocuments(DOCUMENT_UPDATE_REQUEST);
    }
}