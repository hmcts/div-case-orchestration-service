package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DxAddressResponse {

    @JsonProperty
    private String dxNumber;
    @JsonProperty
    private String dxExchange;

}

