package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.divorce.orchestration.util.Constants.LINE_SEPARATOR;

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
            formatAddressAccordingly();
        }

        return formattedAddress;
    }

    private void formatAddressAccordingly() {
        if (isPOBoxAddress()) {
            formattedAddress = formatAddressWithPOBox();
        } else {
            formattedAddress = formatAddressWithoutPOBox();
        }
    }

    private boolean isPOBoxAddress() {
        return poBox != null;
    }

    private String formatAddressWithPOBox() {
        return newArrayList(divorceCentreName,
                poBox,
                courtCity,
                postCode)
                .stream()
                .collect(joining(LINE_SEPARATOR));
    }

    private String formatAddressWithoutPOBox() {
        return newArrayList(divorceCentreName,
                divorceCentreAddressName,
                street,
                courtCity,
                postCode)
                .stream()
                .collect(joining(LINE_SEPARATOR));
    }

}