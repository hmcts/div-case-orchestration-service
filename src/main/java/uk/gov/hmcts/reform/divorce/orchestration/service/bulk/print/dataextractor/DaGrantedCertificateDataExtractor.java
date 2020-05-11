package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDaGrantedCertificateToDocumentsToPrintTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.notNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DaGrantedCertificateDataExtractor {

    @NoArgsConstructor
    public static class CaseDataKeys {
        public static final String COMPLEX_TYPE_COLLECTION_ELEMENT = "value";
        public static final String DOCUMENTS_GENERATED = OrchestrationConstants.D8DOCUMENTS_GENERATED;
        public static final String DOCUMENT_TYPE = OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
        public static final String DOCUMENT_URL = "document_url";
        public static final String DOCUMENT_FILE_NAME = "document_filename";
    }

    public static GeneratedDocumentInfo getExistingDaGrantedFromCaseData(Map<String, Object> caseData) {
        return getDaGrantedDocumentInformPartiallyPopulated(caseData)
            .documentType(AddDaGrantedCertificateToDocumentsToPrintTask.FileMetadata.DOCUMENT_TYPE)
            .build();
    }

    public static GeneratedDocumentInfo.GeneratedDocumentInfoBuilder getDaGrantedDocumentInformPartiallyPopulated(Map<String, Object> caseData) {
        return getGeneratedDocumentsFromCaseData(caseData)
            .stream()
            .filter(isDaGrantedCertificateDocument())
            .findFirst()
            .map(toGeneratedDocumentInfoBuilder())
            .orElseThrow(() -> new IllegalArgumentException("daGranted certificate should be found in caseData!"));
    }

    public static Function<Map<String, Object>, GeneratedDocumentInfo.GeneratedDocumentInfoBuilder> toGeneratedDocumentInfoBuilder() {
        return documentLink -> GeneratedDocumentInfo.builder()
            .fileName(notNull((String) documentLink.get(CaseDataKeys.DOCUMENT_FILE_NAME)))
            .url(notNull((String) documentLink.get(CaseDataKeys.DOCUMENT_URL)));
    }

    public static Predicate<Map<String, Object>> isDaGrantedCertificateDocument() {
        return collectionMember -> {
            Map<String, Object> document = (Map<String, Object>) collectionMember.get(CaseDataKeys.COMPLEX_TYPE_COLLECTION_ELEMENT);

            return AddDaGrantedCertificateToDocumentsToPrintTask.FileMetadata.DOCUMENT_TYPE.equals(document.get(CaseDataKeys.DOCUMENT_TYPE));
        };
    }

    public static List<Map<String, Object>> getGeneratedDocumentsFromCaseData(Map<String, Object> caseData) {
        return Optional.ofNullable((List<Map<String, Object>>) caseData.get(CaseDataKeys.DOCUMENTS_GENERATED))
            .orElseThrow(() -> new IllegalArgumentException("D8GeneratedDocuments should be populated!"));
    }
}
