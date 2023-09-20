package uk.gov.hmcts.reform.divorce.orchestration.service;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import static java.util.Arrays.asList;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintServiceTest {

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    private BulkPrintService classUnderTest;

    @Test
    public void sendLetterApiIsNeverCalledWhenBulkPrintToggledOff() {
        classUnderTest = new BulkPrintService(false, authTokenGenerator, sendLetterApi, featureToggleService);
        classUnderTest.send("foo", "bar", emptyList());

        verifyNoInteractions(sendLetterApi);
        verifyNoInteractions(authTokenGenerator);
    }

    @Test
    public void happyPathWhenSendLetterRecipientCheckDisabled() {
        classUnderTest = new BulkPrintService(true, authTokenGenerator, sendLetterApi, featureToggleService);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);


        final GeneratedDocumentInfo document1 = mock(GeneratedDocumentInfo.class);
        final String document1Data = "foo";
        when(document1.getBytes()).thenReturn(document1Data.getBytes());

        final GeneratedDocumentInfo document2 = mock(GeneratedDocumentInfo.class);
        final String document2Data = "bar";
        when(document2.getBytes()).thenReturn(document2Data.getBytes());

        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenReturn(new SendLetterResponse(randomUUID()));

        final String caseId = "case-id";
        final String letterType = "letter-type";
        classUnderTest.send(caseId, letterType, asList(document1, document2));

        final String stringifiedDocument1 = getEncoder().encodeToString(document1Data.getBytes());
        final String stringifiedDocument2 = getEncoder().encodeToString(document2Data.getBytes());
        final ArgumentCaptor<LetterWithPdfsRequest> letterRequestCaptor = ArgumentCaptor.forClass(LetterWithPdfsRequest.class);
        verify(sendLetterApi, times(1)).sendLetter(eq(authToken), letterRequestCaptor.capture());

        final ImmutableSet<String> expectedAdditionalParameters = ImmutableSet.of(
            "letterType",
            "caseIdentifier",
            "caseReferenceNumber",
            "recipients");

        final String xeroxType = "DIV001";
        assertThat(letterRequestCaptor.getValue(), allOf(
            hasProperty("type", equalTo(xeroxType)),
            hasProperty("documents", equalTo(asList(stringifiedDocument1, stringifiedDocument2)))));

        assertThat("Should match", letterRequestCaptor.getValue().getAdditionalData().keySet().equals(expectedAdditionalParameters));
    }

    @Test
    public void happyPathWhenSendLetterRecipientCheckEnabled() {
        classUnderTest = new BulkPrintService(true, authTokenGenerator, sendLetterApi, featureToggleService);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);


        final GeneratedDocumentInfo document1 = mock(GeneratedDocumentInfo.class);
        final String document1Data = "foo";
        when(document1.getBytes()).thenReturn(document1Data.getBytes());

        final GeneratedDocumentInfo document2 = mock(GeneratedDocumentInfo.class);
        final String document2Data = "bar";
        when(document2.getBytes()).thenReturn(document2Data.getBytes());

        when(featureToggleService.isFeatureEnabled(Features.SEND_LETTER_RECIPIENT_CHECK)).thenReturn(true);
        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenReturn(new SendLetterResponse(randomUUID()));

        final String caseId = "case-id";
        final String letterType = "letter-type";
        classUnderTest.send(caseId, letterType, asList(document1, document2));

        final String stringifiedDocument1 = getEncoder().encodeToString(document1Data.getBytes());
        final String stringifiedDocument2 = getEncoder().encodeToString(document2Data.getBytes());
        final ArgumentCaptor<LetterWithPdfsRequest> letterRequestCaptor = ArgumentCaptor.forClass(LetterWithPdfsRequest.class);
        verify(sendLetterApi, times(1)).sendLetter(eq(authToken), letterRequestCaptor.capture());

        final ImmutableSet<String> expectedAdditionalParameters = ImmutableSet.of(
            "letterType",
            "caseIdentifier",
            "caseReferenceNumber",
            "party");

        final String xeroxType = "DIV001";
        assertThat(letterRequestCaptor.getValue(), allOf(
            hasProperty("type", equalTo(xeroxType)),
            hasProperty("documents", equalTo(asList(stringifiedDocument1, stringifiedDocument2)))));

        assertThat("Should match", letterRequestCaptor.getValue().getAdditionalData().keySet().equals(expectedAdditionalParameters));
    }
}
