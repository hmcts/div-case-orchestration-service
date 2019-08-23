package uk.gov.hmcts.reform.divorce.orchestration.domain.rules.d8;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CoreCaseData;

import java.util.List;
import java.util.Optional;

@Rule(order = 21)
@Data
public class D8ReasonForDivorceSeperationDate {

    private static final String BLANK_SPACE = " ";
    private static final String REASON_SEPARATION_2_YEARS = "separation-2-years";
    private static final String REASON_SEPARATION_5_YEARS = "separation-5-years";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE = "D8ReasonForDivorceSeperationDate can not be null or empty.";

    @Result
    public List<String> result;

    @Given("coreCaseData")
    public CoreCaseData coreCaseData = new CoreCaseData();

    @When
    public boolean when() {
        return (Optional.ofNullable(coreCaseData.getD8ReasonForDivorce()).orElse("")
                .equalsIgnoreCase(REASON_SEPARATION_2_YEARS)
            || Optional.ofNullable(coreCaseData.getD8ReasonForDivorce()).orElse("")
                .equalsIgnoreCase(REASON_SEPARATION_5_YEARS))
            && !Optional.ofNullable(coreCaseData.getD8ReasonForDivorceSeperationDate()).isPresent();
    }

    @Then
    public void then() {
        result.add(String.join(
            BLANK_SPACE, // delimiter
            ERROR_MESSAGE,
            String.format(ACTUAL_DATA, coreCaseData.getD8ReasonForDivorceSeperationDate())
        ));
    }
}