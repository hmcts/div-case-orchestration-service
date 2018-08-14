package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.PetitionIssuedCallBackService;

import java.util.Collections;
import java.util.Map;

@Service
public class PetitionIssuedCallBackServiceImpl implements PetitionIssuedCallBackService {
    private static final String FORM_ID = "case-progression";
    private static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    private static final String CASE_DETAILS_JSON_KEY = "caseDetails";
    private static final String DOCUMENT_TYPE_PETITION = "petition";
    private static final String MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s";
    private static final String DOCUMENT_TYPE_INVITATION = "aosinvitation";
    private static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";
    private static final String INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    private static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    private static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    private static final String ACCESS_CODE = "access_code";
    private static final String RESPONDENT_LETTER_HOLDER_ID = "respondentLetterHolderId";

    @Autowired
    private CaseValidationClient caseValidationClient;

    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    @Autowired
    private IdamClient idamClient;

    @Override
    public CCDCallbackResponse issuePetitionAndAosLetter(CaseDetails caseDetails, String authToken) {
        Map<String, Object> caseData = caseDetails.getCaseData();
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
                                .values(Collections.singletonMap(CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authToken
                );


        miniPetition.setDocumentType(DOCUMENT_TYPE_PETITION);
        miniPetition.setFileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        Pin pin = idamClient.createPin(PinRequest.builder()
                        .firstName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_FIRST_NAME, "")))
                        .lastName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_LAST_NAME, "")))
                        .build(),
                authToken);

        caseData.put(RESPONDENT_LETTER_HOLDER_ID, pin.getUserId());

        GeneratedDocumentInfo aosinvitation =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                                .values(ImmutableMap.of(CASE_DETAILS_JSON_KEY, caseDetails, ACCESS_CODE, pin.getPin()))
                                .build(),
                        authToken
                );

        aosinvitation.setDocumentType(DOCUMENT_TYPE_INVITATION);
        aosinvitation.setFileName(String.format(INVITATION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        return CCDCallbackResponse.builder()
                .data(
                        caseFormatterClient.addDocuments(
                                DocumentUpdateRequest.builder()
                                        .caseData(caseData)
                                        .documents(ImmutableList.of(miniPetition, aosinvitation))
                                        .build()
                        )
                ).build();
    }

}
