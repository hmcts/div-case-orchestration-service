package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_DAY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformDateFromComponentsToCcdDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.BulkScanHelper.transformFormDateIntoLocalDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.BulkScanFormValidator.TRUE;

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
        Map<String, Object> transformedCaseData = new HashMap<>();

        getValueFromOcrDataFields("D8ReasonForDivorceSeparationDate", ocrDataFields)
            .map(formDate -> transformFormDateIntoLocalDate("D8ReasonForDivorceSeparationDate", formDate))
            .ifPresent(localDate -> {
                transformedCaseData.put(D_8_REASON_FOR_DIVORCE_SEPARATION_DAY, String.valueOf(localDate.getDayOfMonth()));
                transformedCaseData.put(D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH, String.valueOf(localDate.getMonthValue()));
                transformedCaseData.put(D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR, String.valueOf(localDate.getYear()));
            });

        applyMappingsForPetitionerHomeAddress(ocrDataFields, transformedCaseData);
        applyMappingsForPetitionerSolicitorAddress(ocrDataFields, transformedCaseData);
        applyMappingsForPetitionerCorrespondenceAddress(ocrDataFields, transformedCaseData);
        applyMappingsForDivorceAdultery3rdPartyAddress(ocrDataFields, transformedCaseData);
        applyMappingsForRespondentHomeAddress(ocrDataFields, transformedCaseData);
        applyMappingsForRespondentSolicitorAddress(ocrDataFields, transformedCaseData);

        applyMappingForDate(ocrDataFields, transformedCaseData, "D8MarriageDate");
        applyMappingForDate(ocrDataFields, transformedCaseData, "D8MentalSeparationDate");
        applyMappingForDate(ocrDataFields, transformedCaseData, "D8PhysicalSeparationDate");

        getValueFromOcrDataFields("D8PetitionerContactDetailsConfidential", ocrDataFields)
            .ifPresent(value -> {
                if (YES_VALUE.equalsIgnoreCase(value)) {
                    transformedCaseData.put("D8PetitionerContactDetailsConfidential", "keep");
                } else if (NO_VALUE.equalsIgnoreCase(value)) {
                    transformedCaseData.put("D8PetitionerContactDetailsConfidential", "share");
                }
            });

        transformTrueOrFalseValuesIntoYesOrNo(ocrDataFields, "D8ScreenHasMarriageCert", transformedCaseData);
        transformTrueOrFalseValuesIntoYesOrNo(ocrDataFields, "D8CertificateInEnglish", transformedCaseData);

        return transformedCaseData;
    }

    @Override
    protected Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {
        Map<String, Object> modifiedCaseData = new HashMap<>(transformedCaseData);

        modifiedCaseData.replace("D8PaymentMethod", "Debit/Credit Card", "card");
        modifiedCaseData.replace("D8PaymentMethod", "Cheque", "cheque");

        modifiedCaseData.replace("D8FinancialOrderFor", "myself", asList("petitioner"));
        modifiedCaseData.replace("D8FinancialOrderFor", "my children", asList("children"));
        modifiedCaseData.replace("D8FinancialOrderFor", "myself, my children", asList("petitioner", "children"));

        modifiedCaseData.replace("D8legalProcess", "Divorce", "divorce");
        modifiedCaseData.replace("D8legalProcess", "Dissolution", "dissolution");
        modifiedCaseData.replace("D8legalProcess", "Judicial (separation)", "judicialSeparation");

        Optional.ofNullable(modifiedCaseData.get("D8DivorceClaimFrom"))
            .map(String.class::cast)
            .map(value -> value.replace("corespondent", "correspondent"))
            .ifPresent(value -> modifiedCaseData.replace("D8DivorceClaimFrom", value));

        transformCommaSeparatedStringIntoList(modifiedCaseData, "D8AppliesForStatementOfTruth");
        transformCommaSeparatedStringIntoList(modifiedCaseData, "D8DivorceClaimFrom");
        transformCommaSeparatedStringIntoList(modifiedCaseData, "D8FinancialOrderStatementOfTruth");

        transformDateIntoIsoFormat(modifiedCaseData, "D8StatementOfTruthDate");

        return modifiedCaseData;
    }

    private void transformCommaSeparatedStringIntoList(Map<String, Object> transformedCaseData, String fieldName) {
        Optional.ofNullable(transformedCaseData.get(fieldName))
            .map(String.class::cast)
            .map(value -> value.split(","))
            .flatMap(valuesArray -> Optional.of(Arrays.stream(valuesArray).map(String::trim).collect(toList())))
            .ifPresent(listOfValues -> transformedCaseData.put(fieldName, listOfValues));
    }

    private void transformTrueOrFalseValuesIntoYesOrNo(List<OcrDataField> ocrDataFields, String fieldName, Map<String, Object> transformedCaseData) {
        getValueFromOcrDataFields(fieldName, ocrDataFields)
            .map(value -> StringUtils.isEmpty(value) ? null : value)
            .map(TRUE::equalsIgnoreCase)
            .map(valueEqualsTrue -> valueEqualsTrue ? YES_VALUE : NO_VALUE)
            .ifPresent(transformedValue -> transformedCaseData.put(fieldName, transformedValue));
    }

    private void transformDateIntoIsoFormat(Map<String, Object> transformedCaseData, String fieldName) {
        Optional.ofNullable(transformedCaseData.get(fieldName))
            .map(String.class::cast)
            .map(value -> BulkScanHelper.transformFormDateIntoLocalDate(fieldName, value))
            .ifPresent(date -> transformedCaseData.put(fieldName, date.toString()));
    }

    private void applyMappingForDate(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap, String field) {
        Optional.ofNullable(getDateFieldsAndConcatenate(ocrDataFields, field))
            .ifPresent(date -> modifiedMap.put(field, date));
    }

    private String getDateFieldsAndConcatenate(List<OcrDataField> ocrDataFields, String prefix) {
        Optional<String> day = getValueFromOcrDataFields(prefix + "Day", ocrDataFields);
        Optional<String> month = getValueFromOcrDataFields(prefix + "Month", ocrDataFields);
        Optional<String> year = getValueFromOcrDataFields(prefix + "Year", ocrDataFields);

        if (day.isPresent() && month.isPresent() && year.isPresent()) {
            return transformDateFromComponentsToCcdDate(day.get(), month.get(), year.get());
        }

        return null;
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
        erToCcdFieldsMap.put("D8LegalProcess", "D8legalProcess");
        erToCcdFieldsMap.put("D8ScreenHasMarriageCert", "D8ScreenHasMarriageCert");
        erToCcdFieldsMap.put("D8CertificateInEnglish", "D8CertificateInEnglish");

        // Section 2 - About you (the applicant/petitioner)
        erToCcdFieldsMap.put("D8PetitionerFirstName", "D8PetitionerFirstName");
        erToCcdFieldsMap.put("D8PetitionerLastName", "D8PetitionerLastName");
        erToCcdFieldsMap.put("D8PetitionerPhoneNumber", "D8PetitionerPhoneNumber");
        erToCcdFieldsMap.put("D8PetitionerEmail", "D8PetitionerEmail");
        erToCcdFieldsMap.put("D8PetitionerHasNameChanged", "D8PetitionerHasNameChanged");
        erToCcdFieldsMap.put("D8PetitionerContactDetailsConfidential", "D8PetitionerContactDetailsConfidential");
        erToCcdFieldsMap.put("PetitionerSolicitor", "PetitionerSolicitor");
        erToCcdFieldsMap.put("PetitionerSolicitorName", "PetitionerSolicitorName");
        erToCcdFieldsMap.put("D8SolicitorReference", "D8SolicitorReference");
        erToCcdFieldsMap.put("PetitionerSolicitorFirm", "PetitionerSolicitorFirm");
        erToCcdFieldsMap.put("PetitionerSolicitorPhone", "PetitionerSolicitorPhone");
        erToCcdFieldsMap.put("PetitionerSolicitorEmail", "PetitionerSolicitorEmail");
        erToCcdFieldsMap.put("D8PetitionerCorrespondenceUseHomeAddress", "D8PetitionerCorrespondenceUseHomeAddress");
        erToCcdFieldsMap.put("D8PetitionerNameChangedHowOtherDetails", "D8PetitionerNameChangedHowOtherDetails");

        // Section 3 - About your spouse/civil partner (the respondent)
        erToCcdFieldsMap.put("D8RespondentFirstName", "D8RespondentFirstName");
        erToCcdFieldsMap.put("D8RespondentLastName", "D8RespondentLastName");
        erToCcdFieldsMap.put("D8RespondentPhoneNumber", "D8RespondentPhoneNumber");
        erToCcdFieldsMap.put("D8PetitionerNameDifferentToMarriageCert", "D8PetitionerNameDifferentToMarriageCert");
        erToCcdFieldsMap.put("RespNameDifferentToMarriageCertExplain", "RespNameDifferentToMarriageCertExplain");
        erToCcdFieldsMap.put("D8RespondentEmailAddress", "D8RespondentEmailAddress");
        erToCcdFieldsMap.put("D8RespondentCorrespondenceSendToSol", "D8RespondentCorrespondenceSendToSol");
        erToCcdFieldsMap.put("D8RespondentSolicitorName", "D8RespondentSolicitorName");
        erToCcdFieldsMap.put("D8RespondentSolicitorReference", "D8RespondentSolicitorReference");
        erToCcdFieldsMap.put("D8RespondentSolicitorCompany", "D8RespondentSolicitorCompany");

        // Section 4 - Details of marriage/civil partnership
        erToCcdFieldsMap.put("D8MarriagePetitionerName", "D8MarriagePetitionerName");
        erToCcdFieldsMap.put("D8MarriageRespondentName", "D8MarriageRespondentName");
        erToCcdFieldsMap.put("D8MarriedInUk", "D8MarriedInUk");
        erToCcdFieldsMap.put("D8ApplicationToIssueWithoutCertificate", "D8ApplicationToIssueWithoutCertificate");
        erToCcdFieldsMap.put("D8MarriagePlaceOfMarriage", "D8MarriagePlaceOfMarriage");
        erToCcdFieldsMap.put("D8MarriageCertificateCorrect", "D8MarriageCertificateCorrect");
        erToCcdFieldsMap.put("D8MarriageCertificateCorrectExplain", "D8MarriageCertificateCorrectExplain");

        // Section 6 - Give the reason for your divorce or dissolution (the facts)
        erToCcdFieldsMap.put("D8ReasonForDivorce", "D8ReasonForDivorce");

        // Section 7 - Supporting information (Statement of case)
        erToCcdFieldsMap.put("SeparationLivedTogetherAsCoupleAgain", "SeparationLivedTogetherAsCoupleAgain");
        erToCcdFieldsMap.put("SeparationLivedTogetherAsCoupleAgainDetails", "SeparationLivedTogetherAsCoupleAgainDetails");
        erToCcdFieldsMap.put("D8ReasonForDivorceDetails", "D8ReasonForDivorceDetails");

        // Section 8 - Details of the person your partner committed adultery with (co-respondent)
        erToCcdFieldsMap.put("D8ReasonForDivorceAdultery3rdPartyFName", "D8ReasonForDivorceAdultery3rdPartyFName");
        erToCcdFieldsMap.put("D8ReasonForDivorceAdultery3rdPartyLName", "D8ReasonForDivorceAdultery3rdPartyLName");

        // Section 9 - Existing court cases
        erToCcdFieldsMap.put("D8LegalProceedings", "D8LegalProceedings");
        erToCcdFieldsMap.put("D8LegalProceedingsDetailsCaseNumber", "D8LegalProceedingsDetailsCaseNumber");
        erToCcdFieldsMap.put("D8LegalProceedingsDetails", "D8LegalProceedingsDetails");

        // Section 10 - Dividing your money and property – Orders which are sought
        erToCcdFieldsMap.put("D8FinancialOrder", "D8FinancialOrder");
        erToCcdFieldsMap.put("D8FinancialOrderFor", "D8FinancialOrderFor");

        // Section 11 - Statement of truth
        erToCcdFieldsMap.put("D8AppliesForStatementOfTruth", "D8AppliesForStatementOfTruth");
        erToCcdFieldsMap.put("D8DivorceClaimFrom", "D8DivorceClaimFrom");
        erToCcdFieldsMap.put("D8FinancialOrderStatementOfTruth", "D8FinancialOrderStatementOfTruth");
        erToCcdFieldsMap.put("D8FullNameStatementOfTruth", "D8FullNameStatementOfTruth");
        erToCcdFieldsMap.put("D8StatementofTruthSignature", "D8StatementOfTruthSignature");
        erToCcdFieldsMap.put("D8StatementofTruthDate", "D8StatementOfTruthDate");
        erToCcdFieldsMap.put("D8SolicitorsFirmStatementOfTruth", "D8SolicitorsFirmStatementOfTruth");

        return erToCcdFieldsMap;
    }

    private void applyMappingsForPetitionerHomeAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
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

    private void applyMappingsForPetitionerSolicitorAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
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

    private void applyMappingsForPetitionerCorrespondenceAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
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

    private void applyMappingsForDivorceAdultery3rdPartyAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        addMappingsTo(
            "D8ReasonForDivorceAdultery3rdAddress",
            ImmutableMap.of(
                "D8ReasonForDivorceAdultery3rdPartyAddressStreet", "AddressLine1",
                "D8ReasonForDivorceAdultery3rdPartyCounty", "County",
                "D8ReasonForDivorceAdultery3rdPartyPostCode", "PostCode",
                "D8ReasonForDivorceAdultery3rdPartyTown", "PostTown"
            ),
            modifiedMap,
            ocrDataFields);
    }

    private void applyMappingsForRespondentHomeAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        addMappingsTo(
            "D8RespondentHomeAddress",
            ImmutableMap.of(
                "D8RespondentHomeAddressStreet", "AddressLine1",
                "D8RespondentHomeAddressTown", "PostTown",
                "D8RespondentPostcode", "PostCode",
                "D8RespondentHomeAddressCounty", "County"
            ),
            modifiedMap,
            ocrDataFields);
    }

    private void applyMappingsForRespondentSolicitorAddress(List<OcrDataField> ocrDataFields, Map<String, Object> modifiedMap) {
        addMappingsTo(
            "D8RespondentSolicitorAddress",
            ImmutableMap.of(
                "D8RespondentSolicitorAddressStreet", "AddressLine1",
                "D8RespondentSolicitorAddressTown", "PostTown",
                "D8RespondentSolicitorAddressPostCode", "PostCode",
                "D8RespondentSolicitorAddressCounty", "County"
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
