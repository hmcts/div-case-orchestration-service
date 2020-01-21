package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AosOffline5YrSepFormToCaseTransformer extends BulkScanFormTransformer {

    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = aosPackOfflineExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    @Override
    Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields) {

        // returning an empty map as we currently have no formSpecificTransformation for AOS Pack 2
        return Collections.emptyMap();
    }

    @Override
    Map<String, Object> runPostMappingModification(Map<String, Object> transformedCaseData) {

        return transformedCaseData;
    }

    private Optional<String> getValueFromOcrDataFields(String fieldName, List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(f -> f.getName().equals(fieldName))
            .map(OcrDataField::getValue)
            .findFirst();
    }

    private static Map<String, String> aosPackOfflineExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("RespConfirmReadPetition", "RespConfirmReadPetition");
        erToCcdFieldsMap.put("DateRespReceivedDivorceApplication", "DateRespReceivedDivorceApplication");
        erToCcdFieldsMap.put("RespHardshipDefenseResponse", "RespHardshipDefenseResponse");
        erToCcdFieldsMap.put("RespWillDefendDivorce", "RespWillDefendDivorce");
        erToCcdFieldsMap.put("RespConsiderFinancialSituation", "RespConsiderFinancialSituation");
        erToCcdFieldsMap.put("RespJurisdictionAgree", "RespJurisdictionAgree");
        erToCcdFieldsMap.put("RespJurisdictionDisagreeReason", "RespJurisdictionDisagreeReason");
        erToCcdFieldsMap.put("RespLegalProceedingsExist", "RespLegalProceedingsExist");
        erToCcdFieldsMap.put("RespLegalProceedingsDescription", "RespLegalProceedingsDescription");
        erToCcdFieldsMap.put("RespAgreeToCosts", "RespAgreeToCosts");
        erToCcdFieldsMap.put("RespCostsReason", "RespCostsReason");
        erToCcdFieldsMap.put("RespStatementOfTruth", "RespStatementOfTruth");
        erToCcdFieldsMap.put("RespStatementofTruthSignedDate", "RespStatementofTruthSignedDate");

        return erToCcdFieldsMap;
    }
}