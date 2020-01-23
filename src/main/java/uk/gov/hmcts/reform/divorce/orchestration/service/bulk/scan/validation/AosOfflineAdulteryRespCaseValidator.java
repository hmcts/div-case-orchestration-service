package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class AosOfflineAdulteryRespCaseValidator extends AosFormValidator {

    // TODO check RespAgreeToCosts and Reason are common
    private static final List<String> MANDATORY_FIELDS = asList(
        "RespAOSAdultery",
        "RespAgreeToCosts"
    );

    private static final Map<String, List<String>> ALLOWED_VALUES_PER_FIELD = new HashMap<>();

    static {
        ALLOWED_VALUES_PER_FIELD.put("AOSReasonForDivorce", singletonList("Adultery"));
        ALLOWED_VALUES_PER_FIELD.put("RespConfirmReadPetition", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespAOSAdultery", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespWillDefendDivorce", asList("Proceed", "Defend"));
        ALLOWED_VALUES_PER_FIELD.put("RespJurisdictionAgree", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespLegalProceedingsExist", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespAgreeToCosts", asList(YES_VALUE, NO_VALUE));
        ALLOWED_VALUES_PER_FIELD.put("RespStatementOfTruth", asList(YES_VALUE, EMPTY_STRING));
    }

    @Override
    protected Map<String, List<String>> getAllowedValuesPerField() {
        return ALLOWED_VALUES_PER_FIELD;
    }

    @Override
    protected List<String> getAosFormSpecificFieldValidation(Map<String, String> fieldsMap) {
        return emptyList();
    }

    @Override
    protected List<String> getAosOfflineSpecificMandatoryFields() {
        return MANDATORY_FIELDS;
    }
}
