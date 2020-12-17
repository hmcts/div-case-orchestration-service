package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CARE_OF_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PHONE_LABEL;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourtLookupServiceTest {

    @Autowired
    private CourtLookupService courtLookupService;

    @Test
    public void testCourtAddressWithPOBoxIsFormattedCorrectly() throws CourtDetailsNotFound {
        Court foundCourt = courtLookupService.getCourtByKey("eastMidlands");

        assertThat(foundCourt.getCourtId(), is("eastMidlands"));
        assertThat(foundCourt.getIdentifiableCentreName(), is("East Midlands Regional Divorce Centre"));
        String expectedAddress = "East Midlands Regional Divorce Centre" + LINE_SEPARATOR
            + "PO Box 10447" + LINE_SEPARATOR
            + "Nottingham" + LINE_SEPARATOR
            + "NG2 9QN";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testCourtAddressWithoutPOBoxIsFormattedCorrectly() throws CourtDetailsNotFound {
        Court foundCourt = courtLookupService.getCourtByKey("northWest");

        assertThat(foundCourt.getCourtId(), is("northWest"));
        assertThat(foundCourt.getIdentifiableCentreName(), is("North West Regional Divorce Centre"));
        String expectedAddress = "North West Regional Divorce Centre" + LINE_SEPARATOR
            + "Liverpool Civil & Family Court" + LINE_SEPARATOR
            + "35 Vernon Street" + LINE_SEPARATOR
            + "Liverpool" + LINE_SEPARATOR
            + "L2 2BX";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testServiceCentreAddressFormattedCorrectly() throws CourtDetailsNotFound {
        Court foundCourt = courtLookupService.getCourtByKey("serviceCentre");

        assertThat(foundCourt.getCourtId(), is("serviceCentre"));
        assertThat(foundCourt.getIdentifiableCentreName(), is("Courts and Tribunals Service Centre"));
        String expectedAddress = "Courts and Tribunals Service Centre" + LINE_SEPARATOR
            + "c/o HMCTS Digital Divorce" + LINE_SEPARATOR
            + "PO Box 12706" + LINE_SEPARATOR
            + "Harlow" + LINE_SEPARATOR
            + "CM20 9QT";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testExceptionInThrownWhenCourtIsNotFound() {
        CourtDetailsNotFound courtDetailsNotFound = assertThrows(
            CourtDetailsNotFound.class, () -> courtLookupService.getCourtByKey("unknownCourt")
        );
        assertThat(courtDetailsNotFound.getMessage(), is("Could not find court by using key \"unknownCourt\""));
    }

    @Test
    public void testDnCourtIsReturnedWithCorrectDetails() throws CourtDetailsNotFound {
        DnCourt foundCourt = courtLookupService.getDnCourtByKey("liverpool");

        assertEquals("Liverpool Civil and Family Court Hearing Centre", foundCourt.getName());
        assertEquals("35 Vernon Street\nLiverpool\nL2 2BX", foundCourt.getAddress());
        assertEquals("divorcecase@justice.gov.uk", foundCourt.getEmail());
        assertEquals("0300 303 0642", foundCourt.getPhone());

        String expectedContactDetails = CARE_OF_PREFIX + SPACE + foundCourt.getName() + LINE_SEPARATOR
            + foundCourt.getAddress() + LINE_SEPARATOR + LINE_SEPARATOR
            + EMAIL_LABEL + SPACE + foundCourt.getEmail() + LINE_SEPARATOR
            + PHONE_LABEL + SPACE + foundCourt.getPhone();
        assertEquals(foundCourt.getFormattedContactDetails(), expectedContactDetails);
    }

    @Test
    public void testExceptionInThrownWhenDnCourtIsNotFound() {
        CourtDetailsNotFound courtDetailsNotFound = assertThrows(
            CourtDetailsNotFound.class, () -> courtLookupService.getDnCourtByKey("unknownCourt")
        );
        assertThat(courtDetailsNotFound.getMessage(), is("Could not find court by using key \"unknownCourt\""));
    }

    @Test
    public void shouldReturnAllCourts() {
        Map<String, Court> allCourts = courtLookupService.getAllCourts();
        assertThat(allCourts.size(), is(5));
    }
}
