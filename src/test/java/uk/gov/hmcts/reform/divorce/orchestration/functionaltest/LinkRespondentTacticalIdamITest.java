package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "idam.strategic.enabled=false"
    })
public class LinkRespondentTacticalIdamITest extends LinkRespondentIdamITest {
}
