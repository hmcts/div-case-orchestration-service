package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AosOfflineAdulteryFormToCaseTransformer extends BulkScanFormTransformer {
    private static final Map<String, String> ocrToCCDMapping;

    static {
        ocrToCCDMapping = aosPackOfflineExceptionRecordToCcdMap();
    }

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return ocrToCCDMapping;
    }

    private static Map<String, String> aosPackOfflineExceptionRecordToCcdMap() {
        Map<String, String> erToCcdFieldsMap = new HashMap<>();

        erToCcdFieldsMap.put("RespConfirmReadPetition", "RespConfirmReadPetition");
        erToCcdFieldsMap.put("DateRespReceivedDivorceApplication", "DateRespReceivedDivorceApplication");
        erToCcdFieldsMap.put("RespAOSAdultery", "RespAOSAdultery");
        erToCcdFieldsMap.put("RespWillDefendDivorce", "RespWillDefendDivorce");
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
