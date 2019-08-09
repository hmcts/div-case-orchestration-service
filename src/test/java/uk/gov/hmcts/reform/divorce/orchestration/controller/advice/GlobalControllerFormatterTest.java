package uk.gov.hmcts.reform.divorce.orchestration.controller.advice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;

import java.beans.PropertyEditorSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GlobalControllerFormatterTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private WebDataBinder webdataBinder;

    @Captor
    private ArgumentCaptor<PropertyEditorSupport> customEditorCaptor;

    private GlobalControllerFormatter globalControllerFormatter;

    @Before
    public void setUp() {
        globalControllerFormatter = new GlobalControllerFormatter();
        globalControllerFormatter.initBinder(webdataBinder);
    }

    @Test
    public void testWebDataBinderHasRightRightCustomEditors() {
        verify(webdataBinder).registerCustomEditor(eq(DivorceParty.class), isNotNull());
    }

    @Test
    public void testDivorcePartyCustomEditorsWorksAsExpected() {
        verify(webdataBinder).registerCustomEditor(eq(DivorceParty.class), customEditorCaptor.capture());
        PropertyEditorSupport propertyEditor = customEditorCaptor.getValue();

        propertyEditor.setAsText("respondent");
        assertThat(propertyEditor.getValue(), equalTo(RESPONDENT));
    }

    @Test
    public void testDivorcePartyCustomEditorsFailsAsExpected() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Could not find divorce party with the given description: " + "invalid-value");

        verify(webdataBinder).registerCustomEditor(eq(DivorceParty.class), customEditorCaptor.capture());
        PropertyEditorSupport propertyEditor = customEditorCaptor.getValue();

        propertyEditor.setAsText("invalid-value");
    }

}