package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;

@Component
public class PetitionRegenerator extends PetitionGeneratorBase {

    @Autowired
    public PetitionRegenerator(DocumentGeneratorClient documentGeneratorClient) {
        super(documentGeneratorClient, DocumentType.DIVORCE_MINI_PETITION_REGENERATE);
    }

}
