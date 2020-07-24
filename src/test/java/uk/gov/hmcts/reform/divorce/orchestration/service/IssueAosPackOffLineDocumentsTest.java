package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.IssueAosPackOfflineDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.util.DocumentGenerator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IssueAosPackOffLineDocumentsTest {
    @Autowired
    private IssueAosPackOfflineDocuments issueAosPackOfflineDocuments;

    @Test
    public void loadIssueAosPackOfflineDocuments() {
        DocumentGenerator expected = new DocumentGenerator();
        expected.setDocumentType(DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        expected.setDocumentTypeForm(AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE);
        expected.setDocumentFileName( AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME);

        DocumentGenerator documentGenerator =
                issueAosPackOfflineDocuments.getIssueAosPackOffLine().get(UNREASONABLE_BEHAVIOUR);
        assertNotNull(documentGenerator);
        assertThat(documentGenerator, equalTo(expected));
    }
}
