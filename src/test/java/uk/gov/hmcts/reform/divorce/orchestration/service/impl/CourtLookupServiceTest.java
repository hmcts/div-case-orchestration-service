package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourtLookupServiceTest {

    @Autowired
    private CourtLookupService courtLookupService;

    @Rule
    public ExpectedException expectedException = none();

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
                + "c/o East Midlands Regional Divorce Centre" + LINE_SEPARATOR
                + "PO Box 10447" + LINE_SEPARATOR
                + "Nottingham" + LINE_SEPARATOR
                + "NG2 9QN";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testExceptionInThrownWhenCourtIsNotFound() throws CourtDetailsNotFound {
        expectedException.expect(CourtDetailsNotFound.class);
        expectedException.expectMessage("Could not find court by using key \"unknownCourt\"");

        courtLookupService.getCourtByKey("unknownCourt");
    }

    @Test
    public void testDnCourtIsReturnedWithCorrectDetails() throws CourtDetailsNotFound {
        DnCourt foundCourt = courtLookupService.getDnCourtByKey("liverpool");

        assertEquals(foundCourt.getName(), "Liverpool Civil and Family Court Hearing Centre");
        assertEquals(foundCourt.getAddress(), "35 Vernon Street\nLiverpool\nL2 2BX");
        assertEquals(foundCourt.getEmail(), "contactdivorce@justice.gov.uk");
        assertEquals(foundCourt.getPhone(), "0300 303 0642");

        String expectedContactDetails = foundCourt.getAddress() + LINE_SEPARATOR
                + foundCourt.getEmail() + LINE_SEPARATOR
                + foundCourt.getPhone();
        assertEquals(foundCourt.getFormattedContactDetails(), expectedContactDetails);
    }

    @Test
    public void testExceptionInThrownWhenDnCourtIsNotFound() throws CourtDetailsNotFound {
        expectedException.expect(CourtDetailsNotFound.class);
        expectedException.expectMessage("Could not find court by using key \"unknownCourt\"");

        courtLookupService.getDnCourtByKey("unknownCourt");
    }
}