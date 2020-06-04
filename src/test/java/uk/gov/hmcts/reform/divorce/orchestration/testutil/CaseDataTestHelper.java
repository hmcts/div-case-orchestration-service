package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;

import java.util.HashMap;
import java.util.Map;

public class CaseDataTestHelper {

    public static Map<String, Object> createCollectionMemberDocument(String url, String documentType, String fileName) {
        final DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentUrl(url);
        documentLink.setDocumentBinaryUrl(url + "/binary");
        documentLink.setDocumentFilename(fileName + ".pdf");

        final Document document = new Document();
        document.setDocumentFileName(fileName);
        document.setDocumentLink(documentLink);
        document.setDocumentType(documentType);

        final CollectionMember<Document> collectionMember = new CollectionMember<>();
        collectionMember.setValue(document);

        return ObjectMapperTestUtil.convertObject(collectionMember, new TypeReference<HashMap<String, Object>>() {});
    }

}