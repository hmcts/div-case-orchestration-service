package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
public class CoEBasicCoverLetter extends BasicCoverLetter {

    @JsonProperty("costClaimGranted")
    private boolean costClaimGranted;

    @JsonProperty("hearingDate")
    private String hearingDate;

    @JsonProperty("deadlineToContactCourtBy")
    private String deadlineToContactCourtBy;

}
