package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class CoERespondentCoverLetter extends CoECoverLetter {

    @JsonProperty("husbandOrWife")
    private String husbandOrWife;

}
