package uk.gov.hmcts.reform.divorce.orchestration.util;

import uk.gov.hmcts.reform.divorce.orchestration.exception.InvalidPropertyException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class JsonPayloadUtils {

    public static boolean getBooleanFromPayloadField(final Map<String, Object> payload, final String keyValue)
            throws InvalidPropertyException {
        if (!payload.containsKey(keyValue)) {
            throw new InvalidPropertyException(keyValue);
        }

        String stringValue = (String) payload.get(keyValue);
        if (YES_VALUE.equalsIgnoreCase(stringValue)) {
            return true;
        } else if (NO_VALUE.equalsIgnoreCase(stringValue)) {
            return false;
        } else {
            throw new InvalidPropertyException(keyValue);
        }
    }

}