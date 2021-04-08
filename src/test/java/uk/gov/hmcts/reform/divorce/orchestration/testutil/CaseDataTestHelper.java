package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObject;

public class CaseDataTestHelper {

    public static CollectionMember<Document> createCollectionMemberDocument(String url, String documentType, String fileName) {
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

        return collectionMember;
    }

    public static Map<String, Object> createCollectionMemberDocumentAsMap(String url, String documentType, String fileName) {
        CollectionMember<Document> document = createCollectionMemberDocument(url, documentType, fileName);
        return convertObject(document, new TypeReference<>() {});
    }

    public static OrganisationPolicy buildOrganisationPolicy() {
        return buildOrganisationPolicy(TEST_ORGANISATION_POLICY_ID);
    }

    public static OrganisationPolicy buildOrganisationPolicy(String orgId) {
        return OrganisationPolicy.builder()
            .organisation(
                Organisation.builder()
                    .organisationID(orgId)
                    .organisationName(TEST_ORGANISATION_POLICY_NAME)
                    .build())
            .build();
    }

}