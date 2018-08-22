package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.PetitionIssuedCallBackService;

import java.util.Collections;

@Service
public class PetitionIssuedCallBackServiceImpl implements PetitionIssuedCallBackService {
    private static final String FORM_ID = "case-progression";
    private static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    private static final String CASE_DETAILS_JSON_KEY = "caseDetails";
    private static final String DOCUMENT_TYPE_PETITION = "petition";
    private static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";

    @Autowired
    private CaseValidationClient caseValidationClient;

    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    @Override
    public CCDCallbackResponse issuePetition(CaseDetails caseDetails, String authToken) {
        ValidationResponse validationResponse =
            caseValidationClient.validate(
                ValidationRequest.builder()
                    .data(caseDetails.getCaseData())
                    .formId(FORM_ID)
                    .build());

        if (!validationResponse.isValid()) {
            return CCDCallbackResponse.builder()
                .data(caseDetails.getCaseData())
                .errors(validationResponse.getErrors())
                .warnings(validationResponse.getWarnings())
                .build();
        }

        GeneratedDocumentInfo miniPetition =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(MINI_PETITION_TEMPLATE_NAME)
                    .values(Collections.singletonMap(CASE_DETAILS_JSON_KEY, caseDetails))
                .build(), authToken
            );

        miniPetition.setDocumentType(DOCUMENT_TYPE_PETITION);
        miniPetition.setFileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        return CCDCallbackResponse.builder()
            .data(
                caseFormatterClient.addDocuments(
                    DocumentUpdateRequest.builder()
                        .caseData(caseDetails.getCaseData())
                        .documents(Collections.singletonList(miniPetition))
                        .build()
                )
            ).build();
    }
}
