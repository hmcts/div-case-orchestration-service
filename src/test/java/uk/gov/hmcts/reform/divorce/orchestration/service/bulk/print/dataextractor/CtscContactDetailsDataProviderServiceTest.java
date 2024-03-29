package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CtscContactDetailsDataProviderServiceTest {

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Test
    public void getCtscContactDetailsReturnsPopulatedModel() {
        CtscContactDetails details = ctscContactDetailsDataProviderService.getCtscContactDetails();

        assertThat(details.getServiceCentre(), is("Courts and Tribunals Service Centre"));
        assertThat(details.getCareOf(), is("c/o HMCTS Digital Divorce"));
        assertThat(details.getPoBox(), is("PO Box 12706"));
        assertThat(details.getPostcode(), is("CM20 9QT"));
        assertThat(details.getTown(), is("Harlow"));
        assertThat(details.getEmailAddress(), is("divorcecase@justice.gov.uk"));
        assertThat(details.getPhoneNumber(), is("0300 303 0642"));
        assertThat(details.getOpeningHours(), is("8am to 6pm, Monday to Friday"));
    }
}
