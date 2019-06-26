package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CoRespondentAosPackPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private CoRespondentAosPackPrinter classUnderTest;

    @Test
    public void happyPath() {
        final DefaultTaskContext context = new DefaultTaskContext();

        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo miniPetition = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo coRespondentAosLetter = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", miniPetition);
        generatedDocuments.put("aoscr", coRespondentAosLetter);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        final Map<String, Object> payload = emptyMap();
        final Map<String, Object> result = classUnderTest.execute(context, payload);

        assertThat(result, is(payload));

        verify(bulkPrintService).send(caseId, "co-respondent-aos-pack", asList(coRespondentAosLetter, miniPetition));
    }


    @Test
    public void errorsFromBulkPrintServiceAreReported() {
        final DefaultTaskContext context = new DefaultTaskContext();
        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo miniPetition = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo coRespondentAosLetter = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", miniPetition);
        generatedDocuments.put("aoscr", coRespondentAosLetter);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        doThrow(new RuntimeException()).when(bulkPrintService).send(anyString(), anyString(), anyList());

        classUnderTest.execute(context, emptyMap());

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject(BULK_PRINT_ERROR_KEY), is("Bulk print failed for co-respondent pack"));
    }

    @Test
    public void coRespondentPackCannotBePrintedWithoutTheCoRespondentLetter() {
        final DefaultTaskContext context = new DefaultTaskContext();
        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo miniPetition = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", miniPetition);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        classUnderTest.execute(context, emptyMap());

        verifyZeroInteractions(bulkPrintService);
    }

    @Test
    public void coRespondentPackCannotBePrintedWithoutTheMiniPetition() {
        final DefaultTaskContext context = new DefaultTaskContext();
        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        final GeneratedDocumentInfo respondentAosLetter = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("aoscr", respondentAosLetter);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        classUnderTest.execute(context, emptyMap());

        verifyZeroInteractions(bulkPrintService);
    }
}
