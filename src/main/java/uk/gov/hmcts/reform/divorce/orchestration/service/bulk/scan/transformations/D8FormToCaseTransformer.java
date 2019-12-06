package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import com.google.common.collect.ImmutableMap;
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

        applyMappingsForPetitionerHomeAddress(ocrDataFields, modifiedMap);
        applyMappingsForPetitionerSolicitorAddress(ocrDataFields, modifiedMap);
        applyMappingsForPetitionerCorrespondenceAddress(ocrDataFields, modifiedMap);

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

    private void applyMappingsForPetitionerHomeAddress(List<OcrDataField> ocrDataFields,
                                                       Map<String, Object> modifiedMap) {
        addMappingsTo(
                "D8PetitionerHomeAddress",
                ImmutableMap.of(
                        "D8PetitionerHomeAddressStreet", "AddressLine1",
                        "D8PetitionerHomeAddressCounty", "County",
                        "D8PetitionerPostCode", "PostCode",
                        "D8PetitionerHomeAddressTown", "PostTown"
                ),
                modifiedMap,
                ocrDataFields);
    }

    private void applyMappingsForPetitionerSolicitorAddress(List<OcrDataField> ocrDataFields,
                                                       Map<String, Object> modifiedMap) {
        addMappingsTo(
                "PetitionerSolicitorAddress",
                ImmutableMap.of(
                        "PetitionerSolicitorAddressStreet", "AddressLine1",
                        "PetitionerSolicitorAddressCounty", "County",
                        "PetitionerSolicitorAddressPostCode", "PostCode",
                        "PetitionerSolicitorAddressTown", "PostTown"
                ),
                modifiedMap,
                ocrDataFields);
    }

    private void applyMappingsForPetitionerCorrespondenceAddress(List<OcrDataField> ocrDataFields,
                                                            Map<String, Object> modifiedMap) {
        addMappingsTo(
                "D8PetitionerCorrespondenceAddress",
                ImmutableMap.of(
                        "D8PetitionerCorrespondenceAddressStreet", "AddressLine1",
                        "D8PetitionerCorrespondenceAddressCounty", "County",
                        "D8PetitionerCorrespondencePostcode", "PostCode",
                        "D8PetitionerCorrespondenceAddressTown", "PostTown"
                ),
                modifiedMap,
                ocrDataFields);
    }

    private void addMappingsTo(String parentField, ImmutableMap<String, String> mappings,
                               Map<String, Object> modifiedMap, List<OcrDataField> ocrDataFields) {
        HashMap<String, Object> parentFieldObject = new HashMap<>();

        mappings.forEach((srcField, targetField) -> {
            mapIfSourceExists(srcField, targetField,
                    parentFieldObject,
                    ocrDataFields);
        });

        if (parentFieldObject.size() > 0) {
            modifiedMap.put(parentField, parentFieldObject);
        }
    }

    private void mapIfSourceExists(String srcField, String targetField, HashMap<String, Object> parentObject,
                                   List<OcrDataField> ocrDataFields) {
        getValueFromOcrDataFields(srcField, ocrDataFields)
            .ifPresent(srcFieldValue -> {
                parentObject.put(targetField, srcFieldValue);
            });
    }
}