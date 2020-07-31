package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TemplateConfigServiceImplTest {

    @Autowired
    TemplateConfigService templateConfigService;

    @Test
    public void getRelationshipTermByGender_male_eng() {
        assertEquals("husband", templateConfigService.getRelationshipTermByGender("male", LanguagePreference.ENGLISH));
    }

    @Test
    public void getRelationshipTermByGender_female_eng() {
        assertEquals("wife", templateConfigService.getRelationshipTermByGender("female", LanguagePreference.ENGLISH));
    }

    @Test
    public void getRelationshipTermByGender_male_welsh() {
        assertEquals("gŵr", templateConfigService.getRelationshipTermByGender("male", LanguagePreference.WELSH));
    }

    @Test
    public void getRelationshipTermByGender_female_welsh() {
        assertEquals("gwraig", templateConfigService.getRelationshipTermByGender("female", LanguagePreference.WELSH));
    }
}
