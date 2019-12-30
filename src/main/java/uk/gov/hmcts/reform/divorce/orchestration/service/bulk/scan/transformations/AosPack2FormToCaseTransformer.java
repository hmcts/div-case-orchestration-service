package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AosPack2FormToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = aosPack2ExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {
        return null;
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {

        // will delete this line
        // transformedCaseData.replace("D8PaymentMethod", "Debit/Credit Card", "Card");

        return transformedCaseData;
    }

    private Optional<String> getValueFromOcrDataFields(String fieldName, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .map(OcrDataField::getValue)
            .findFirst();
    }

    private static Map<String, String> aosPack2ExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("RespConfirmReadPetition", "RespConfirmReadPetition");
        erToCcdFieldsMap.put("DateRespReceivedDivorceApplication", "DateRespReceivedDivorceApplication");
        erToCcdFieldsMap.put("RespAOS2yrConsent", "RespAOS2yrConsent");
        erToCcdFieldsMap.put("RespWillDefendDivorce", "RespWillDefendDivorce");
        erToCcdFieldsMap.put("RespConsiderFinancialSituation", "RespConsiderFinancialSituation");
        erToCcdFieldsMap.put("RespJurisdictionAgree", "RespJurisdictionAgree");
        erToCcdFieldsMap.put("RespJurisdictionDisagreeReason", "RespJurisdictionDisagreeReason");
        erToCcdFieldsMap.put("RespLegalProceedingsExist", "RespLegalProceedingsExist");
        erToCcdFieldsMap.put("RespLegalProceedingsDescription", "RespLegalProceedingsDescription");
        erToCcdFieldsMap.put("RespAgreeToCosts", "RespAgreeToCosts");
        erToCcdFieldsMap.put("RespCostsReason", "RespCostsReason");
        erToCcdFieldsMap.put("RespStatementOfTruth", "RespStatementOfTruth");
        // TODO: Create this new field in CCD
        erToCcdFieldsMap.put("RespStatementofTruthSignedDate", "RespStatementofTruthSignedDate");

        return erToCcdFieldsMap;
    }
}