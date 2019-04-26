package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PopulateDivorceCostOptionsTest {

    @InjectMocks
    private PopulateDivorceCostOptions populateMiniPetitionUrl;

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSetsCostOptions() throws Exception {
        Map<String, Object> payload = ObjectMapperTestUtil.getJsonFromResourceFile(
                "/jsonExamples/payloads/sol-dn-costs-resp-agree.json",
                Map.class
        );
        Map<String, Object> expectedPayload = ObjectMapperTestUtil.getJsonFromResourceFile(
                "/jsonExamples/payloads/sol-dn-costs-resp-agree-expected-options.json",
                Map.class
        );

        Map<String, Object> result = populateMiniPetitionUrl.execute(null, payload);

        assertThat(result, is(payload));
        assertThat(result.get("DivorceCostsOptionDNEnum"), is(expectedPayload));
    }
}
