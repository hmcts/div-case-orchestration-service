package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("siteId")
    @Setter
    @Getter
    private String siteId;

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

        addressLines.add(getDivorceCentreNameFormattedForAddress());

        if (isPOBoxAddress()) {
            addressLines.add(poBox);
            addressLines.add(courtCity);
            addressLines.add(postCode);
        } else {
            addressLines.add(divorceCentreAddressName);
            addressLines.add(street);
            addressLines.add(courtCity);
            addressLines.add(postCode);
        }

        return addressLines.stream().collect(joining(LINE_SEPARATOR));
    }

    private boolean isPOBoxAddress() {
        return poBox != null;
    }

    private boolean isServiceCentre() {
        //serviceCentreName is a field in the json. If it's null, it means the court is not a service centre
        return serviceCentreName != null;
    }

    private String getDivorceCentreNameFormattedForAddress() {
        StringBuffer stringBuffer = new StringBuffer();

        if (isServiceCentre()) {
            stringBuffer.append(CARE_OF_PREFIX);
        }
        stringBuffer.append(divorceCentreName);

        return stringBuffer.toString();
    }

    public String getIdentifiableCentreName() {
        String identifiableCentreName;

        if (isServiceCentre()) {
            identifiableCentreName = serviceCentreName;
        } else {
            identifiableCentreName = divorceCentreName;
        }

        return identifiableCentreName;
    }

}