package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtilsTest;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.DA_GRANTED_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCaseDataWithDocumentsGeneratedList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCollectionMemberWithDocumentType;

@RunWith(MockitoJUnitRunner.class)
public class AddDaGrantedCertificateToDocumentsToPrintTaskTest {

    @Mock
    private DocumentContentFetcherService documentContentFetcherService;

    @InjectMocks
    private AddDaGrantedCertificateToDocumentsToPrintTask addDaGrantedCertificateToDocumentsToPrintTask;

    @Before
    public void setup() {
        when(documentContentFetcherService.fetchPrintContent(any(GeneratedDocumentInfo.class)))
            .thenReturn(GeneratedDocumentInfo.builder().documentType(DA_GRANTED_CERTIFICATE).build());
    }

    @Test
    public void executeAddsFirstDocumentToContext() {
        TaskContext context = new DefaultTaskContext();

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = context.getTransientObject(DOCUMENTS_GENERATED);
        GeneratedDocumentInfo document = documentsToBulkPrint.get(DA_GRANTED_CERTIFICATE);

        assertThat(documentsToBulkPrint.size(), is(1));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(DA_GRANTED_CERTIFICATE));
    }

    @Test
    public void executeAddsAnotherDocumentToContext() {
        TaskContext context = new DefaultTaskContext();
        Map<String, GeneratedDocumentInfo> existingDocumentsToBulkPrint = new HashMap<>();//TODO - make immutable
        final GeneratedDocumentInfo existingDocument = TaskUtilsTest.document();
        existingDocumentsToBulkPrint.put(existingDocument.getDocumentType(), existingDocument);
        context.setTransientObject(DOCUMENTS_GENERATED, existingDocumentsToBulkPrint);

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = context.getTransientObject(DOCUMENTS_GENERATED);
        final GeneratedDocumentInfo document = documentsToBulkPrint.get(DA_GRANTED_CERTIFICATE);

        assertThat(documentsToBulkPrint.size(), is(2));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()), instanceOf(GeneratedDocumentInfo.class));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()).getDocumentType(), is(existingDocument.getDocumentType()));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(DA_GRANTED_CERTIFICATE));
    }
}
