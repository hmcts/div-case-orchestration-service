package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateTimeForDocument;

public class TaskTestHelper {

    public static GeneratedDocumentInfo createGeneratedDocument(String url, String documentType, String fileName) {
        return GeneratedDocumentInfo.builder()
            .url(url)
            .documentType(documentType)
            .fileName(fileName)
            .build();
    }

    public static GeneratedDocumentInfo createRandomGeneratedDocument() {
        String randomId = UUID.randomUUID().toString();
        return createGeneratedDocument("http://" + randomId, "docType" + randomId, "filename" + randomId);
    }

    public static String formatWithCurrentDateTime(String documentType) {
        return format(DOCUMENT_FILENAME_FMT, documentType, formatDateTimeForDocument(LocalDateTime.now()));
    }

}