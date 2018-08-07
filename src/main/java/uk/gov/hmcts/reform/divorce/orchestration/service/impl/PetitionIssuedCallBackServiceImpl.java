package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.PetitionIssuedCallBackService;

import java.util.Collections;
import java.util.Map;

@Service
public class PetitionIssuedCallBackServiceImpl implements PetitionIssuedCallBackService {
    private static final String FORM_ID = "case-progression";
    private static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";

    @Autowired
    private CaseValidationClient caseValidationClient;

    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    @Override
    public CCDCallbackResponse issuePetition(Map<String, Object> caseData, String authToken) {
        ValidationResponse validationResponse =
            caseValidationClient.validate(
                ValidationRequest.builder()
                    .data(caseData)
                    .formId(FORM_ID)
                    .build());

        if (!validationResponse.isValid()) {
            return CCDCallbackResponse.builder()
                .data(caseData)
                .errors(validationResponse.getErrors())
                .warnings(validationResponse.getWarnings())
                .build();
        }

        GeneratedDocumentInfo miniPetition =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(MINI_PETITION_TEMPLATE_NAME)
                    .values(caseData)
                .build(), authToken
            );

        return CCDCallbackResponse.builder()
            .data(
                caseFormatterClient.addDocuments(
                    DocumentUpdateRequest.builder()
                        .caseData(caseData)
                        .documents(Collections.singletonList(miniPetition))
                        .build()
                )
            ).build();
    }
}
