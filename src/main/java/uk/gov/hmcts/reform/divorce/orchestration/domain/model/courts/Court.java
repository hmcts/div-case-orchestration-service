package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Court {

    private static final String CARE_OF_PREFIX = "c/o ";

    @JsonProperty("serviceCentreName")
    @Setter
    private String serviceCentreName;

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
        List<String> addressLines = new ArrayList<>();

        if (isServiceCentre()) {
            addressLines.add(serviceCentreName);
        }

        if (isPOBoxAddress()) {
            addressLines.addAll(getAddressLinesWithPOBox());
        } else {
            addressLines.addAll(getAddressLinesWithoutPOBox());
        }

        return addressLines.stream().collect(joining(LINE_SEPARATOR));
    }

    private boolean isPOBoxAddress() {
        return poBox != null;
    }

    private boolean isServiceCentre() {
        return serviceCentreName != null;
    }

    private List<String> getAddressLinesWithPOBox() {
        String divorceCentreNameFormattedForAddress = getDivorceCentreNameFormattedForAddress();

        return newArrayList(divorceCentreNameFormattedForAddress,
                poBox,
                courtCity,
                postCode);
    }

    private List<String> getAddressLinesWithoutPOBox() {
        String divorceCentreNameFormattedForAddress = getDivorceCentreNameFormattedForAddress();

        return newArrayList(divorceCentreNameFormattedForAddress,
                divorceCentreAddressName,
                street,
                courtCity,
                postCode);
    }

    private String getDivorceCentreNameFormattedForAddress() {
        StringBuffer stringBuffer = new StringBuffer();

        if (isServiceCentre()) {
            stringBuffer.append(CARE_OF_PREFIX);
        }
        stringBuffer.append(divorceCentreName);

        return stringBuffer.toString();
    }

}