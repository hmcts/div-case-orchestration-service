package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import com.google.common.collect.Sets;
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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
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

        Set<GeneratedDocumentInfo> documentCollection = context.getTransientObject(DOCUMENT_COLLECTION);

        assertThat(documentCollection.size(), is(1));
        GeneratedDocumentInfo document = documentCollection.stream().findFirst().get();
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(DA_GRANTED_CERTIFICATE));
    }

    @Test
    public void executeAddsAnotherDocumentToContext() {
        TaskContext context = new DefaultTaskContext();
        final GeneratedDocumentInfo existingDocument = TaskUtilsTest.document();
        context.setTransientObject(DOCUMENT_COLLECTION, Sets.newHashSet(existingDocument));
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        Set<GeneratedDocumentInfo> documentsToBulkPrint = context.getTransientObject(DOCUMENT_COLLECTION);
        assertThat(documentsToBulkPrint.size(), is(2));
        Set<String> documentTypes = documentsToBulkPrint.stream().map(GeneratedDocumentInfo::getDocumentType).collect(Collectors.toSet());
        assertThat(documentTypes, containsInAnyOrder(
            existingDocument.getDocumentType(),
            DA_GRANTED_CERTIFICATE
        ));
    }

}