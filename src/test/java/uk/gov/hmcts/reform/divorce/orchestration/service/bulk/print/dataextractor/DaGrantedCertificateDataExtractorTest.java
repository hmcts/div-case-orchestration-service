package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.CaseDataKeys.COMPLEX_TYPE_COLLECTION_ELEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.getDaGrantedDocumentInformPartiallyPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.isDaGrantedCertificateDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDaGrantedCertificateToDocumentsToPrintTask.FileMetadata.DOCUMENT_TYPE;

public class DaGrantedCertificateDataExtractorTest {

    public static final String DA_GRANTED_CERTIFICATE = DOCUMENT_TYPE;

    @Test
    public void getDaGrantedDocumentInformPartiallyPopulatedReturnsPartiallyPopulatedBuilderOfGeneratedDocumentInfo() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE),
                buildCollectionMemberWithDocumentType("some-other-document")
            )
        );

        GeneratedDocumentInfo.GeneratedDocumentInfoBuilder actual = getDaGrantedDocumentInformPartiallyPopulated(caseData);
        GeneratedDocumentInfo document = actual.build();

        assertThat(document.getFileName(), is("file" + DA_GRANTED_CERTIFICATE));
        assertThat(document.getUrl(), is("url" + DA_GRANTED_CERTIFICATE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDaGrantedDocumentInformPartiallyPopulatedThrowsExceptionWhenNoDaGrantedDocumentFound() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType("some-other-document")
            )
        );

        getDaGrantedDocumentInformPartiallyPopulated(caseData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDaGrantedDocumentInformPartiallyPopulatedThrowsExceptionWhenEmptyListOfDocuments() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(EMPTY_LIST);

        getDaGrantedDocumentInformPartiallyPopulated(caseData);
    }

    @Test
    public void isDaGrantedCertificateDocumentShouldReturnFalseWhenEmptyDocument() {
        Map<String, Object> collectionMember = buildCollectionMember(new HashMap<>());

        assertThat(isDaGrantedCertificateDocument(collectionMember), is(false));
    }

    @Test
    public void isDaGrantedCertificateDocumentShouldReturnFalseWhenDocumentTypeIsWrong() {
        Map<String, Object> collectionMember = buildCollectionMemberWithDocumentType("This is wrong type of document");

        assertThat(isDaGrantedCertificateDocument(collectionMember), is(false));
    }

    @Test
    public void isDaGrantedCertificateDocumentShouldReturnTrueWhenDocumentTypeIsDaGranted() {
        Map<String, Object> collectionMember = buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE);

        assertThat(isDaGrantedCertificateDocument(collectionMember), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGeneratedDocumentsFromCaseDataThrowsExceptionWhenNullProvided() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(null);

        DaGrantedCertificateDataExtractor.getGeneratedDocumentsFromCaseData(caseData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGeneratedDocumentsFromCaseDataThrowsExceptionWhenEmptyList() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(EMPTY_LIST);

        DaGrantedCertificateDataExtractor.getGeneratedDocumentsFromCaseData(caseData);
    }

    @Test
    public void getGeneratedDocumentsFromCaseDataReturnsListWhenNotEmpty() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(asList(EMPTY_MAP));

        assertThat(DaGrantedCertificateDataExtractor.getGeneratedDocumentsFromCaseData(caseData).size(), is(1));
    }

    public static Map<String, Object> buildCaseDataWithDocumentsGeneratedList(Object list) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DOCUMENTS_GENERATED, list);

        return caseData;
    }

    private static Map<String, Object> buildCollectionMember(Map<String, Object> element) {
        Map<String, Object> collectionMember = new HashMap<>();
        collectionMember.put(COMPLEX_TYPE_COLLECTION_ELEMENT, element);

        return collectionMember;
    }

    public static Map<String, Object> buildCollectionMemberWithDocumentType(String documentType) {
        return buildCollectionMember(
            buildDocument(documentType)
        );
    }

    private static Map<String, Object> buildDocument(String documentType) {
        Map<String, Object> document = new HashMap<>();
        document.put(DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENT_LINK, buildDocumentLink(documentType));
        document.put(DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENT_TYPE, documentType);

        return document;
    }

    private static Map<String, Object> buildDocumentLink(String type) {
        Map<String, Object> documentLink = new HashMap<>();

        documentLink.put(DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENT_FILE_NAME, "file" + type);
        documentLink.put(DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENT_URL, "url" + type);

        return documentLink;
    }
}