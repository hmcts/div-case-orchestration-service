package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.UUID;

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

}