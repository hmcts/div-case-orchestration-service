package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

public class CamelConfig {

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/api" + "/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

}
