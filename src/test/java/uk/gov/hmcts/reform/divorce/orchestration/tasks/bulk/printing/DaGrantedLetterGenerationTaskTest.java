package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.ADDRESS_LINE1;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.ADDRESS_LINE2;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.COUNTY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.POSTCODE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor.CaseDataKeys.TOWN;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractorTest.buildCaseDataWithAddressee;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class DaGrantedLetterGenerationTaskTest {

    private static final String RESPONDENTS_FIRST_NAME = "Jane";
    private static final String RESPONDENTS_LAST_NAME = "Doe";
    private static final String PETITIONERS_FIRST_NAME = "John";
    private static final String PETITIONERS_LAST_NAME = "Doe";
    private static final String VALID_DATE = "2020-05-11";

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @InjectMocks
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CtscContactDetails.builder().build());
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();
        Map<String, Object> caseData = buildCaseData();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), anyString(), anyString())).thenReturn(generatedDocumentInfo);

        Map<String, Object> result = daGrantedLetterGenerationTask.execute(context, caseData);

        assertEquals(result, caseData);
        assertThat(context.getTransientObject(GENERATED_DOCUMENTS), hasItems(generatedDocumentInfo));

        verify(pdfDocumentGenerationService, times(1)).generatePdf(any(DocmosisTemplateVars.class), anyString(), anyString());
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
    }


    @Test
    public void testPrepareDataForPdfCorrectlyMapsToValidTemplateVars() throws TaskException, IOException {
        TaskContext context = prepareTaskContext();
        Map<String, Object> caseData = buildCaseData();

        DaGrantedLetter daGrantedLetter = (DaGrantedLetter) daGrantedLetterGenerationTask.prepareDataForPdf(context, caseData);

        assertEquals(daGrantedLetter.getCaseReference(), TEST_CASE_ID);
        assertEquals(daGrantedLetter.getLetterDate(), VALID_DATE);
        assertThat(daGrantedLetter.getRespondentFullName(), is("Jane Doe"));
        assertThat(daGrantedLetter.getPetitionerFullName(), is("John Doe"));
        assertThat(daGrantedLetter.getAddressee().getName(), is("Jane Doe"));
        assertNotNull(daGrantedLetter.getCtscContactDetails());
        assertNotNull(daGrantedLetter.getAddressee().getFormattedAddress(), is("line1\nline2\ntown\npostcode"));
    }


    private TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put(DA_GRANTED_DATE, VALID_DATE);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);

        caseData.put(ADDRESS_LINE1, "line1");
        caseData.put(ADDRESS_LINE2, "line2");
        caseData.put(TOWN, "town");
        caseData.put(COUNTY, "county");
        caseData.put(POSTCODE, "postcode");

        return caseData;
    }
}
