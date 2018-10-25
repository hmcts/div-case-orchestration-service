package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Court {

    @JsonProperty("divorceCentre")
    @Setter
    private String divorceCentreName;

    @JsonProperty("divorceCentreAddressName")
    private String divorceCentreAddressName;

    @JsonProperty("street")
    private String street;

    @JsonProperty("poBox")
    @Setter
    private String poBox;

    @JsonProperty("courtCity")
    @Setter
    private String courtCity;

    @JsonProperty("postCode")
    @Setter
    private String postCode;

    private String formattedAddress;

    public String getFormattedAddress() {
        if (formattedAddress == null) {
            formattedAddress = formatAddress();
        }

        return formattedAddress;
    }

    private String formatAddress() {
        List<String> addressLines;

        if (isPOBoxAddress()) {
            addressLines = getAddressLinesWithPOBox();
        } else {
            addressLines = getAddressLinesWithoutPOBox();
        }

        return addressLines.stream().collect(joining(LINE_SEPARATOR));
    }

    private boolean isPOBoxAddress() {
        return poBox != null;
    }

    private List<String> getAddressLinesWithPOBox() {
        return newArrayList(divorceCentreName,
                poBox,
                courtCity,
                postCode);
    }

    private List<String> getAddressLinesWithoutPOBox() {
        return newArrayList(divorceCentreName,
                divorceCentreAddressName,
                street,
                courtCity,
                postCode);
    }

}