package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.util.Constants.LINE_SEPARATOR;

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

        assertThat(foundCourt.getDivorceCentreName(), is("East Midlands Regional Divorce Centre"));
        String expectedAddress = "East Midlands Regional Divorce Centre" + LINE_SEPARATOR +
                "PO Box 10447" + LINE_SEPARATOR +
                "Nottingham" + LINE_SEPARATOR +
                "NG2 9QN";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testCourtAddressWithoutPOBoxIsFormattedCorrectly() throws CourtDetailsNotFound {
        Court foundCourt = courtLookupService.getCourtByKey("northWest");

        assertThat(foundCourt.getDivorceCentreName(), is("North West Regional Divorce Centre"));
        String expectedAddress = "North West Regional Divorce Centre" + LINE_SEPARATOR +
                "Liverpool Civil & Family Court" + LINE_SEPARATOR +
                "35 Vernon Street" + LINE_SEPARATOR +
                "Liverpool" + LINE_SEPARATOR +
                "L2 2BX";
        assertThat(foundCourt.getFormattedAddress(), is(expectedAddress));
    }

    @Test
    public void testExceptionInThrownWhenCourtIsNotFound() throws CourtDetailsNotFound {
        expectedException.expect(CourtDetailsNotFound.class);
        expectedException.expectMessage("Could not find court by using key \"unknownCourt\"");

        courtLookupService.getCourtByKey("unknownCourt");
    }

}