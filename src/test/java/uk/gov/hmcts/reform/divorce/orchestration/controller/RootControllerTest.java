package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RootController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
public class RootControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private MockMvc mvc;
    
    @Test
    public void getShouldReturn200() throws Exception {

        // given
        MockHttpServletRequestBuilder getRequest = MockMvcRequestBuilders.get("/");

        // when
        ResultActions performedGet = mvc.perform(getRequest);

        // then
        performedGet.andExpect(status().isOk()).andReturn();
    }
}
