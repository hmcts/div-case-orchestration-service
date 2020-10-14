package uk.gov.hmcts.reform.divorce.orchestration.controller.advice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;

import java.beans.PropertyEditorSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class GlobalControllerFormatterTest {

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
        verify(webdataBinder).registerCustomEditor(eq(DivorceParty.class), customEditorCaptor.capture());
        PropertyEditorSupport propertyEditor = customEditorCaptor.getValue();

        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> propertyEditor.setAsText("invalid-value")
        );

        assertThat(
            illegalArgumentException.getMessage(),
            is("Could not find divorce party with the given description: invalid-value")
        );
    }
}
