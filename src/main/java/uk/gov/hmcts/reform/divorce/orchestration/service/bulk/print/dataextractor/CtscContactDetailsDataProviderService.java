package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Component
public class CtscContactDetailsDataProviderService {

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.divorceCentreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.courtCity}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.email}")
    private String emailAddress;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Value("${court.locations.serviceCentre.openingHours}")
    private String openingHours;

    public CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails.builder()
            .serviceCentre(serviceCentre)
            .centreName(centreName)
            .careOf("c/o " + centreName)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .emailAddress(emailAddress)
            .phoneNumber(phoneNumber)
            .openingHours(openingHours)
            .build();
    }
}
