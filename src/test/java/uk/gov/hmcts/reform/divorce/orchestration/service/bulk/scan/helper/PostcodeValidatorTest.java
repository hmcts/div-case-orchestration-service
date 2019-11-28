package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper.PostcodeValidator.validatePostcode;

public class PostcodeValidatorTest {

    @Test
    public void shouldNotProduceErrorMessagesWhenPostcodeIsValid() {
        String[] validPostcodes = {"SW15 5PU", "M1 1AA", "B151TT", "SE279TU", "L1 0AP"};
        for (String validPostcode : validPostcodes) {
            Map<String, String> validPostcodeFieldMap = new HashMap<>();
            String d8PetitionerPostcodeKey = "D8PetitionerPostcode";
            validPostcodeFieldMap.put(d8PetitionerPostcodeKey, validPostcode);

            List<String> actualValidationMessages = validatePostcode(validPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, is(emptyList()));
        }
    }

    @Test
    public void shouldFailPostcodesWithLessThan6Characters() {
        String[] invalidPostcodes = {"SW15P", "M11 A", "B1", "SE4", "CV30A", "BT7"};

        for (String invalidPostcode : invalidPostcodes) {
            Map<String, String> invalidPostcodeFieldMap = new HashMap<>();
            String d8PetitionerPostcodeKey = "D8PetitionerPostcode";
            invalidPostcodeFieldMap.put(d8PetitionerPostcodeKey, invalidPostcode);

            List<String> actualValidationMessages = validatePostcode(invalidPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, hasItem(d8PetitionerPostcodeKey + " is usually 6 or 7 characters long"));
        }
    }

    @Test
    public void shouldFailPostcodesWithMoreThan8Characters() {
        String[] invalidPostcodes = {"SW15 1PXX", "M11 12HEQ", "BT12  8TR", "BIRMINGHAM", "LT1REEEEE"};

        for (String invalidPostcode : invalidPostcodes) {
            Map<String, String> invalidPostcodeFieldMap = new HashMap<>();
            String d8PetitionerPostcodeKey = "D8PetitionerPostcode";
            invalidPostcodeFieldMap.put(d8PetitionerPostcodeKey, invalidPostcode);

            List<String> actualValidationMessages = validatePostcode(invalidPostcodeFieldMap, d8PetitionerPostcodeKey);

            assertThat(actualValidationMessages, hasItem(d8PetitionerPostcodeKey + " is usually 6 or 7 characters long"));
        }
    }
}
