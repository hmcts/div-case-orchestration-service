package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TEMPLATE_RELATION;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TemplateConfigTest {

    @Autowired
    private TemplateConfig templateConfig;
    private Map<String, String> relations;

    public static final String MONTHS = "months";

    @Test
    public void en_hustband() {
        relations = templateConfig.getTemplate().get(TEMPLATE_RELATION).get(LanguagePreference.ENGLISH);
        Assert.assertEquals("husband", relations.get("male"));
    }

    @Test
    public void en_wife() {
        relations = templateConfig.getTemplate().get(TEMPLATE_RELATION).get(LanguagePreference.ENGLISH);
        Assert.assertEquals("wife", relations.get("female"));
    }

    @Test
    public void welsh_hustband() {
        relations = templateConfig.getTemplate().get(TEMPLATE_RELATION).get(LanguagePreference.WELSH);
        Assert.assertEquals("gŵr", relations.get("male"));
    }

    @Test
    public void welsh__wife() {
        relations = templateConfig.getTemplate().get(TEMPLATE_RELATION).get(LanguagePreference.WELSH);
        Assert.assertEquals("gwraig", relations.get("female"));
    }

    @Test
    public void welsh_month() {
        String novMonth =  templateConfig.getTemplate().get(MONTHS)
                .get(LanguagePreference.WELSH).get("11");
        Assert.assertEquals("Tachwedd", novMonth);
    }
}