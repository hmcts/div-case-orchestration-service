package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DocumentDataExtractor.getDocumentInformPartiallyPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DocumentDataExtractor.isSpecifiedDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.CaseDataKeys.COMPLEX_TYPE_COLLECTION_ELEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractor.CaseDataKeys.DOCUMENTS_GENERATED;

public class DocumentDataExtractorTest {

    public static final List<String> DOCUMENT_TYPES = asList(
        OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE,
        CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE
    );

    @Test
    public void getDocumentInformPartiallyPopulatedReturnsPartiallyPopulatedBuilderOfGeneratedDocumentInfo() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
                asList(
                    buildCollectionMemberWithDocumentType(documentType),
                    buildCollectionMemberWithDocumentType("some-other-document")
                )
            );

            GeneratedDocumentInfo.GeneratedDocumentInfoBuilder actual = getDocumentInformPartiallyPopulated(caseData, documentType);
            GeneratedDocumentInfo document = actual.build();

            assertThat(document.getFileName(), is("file" + documentType));
            assertThat(document.getUrl(), is("url" + documentType));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDocumentInformPartiallyPopulatedThrowsExceptionWhenNoDocumentFound() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
                asList(
                    buildCollectionMemberWithDocumentType("some-other-document")
                )
            );

            getDocumentInformPartiallyPopulated(caseData, documentType);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDocumentInformPartiallyPopulatedThrowsExceptionWhenEmptyListOfDocuments() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(EMPTY_LIST);

            getDocumentInformPartiallyPopulated(caseData, documentType);
        });
    }

    @Test
    public void isSpecifiedDocumentShouldReturnFalseWhenEmptyDocument() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> collectionMember = buildCollectionMember(new HashMap<>());

            assertThat(isSpecifiedDocument(collectionMember, documentType), is(false));
        });
    }

    @Test
    public void isSpecifiedDocumentShouldReturnFalseWhenDocumentTypeIsWrong() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> collectionMember = buildCollectionMemberWithDocumentType("This is wrong type of document");

            assertThat(isSpecifiedDocument(collectionMember, documentType), is(false));
        });
    }

    @Test
    public void isSpecifiedDocumentShouldReturnTrueWhenDocumentTypeIsCorrect() {
        DOCUMENT_TYPES.forEach((documentType) -> {
            Map<String, Object> collectionMember = buildCollectionMemberWithDocumentType(documentType);

            assertThat(isSpecifiedDocument(collectionMember, documentType), is(true));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGeneratedDocumentsFromCaseDataThrowsExceptionWhenNullProvided() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(null);

        DocumentDataExtractor.getGeneratedDocumentsFromCaseData(caseData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGeneratedDocumentsFromCaseDataThrowsExceptionWhenEmptyList() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(EMPTY_LIST);

        DocumentDataExtractor.getGeneratedDocumentsFromCaseData(caseData);
    }

    @Test
    public void getGeneratedDocumentsFromCaseDataReturnsListWhenNotEmpty() {
        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(asList(EMPTY_MAP));

        assertThat(DocumentDataExtractor.getGeneratedDocumentsFromCaseData(caseData).size(), is(1));
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
        document.put(DocumentDataExtractor.CaseDataKeys.DOCUMENT_LINK, buildDocumentLink(documentType));
        document.put(DocumentDataExtractor.CaseDataKeys.DOCUMENT_TYPE, documentType);

        return document;
    }

    private static Map<String, Object> buildDocumentLink(String type) {
        Map<String, Object> documentLink = new HashMap<>();

        documentLink.put(DocumentDataExtractor.CaseDataKeys.DOCUMENT_FILE_NAME, "file" + type);
        documentLink.put(DocumentDataExtractor.CaseDataKeys.DOCUMENT_URL, "url" + type);

        return documentLink;
    }
}