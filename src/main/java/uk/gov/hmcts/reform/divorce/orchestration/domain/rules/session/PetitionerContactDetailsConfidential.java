package uk.gov.hmcts.reform.divorce.orchestration.domain.rules.session;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.DivorceSession;

import java.util.List;
import java.util.Optional;

@Rule(order = 8)
@Data
public class PetitionerContactDetailsConfidential {

    private static final String BLANK_SPACE = " ";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE = "petitionerContactDetailsConfidential can not be null or empty.";

    @Result
    public List<String> result;

    @Given("divorceSession")
    public DivorceSession divorceSession = new DivorceSession();

    @When
    public boolean when() {
        return !Optional.ofNullable(divorceSession.getPetitionerContactDetailsConfidential()).isPresent();
    }

    @Then
    public void then() {
        result.add(String.join(
            BLANK_SPACE, // delimiter
            ERROR_MESSAGE,
            String.format(ACTUAL_DATA, divorceSession.getPetitionerContactDetailsConfidential())
        ));
    }
}