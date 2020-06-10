package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class BasePayloadSpecificDocumentGenerationTaskTest {

    private static final String TEST_TEMPLATE_ID = "TEST_TEMPLATE_ID";

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CcdUtil ccdUtil;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentsCaptor;

    private DocmosisTemplateVars testDocmosisTemplateVars;
    private GeneratedDocumentInfo newGeneratedDocument;
    private Map<String, Object> modifiedCaseData;
    private DefaultTaskContext taskContext;

    @Before
    public void setUp() {
        testDocmosisTemplateVars = new DocmosisTemplateVars();
        newGeneratedDocument = GeneratedDocumentInfo.builder().build();
        when(pdfDocumentGenerationService.generatePdf(any(), any(), any())).thenReturn(newGeneratedDocument);

        modifiedCaseData = Collections.singletonMap("modifiedKey", "modifiedValue");
        when(ccdUtil.addNewDocumentsToCaseData(any(), any())).thenReturn(modifiedCaseData);

        taskContext = new DefaultTaskContext();
    }

    @Test
    public void makeSureAbstractClassExecutesDesiredBehaviour() throws TaskException {
        BasePayloadSpecificDocumentGenerationTask testDocGenerationTask =
            new BasePayloadSpecificDocumentGenerationTask(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil) {
                @Override
                protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
                    return testDocmosisTemplateVars;
                }

                @Override
                protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo) {
                    return documentInfo;
                }

                @Override
                protected String getDocumentType() {
                    return null;
                }

                @Override
                protected String getTemplateId() {
                    return TEST_TEMPLATE_ID;
                }
            };

        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> incomingCaseData = Collections.singletonMap("testKey", "testValue");
        Map<String, Object> returnedCaseData = testDocGenerationTask.execute(taskContext, incomingCaseData);

        assertThat(returnedCaseData, equalTo(modifiedCaseData));
        verify(pdfDocumentGenerationService).generatePdf(testDocmosisTemplateVars, TEST_TEMPLATE_ID, AUTH_TOKEN);
        verify(ccdUtil).addNewDocumentsToCaseData(eq(incomingCaseData), newDocumentsCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentsToAdd = newDocumentsCaptor.getValue();
        assertThat(newDocumentsToAdd, hasSize(1));
        assertThat(newDocumentsToAdd.get(0), is(newGeneratedDocument));

    }

}