package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Ignore;
import org.springframework.test.context.TestPropertySource;

@Ignore
@TestPropertySource(properties = {
    "idam.strategic.enabled=false"
    })
public class LinkRespondentTacticalIdamITest extends LinkRespondentIdamITest {
}
