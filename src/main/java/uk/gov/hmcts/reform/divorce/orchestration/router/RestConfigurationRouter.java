package uk.gov.hmcts.reform.divorce.orchestration.router;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestConfigurationRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/api")
                .port("8080")
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Divorce Orchestration Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");
    }
}
