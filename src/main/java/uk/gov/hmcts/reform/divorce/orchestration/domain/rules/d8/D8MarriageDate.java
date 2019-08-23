package uk.gov.hmcts.reform.divorce.orchestration.domain.rules.d8;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Rule(order = 4)
@Data
public class D8MarriageDate {

    private static final String BLANK_SPACE = " ";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE_NULL = "D8MarriageDate can not be null or empty.";
    private static final String ERROR_MESSAGE_LESS_THAN_ONE_YEAR_AGO =
            "D8MarriageDate can not be less than one year ago.";
    private static final String ERROR_MESSAGE_MORE_THAN_ONE_HUNDRED_YEARS_AGO =
            "D8MarriageDate can not be more than 100 years ago.";
    private static final String ERROR_MESSAGE_IN_THE_FUTURE = "D8MarriageDate can not be in the future.";

    @Result
    public List<String> result;

    @Given("coreCaseData")
    public CoreCaseData coreCaseData = new CoreCaseData();

    @When
    public boolean when() {
        String marriageDate = coreCaseData.getD8MarriageDate();
        return isNull()
                || isLessThanOneYearAgo(marriageDate)
                || isOverOneHundredYearsAgo(marriageDate)
                || isInTheFuture(marriageDate);
    }

    @Then
    public void then() {
        String errorMessage = deriveErrorMessage();

        result.add(String.join(
            BLANK_SPACE, // delimiter
            errorMessage,
            String.format(ACTUAL_DATA, coreCaseData.getD8MarriageDate())
        ));
    }

    private boolean isNull() {
        return !Optional.ofNullable(coreCaseData.getD8MarriageDate()).isPresent();
    }

    private boolean isLessThanOneYearAgo(String date) {
        return !DateUtils.parseToInstant(date).isAfter(Instant.now())
            && DateUtils.parseToInstant(date).isAfter(Instant.now().minus(365, ChronoUnit.DAYS));
    }

    private boolean isOverOneHundredYearsAgo(String date) {
        return DateUtils.parseToInstant(date).isBefore(Instant.now().minus(365 * 100, ChronoUnit.DAYS));
    }

    private boolean isInTheFuture(String date) {
        return DateUtils.parseToInstant(date).isAfter(Instant.now());
    }

    private String deriveErrorMessage() {
        String marriageDate = coreCaseData.getD8MarriageDate();
        return isNull()
            ? ERROR_MESSAGE_NULL
            : Stream.of(
                isLessThanOneYearAgo(marriageDate) ? ERROR_MESSAGE_LESS_THAN_ONE_YEAR_AGO : "",
                isOverOneHundredYearsAgo(marriageDate) ? ERROR_MESSAGE_MORE_THAN_ONE_HUNDRED_YEARS_AGO : "",
                isInTheFuture(marriageDate) ? ERROR_MESSAGE_IN_THE_FUTURE : ""
            ).filter(string -> !string.isEmpty()).collect(Collectors.joining(BLANK_SPACE));
    }
}