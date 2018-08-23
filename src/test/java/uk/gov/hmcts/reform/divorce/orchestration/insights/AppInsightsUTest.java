package uk.gov.hmcts.reform.divorce.orchestration.insights;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppInsightsUTest {
    @Test
    public void test() {
        TelemetryContext telemetryContext = new TelemetryContext();
        telemetryContext.setInstrumentationKey("key");

        TelemetryClient telemetryClient = mock(TelemetryClient.class);
        when(telemetryClient.getContext()).thenReturn(telemetryContext);

        AppInsights appInsights = new AppInsights(telemetryClient);

        Assert.isInstanceOf(AppInsights.class, appInsights);
    }
}
