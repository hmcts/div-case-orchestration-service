package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDaGrantedCertificateToDocumentsToPrintTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.notNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaGrantedCertificateDataExtractor {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CaseDataKeys {
        public static final String COMPLEX_TYPE_COLLECTION_ELEMENT = "value";
        public static final String DOCUMENTS_GENERATED = OrchestrationConstants.D8DOCUMENTS_GENERATED;
        public static final String DOCUMENT_TYPE = OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
        public static final String DOCUMENT_URL = "document_url";
        public static final String DOCUMENT_FILE_NAME = "document_filename";
        public static final String DOCUMENT_LINK = "DocumentLink";
    }

    public static GeneratedDocumentInfo.GeneratedDocumentInfoBuilder getDaGrantedDocumentInformPartiallyPopulated(
        Map<String, Object> caseData
    ) {
        Map<String, Object> data = getGeneratedDocumentsFromCaseData(caseData)
            .stream()
            .filter(DaGrantedCertificateDataExtractor::isDaGrantedCertificateDocument)
            .map(element -> getNotEmptyField(element, CaseDataKeys.COMPLEX_TYPE_COLLECTION_ELEMENT))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("daGranted certificate should be found in caseData!"));

        return toGeneratedDocumentInfoBuilder(data);
    }

    private static GeneratedDocumentInfo.GeneratedDocumentInfoBuilder toGeneratedDocumentInfoBuilder(Map<String, Object> document) {
        Map<String, Object> documentLink = getNotEmptyField(document, CaseDataKeys.DOCUMENT_LINK);

        return GeneratedDocumentInfo.builder()
            .fileName(notNull((String) documentLink.get(CaseDataKeys.DOCUMENT_FILE_NAME)))
            .url(notNull((String) documentLink.get(CaseDataKeys.DOCUMENT_URL)));
    }

    public static boolean isDaGrantedCertificateDocument(Map<String, Object> collectionMember) {
        Map<String, Object> document = getNotEmptyField(collectionMember, CaseDataKeys.COMPLEX_TYPE_COLLECTION_ELEMENT);

        return AddDaGrantedCertificateToDocumentsToPrintTask.FileMetadata.DOCUMENT_TYPE.equals(document.get(CaseDataKeys.DOCUMENT_TYPE));
    }

    public static List<Map<String, Object>> getGeneratedDocumentsFromCaseData(Map<String, Object> caseData) {
        return Optional.ofNullable((List<Map<String, Object>>) caseData.get(CaseDataKeys.DOCUMENTS_GENERATED))
            .filter(list -> list.size() > 0)
            .orElseThrow(() -> new IllegalArgumentException("D8GeneratedDocuments should be populated!"));
    }

    private static Map<String, Object> getNotEmptyField(Map<String, Object> data, String field) {
        return Optional.ofNullable((Map<String, Object>) data.get(field))
            .orElseThrow(() -> new IllegalArgumentException(
                    "Invalid data structure. Expected `" + field + "` to be populated."
                )
            );
    }
}
