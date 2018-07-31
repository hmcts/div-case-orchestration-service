package uk.gov.hmcts.reform.divorce.orchestration.router;

import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

/**
 * Router responsible to receive callback events generated from
 * <code>CCD</code> to invoke right work flow and route the requests.
 *
 * @author Ganesh Raja
 * @since 0.1
 */
@Component
public class CallbackRouter extends RouteBuilder {

    /**
     * Configure router
     */
    @Override
    public void configure() {


        rest("/api/").description("Callback route to receive ccd events")
                .id("ccd-events")
                .post("/ccd-event")
                .produces(MediaType.APPLICATION_JSON)
                .consumes(MediaType.APPLICATION_JSON)
                .bindingMode(RestBindingMode.auto)
                .type(MyBean.class)
                .enableCORS(true)
                .to("direct:remoteService");

        from("direct:remoteService")
                .routeId("direct-route")
                .tracing()
                .log(">>> ${body.id}")
                .log(">>> ${body.name}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        MyBean bodyIn = (MyBean) exchange.getIn().getBody();
                        ExampleServices.example(bodyIn);
                        exchange.getIn().setBody(bodyIn);
                    }
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
    }
}
