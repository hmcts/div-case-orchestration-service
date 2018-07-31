package uk.gov.hmcts.reform.divorce.orchestration.router;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

/**
 * Created by mrganeshraja on 06/05/2018.
 */
@Component
public class RestConfigurationRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // http://localhost:8080/camel/api-doc
        restConfiguration().contextPath("/api") //
                .port("8080")
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Divorce Orchestration Service")
                .apiProperty("api.version", "v1")
                .apiProperty("cors", "true") // cross-site
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");
    }
}
