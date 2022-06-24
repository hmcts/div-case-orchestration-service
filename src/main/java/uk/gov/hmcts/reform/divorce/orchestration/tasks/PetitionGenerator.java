package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;

@Component
public class PetitionGenerator extends PetitionGeneratorBase {

    @Autowired
    public PetitionGenerator(DocumentGeneratorClient documentGeneratorClient) {
        super(documentGeneratorClient, DocumentType.DIVORCE_MINI_PETITION);
    }
}
