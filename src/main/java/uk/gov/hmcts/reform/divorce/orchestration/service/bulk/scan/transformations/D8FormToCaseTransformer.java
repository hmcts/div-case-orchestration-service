package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_DAY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformFormDateIntoLocalDate;

@Component
public class D8FormToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = d8ExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        Map<String, Object> modifiedMap = new HashMap<>();

        ocrDataFields.stream()
            .filter(f -> f.getName().equals("D8ReasonForDivorceSeparationDate"))
            .map(OcrDataField::getValue)
            .map(formDate -> transformFormDateIntoLocalDate("D8ReasonForDivorceSeparationDate", formDate))
            .findFirst()
            .ifPresent(localDate -> {
                modifiedMap.put(D_8_REASON_FOR_DIVORCE_SEPARATION_DAY, String.valueOf(localDate.getDayOfMonth()));
                modifiedMap.put(D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH, String.valueOf(localDate.getMonthValue()));
                modifiedMap.put(D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR, String.valueOf(localDate.getYear()));
            });

        transformPetitionerHomeAddressFieldsIn(ocrDataFields, modifiedMap);

        getValueFromOcrDataFields("PetitionerSolicitorAddressPostCode", ocrDataFields)
            .ifPresent(petitionerSolicitorPostcode -> {
                HashMap<String, Object> petitionerSolicitorAddressObject = new HashMap<>();
                petitionerSolicitorAddressObject.put("PostCode", petitionerSolicitorPostcode);
                modifiedMap.put("PetitionerSolicitorAddress", petitionerSolicitorAddressObject);
            });

        getValueFromOcrDataFields("D8PetitionerCorrespondencePostcode", ocrDataFields)
            .ifPresent(petitionerCorrespondencePostcode -> {
                HashMap<String, Object> d8PetitionerCorrespondenceAddressObject = new HashMap<>();
                d8PetitionerCorrespondenceAddressObject.put("PostCode", petitionerCorrespondencePostcode);
                modifiedMap.put("D8PetitionerCorrespondenceAddress", d8PetitionerCorrespondenceAddressObject);
            });

        return modifiedMap;
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {
        transformedCaseData.replace("D8PaymentMethod", "Debit/Credit Card", "Card");

        return transformedCaseData;
    }

    private Optional<String> getValueFromOcrDataFields(String fieldName, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .map(OcrDataField::getValue)
            .findFirst();
    }

    private static Map<String, String> d8ExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        // Help With Fees
        erToCcdFieldsMap.put("D8HelpWithFeesReferenceNumber", "D8HelpWithFeesReferenceNumber");
        erToCcdFieldsMap.put("D8PaymentMethod", "D8PaymentMethod");

        // Section 1 - Your application (known as a petition in divorce and judicial separation)
        erToCcdFieldsMap.put("D8LegalProcess", "D8LegalProcess");
        erToCcdFieldsMap.put("D8ScreenHasMarriageCert", "D8ScreenHasMarriageCert");
        erToCcdFieldsMap.put("D8CertificateInEnglish", "D8CertificateInEnglish");

        // Section 2 - About you (the applicant/petitioner)
        erToCcdFieldsMap.put("D8PetitionerFirstName", "D8PetitionerFirstName");
        erToCcdFieldsMap.put("D8PetitionerLastName", "D8PetitionerLastName");
        erToCcdFieldsMap.put("D8PetitionerPhoneNumber", "D8PetitionerPhoneNumber");
        erToCcdFieldsMap.put("D8PetitionerEmail", "D8PetitionerEmail");
        erToCcdFieldsMap.put("D8PetitionerNameChangedHow", "D8PetitionerNameChangedHow");
        erToCcdFieldsMap.put("D8PetitionerContactDetailsConfidential", "D8PetitionerContactDetailsConfidential");

        erToCcdFieldsMap.put("PetitionerSolicitor", "PetitionerSolicitor");
        erToCcdFieldsMap.put("PetitionerSolicitorName", "PetitionerSolicitorName");
        erToCcdFieldsMap.put("D8SolicitorReference", "D8SolicitorReference");
        erToCcdFieldsMap.put("PetitionerSolicitorFirm", "PetitionerSolicitorFirm");
        erToCcdFieldsMap.put("PetitionerSolicitorPhone", "PetitionerSolicitorPhone");
        erToCcdFieldsMap.put("PetitionerSolicitorEmail", "PetitionerSolicitorEmail");
        erToCcdFieldsMap.put("D8PetitionerCorrespondenceUseHomeAddress", "D8PetitionerCorrespondenceUseHomeAddress");

        // Section 3 - About your spouse/civil partner (the respondent)
        erToCcdFieldsMap.put("D8RespondentFirstName", "D8RespondentFirstName");
        erToCcdFieldsMap.put("D8RespondentLastName", "D8RespondentLastName");
        erToCcdFieldsMap.put("D8RespondentPhoneNumber", "D8RespondentPhoneNumber");

        // Section 4 - Details of marriage/civil partnership
        erToCcdFieldsMap.put("D8MarriagePetitionerName", "D8MarriagePetitionerName");
        erToCcdFieldsMap.put("D8MarriageRespondentName", "D8MarriageRespondentName");

        return erToCcdFieldsMap;
    }

    private void transformPetitionerHomeAddressFieldsIn(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        HashMap<String, Object> d8petitionerHomeAddressObject = new HashMap<>();

        getValueFromOcrDataFields("D8PetitionerHomeAddressStreet", ocrDataFields)
            .ifPresent(petitionerHomeAddressStreet -> {
                d8petitionerHomeAddressObject.put("AddressLine1", petitionerHomeAddressStreet);
            });
        getValueFromOcrDataFields("D8PetitionerPostCode", ocrDataFields)
            .ifPresent(petitionerPostCode -> {
                d8petitionerHomeAddressObject.put("PostCode", petitionerPostCode);
            });

        if (d8petitionerHomeAddressObject.size() > 0) {
            modifiedMap.put("D8PetitionerHomeAddress", d8petitionerHomeAddressObject);
        }
    }
}