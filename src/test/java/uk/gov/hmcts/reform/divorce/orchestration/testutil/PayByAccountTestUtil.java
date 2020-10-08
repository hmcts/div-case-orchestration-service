package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.FeatureToggleServiceImpl;

import java.util.Map;

import static java.util.Collections.singletonMap;

public class PayByAccountTestUtil {

    private static final FeatureToggleServiceImpl featureToggleService = new FeatureToggleServiceImpl();

    public static void setPbaToggleTo(boolean value) {
        Map<String, String> toggles = singletonMap(Features.PAY_BY_ACCOUNT.getName(), Boolean.toString(value));
        ReflectionTestUtils.setField(featureToggleService, "toggle", toggles);

        SolicitorDataExtractor.CaseDataKeys.FEATURE_TOGGLE_SERVICE = featureToggleService;
    }

}
