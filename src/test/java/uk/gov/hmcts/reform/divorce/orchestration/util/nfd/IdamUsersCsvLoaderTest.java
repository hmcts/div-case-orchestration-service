package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IdamUsersCsvLoaderTest {

    IdamUsersCsvLoader idamUsersCsvLoader = new IdamUsersCsvLoader();

    @Test
    public void shouldLoadIdamUsersCsvFile() {
        List<IdamUser> idamUsers = idamUsersCsvLoader.loadIdamUserList("idam-ids-test.csv");

        assertThat(idamUsers.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(idamUsers.size(), equalTo(5));
    }
}