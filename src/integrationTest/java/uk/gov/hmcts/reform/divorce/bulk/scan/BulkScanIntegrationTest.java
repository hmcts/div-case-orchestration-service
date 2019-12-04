package uk.gov.hmcts.reform.divorce.bulk.scan;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.NEW_DIVORCE_CASE;

public class BulkScanIntegrationTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedService() {
        cosApiClient.validateBulkScannedFields("any", NEW_DIVORCE_CASE, new OcrDataValidationRequest(singletonList(new OcrDataField("name", "value"))));
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedService() {
        fail();
    }

}