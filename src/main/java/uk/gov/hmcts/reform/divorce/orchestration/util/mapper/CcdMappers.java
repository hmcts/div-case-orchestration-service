package uk.gov.hmcts.reform.divorce.orchestration.util.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdMappers {

    public static CollectionMember<Document> mapDocumentInfoToCcdDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        Document document = new Document();
        document.setDocumentFileName(generatedDocumentInfo.getFileName());
        document.setDocumentType(generatedDocumentInfo.getDocumentType());

        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentUrl(generatedDocumentInfo.getUrl());
        documentLink.setDocumentBinaryUrl(generatedDocumentInfo.getUrl() + "/binary");
        documentLink.setDocumentFilename(generatedDocumentInfo.getFileName() + ".pdf");
        document.setDocumentLink(documentLink);

        CollectionMember<Document> collectionMember = new CollectionMember<>();
        collectionMember.setValue(document);

        return collectionMember;
    }

}
