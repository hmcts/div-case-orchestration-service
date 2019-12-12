package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.helper;

import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.format.ResolverStyle.STRICT;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.formatDateForCCD;

public class BulkScanHelper {

    private static final DateTimeFormatter EXPECTED_DATE_FORMAT_FROM_FORM = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(STRICT);

    /**
     * Returns map with only the fields that were not blank from the OCR data.
     */
    public static Map<String, String> produceMapWithoutEmptyEntries(List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

    public static LocalDate transformFormDateIntoLocalDate(String formFieldName, String formDate) throws FormFieldValidationException {
        try {
            return LocalDate.parse(formDate, EXPECTED_DATE_FORMAT_FROM_FORM);
        } catch (DateTimeParseException exception) {
            throw new FormFieldValidationException(String.format("%s must be a valid date", formFieldName));
        }
    }

    public static String transformDateFromComponentsToCcdDate(String dayValue, String monthValue, String yearValue)
            throws FormFieldValidationException {
        int dayParsed = Integer.parseInt(dayValue);
        int monthParsed = Integer.parseInt(monthValue);
        int yearParsed = Integer.parseInt(yearValue);
        try {
            LocalDate date = LocalDate.of(yearParsed, Month.of(monthParsed), dayParsed);
            return formatDateForCCD(date);
        } catch (DateTimeException exception) {
            throw new FormFieldValidationException(String.format("Cannot form a valid date from %s, %s, %s", dayValue, monthValue, yearValue));
        }
    }

    public static List<String> validateDateComponents(Map<String, String> fieldsMap,
                                                      String dayKey, String monthKey, String yearKey) {

        List<String> validationWarningMessages = new ArrayList<>();

        String dayValue = fieldsMap.get(dayKey);
        String monthValue = fieldsMap.get(monthKey);
        String yearValue = fieldsMap.get(yearKey);

        boolean allDateComponentsParsable = Stream.of(dayValue, monthValue, yearValue)
            .map(NumberUtils::isParsable)
            .reduce(Boolean::logicalAnd)
            .orElse(false);

        if (!allDateComponentsParsable) {
            return Collections.singletonList(
                String.format("One or more of %s, %s, %s contain invalid characters that can't be converted into a date",
                    dayKey,
                    monthKey,
                    yearKey));
        }

        int dayParsed = Integer.parseInt(dayValue);
        int monthParsed = Integer.parseInt(monthValue);
        int yearParsed = Integer.parseInt(yearValue);

        if (dayParsed < 1 || dayParsed > 31) {
            validationWarningMessages.add(String.format("%s is invalid", dayKey));
        }

        if (monthParsed < 1 || monthParsed > 12) {
            validationWarningMessages.add(String.format("%s is not a valid month e.g. 03 for March", monthKey));
        }

        if (yearParsed < 1900) {
            validationWarningMessages.add(String.format("%s needs to be 4 digits e.g. 2011", yearKey));
        }

        try {
            LocalDate parsedDateForFurtherValidation = LocalDate.of(yearParsed, Month.of(monthParsed), dayParsed);
        } catch (DateTimeException exception) {
            validationWarningMessages.add(
                String.format("Invalid date made up of %s, %s, %s", dayKey, monthKey, yearKey));
        }

        return validationWarningMessages;
    }

}