package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class D8FormToCaseTransformer implements ExceptionRecordToCaseTransformer {

    private static Map<String, String> ocrToCCDMapping;

    public D8FormToCaseTransformer() {
        ocrToCCDMapping = d8ExceptionRecordToCcdMap();
    }

    private static Map<String, String> d8ExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        // Help With Fees
        erToCcdFieldsMap.put("D8HelpWithFeesReferenceNumber", "D8HelpWithFeesReferenceNumber");
        erToCcdFieldsMap.put("D8PaymentMethod", "D8PaymentMethod");

        // Section 1 - Your application (known as a petition in divorce and judicial separation)
        erToCcdFieldsMap.put("D8legalProcess", "D8legalProcess");
        erToCcdFieldsMap.put("D8ScreenHasMarriageCert", "D8ScreenHasMarriageCert");
        erToCcdFieldsMap.put("D8CertificateInEnglish", "D8CertificateInEnglish");

        // Section 2 - About you (the applicant/petitioner)
        erToCcdFieldsMap.put("PetitionerFirstName", "D8FirstName");
        erToCcdFieldsMap.put("PetitionerLastName", "D8LastName");
        erToCcdFieldsMap.put("email", "D8PetitionerEmail");

        // Section 3 - About your spouse/civil partner (the respondent)
        erToCcdFieldsMap.put("D8RespondentFirstName", "D8RespondentFirstName");
        erToCcdFieldsMap.put("D8RespondentLastName", "D8RespondentLastName");
        erToCcdFieldsMap.put("D8RespondentPhoneNumber", "D8RespondentPhoneNumber");

        return erToCcdFieldsMap;
    }

    private Map<String, Object> mapOcrFieldsToCcdFields(ExceptionRecord exceptionRecord) {
        return exceptionRecord.getOcrDataFields().stream()
            .collect(Collectors.toMap(
                ocrDataField -> ocrToCCDMapping.get(ocrDataField.getName()),
                OcrDataField::getValue
            ));
    }

    @Override
    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) {

        Map<String, Object> ccdTransformedFields = mapOcrFieldsToCcdFields(exceptionRecord);
        ccdTransformedFields.replace("D8PaymentMethod", "debit/credit card", "card");

        return ccdTransformedFields;
    }
}