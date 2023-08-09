package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.mapper.config.DataFormatterConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
    "DOCUMENT_MANAGEMENT_STORE_URL=http://customUrl"
})
public class CaseDataFormatterConfigurationTest {

    @Autowired
    private DataFormatterConfiguration dataFormatterConfiguration;

    @Test
    public void shouldHaveServiceValuesReturningFromConfigurationMethods() {
        assertThat(dataFormatterConfiguration.getCohort(), is("onlineSubmissionPrivateBeta"));
        assertThat(dataFormatterConfiguration.getDocumentManagementStoreUrl(), is("http://customUrl"));
    }

}
