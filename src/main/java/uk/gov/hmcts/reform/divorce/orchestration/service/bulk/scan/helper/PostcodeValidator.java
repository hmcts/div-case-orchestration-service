package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostcodeValidator {

    public static List<String> validatePostcode(Map<String, String> fieldsMap, String postcodeKey) {
        List<String> validationMessages = new ArrayList<>();
        if (fieldsMap.containsKey(postcodeKey)) {
            String postcodeValue = fieldsMap.get(postcodeKey);
            if (isPostcodeLengthValid(postcodeValue)) {
                validationMessages.add(postcodeKey + " is usually 6 or 7 characters long");
            }
        }
        return validationMessages;
    }

    private static boolean isPostcodeLengthValid(String postcode) {
        int postcodeLength = postcode.length();
        return postcodeLength < 6 || postcodeLength > 8;
    }
}
