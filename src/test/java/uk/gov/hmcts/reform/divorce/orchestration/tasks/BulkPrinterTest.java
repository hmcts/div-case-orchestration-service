package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeatureToggleServiceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrinterTest {

    private static final String FEATURE_TOGGLE_NAME = "foo";

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FeatureToggleServiceClient featureToggleServiceClient;

    @InjectMocks
    private BulkPrinter classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "bulkPrintFeatureToggleName", FEATURE_TOGGLE_NAME);
    }

    @Test
    public void sendLetterApiIsNeverCalledWhenBulkPrintToggledOff() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn("false");

        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);
        classUnderTest.execute(new DefaultTaskContext(), emptyMap());

        verifyZeroInteractions(sendLetterApi);
        verifyZeroInteractions(authTokenGenerator);
    }

    @Test
    public void sendLetterApiIsNeverCalledWhenBulkPrintToggleValueAbsent() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn(null);

        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);
        classUnderTest.execute(new DefaultTaskContext(), emptyMap());

        verifyZeroInteractions(sendLetterApi);
        verifyZeroInteractions(authTokenGenerator);

    }

    @Test
    public void respondentPackIsSent() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn("true");
        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);

        final DefaultTaskContext context = new DefaultTaskContext();

        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject("case_details", caseDetails);

        final GeneratedDocumentInfo petitionDocument = mock(GeneratedDocumentInfo.class);
        final String petitionData = "This is a mini petition";
        when(petitionDocument.getBytes()).thenReturn(petitionData.getBytes());

        final GeneratedDocumentInfo aosInvitation = mock(GeneratedDocumentInfo.class);
        final String aosData = "This is a aos invitation";
        when(aosInvitation.getBytes()).thenReturn(aosData.getBytes());

        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", petitionDocument);
        generatedDocuments.put("aos", aosInvitation);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenReturn(new SendLetterResponse(randomUUID()));

        classUnderTest.execute(context, emptyMap());

        final String stringifiedMiniPetition = getEncoder().encodeToString(petitionData.getBytes());
        final String stringifiedAosLetter = getEncoder().encodeToString(aosData.getBytes());
        final ArgumentCaptor<LetterWithPdfsRequest> letterRequestCaptor = ArgumentCaptor.forClass(LetterWithPdfsRequest.class);
        verify(sendLetterApi, times(1)).sendLetter(eq(authToken), letterRequestCaptor.capture());

        final String letterType = "DIV001";
        final ImmutableMap<String, Object> expectedAdditionalParameters = ImmutableMap.of(
            "letterType", "respondent-aos-pack",
            "caseIdentifier", caseId,
            "caseReferenceNumber", caseId);

        assertThat(letterRequestCaptor.getValue(), allOf(
            hasProperty("type", equalTo(letterType)),
            hasProperty("documents", equalTo(asList(stringifiedAosLetter, stringifiedMiniPetition))),
            hasProperty("additionalData", equalTo(expectedAdditionalParameters))));
    }

    @Test
    public void errorsSendingRespondentPackAreReported() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn("true");
        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);

        final DefaultTaskContext context = new DefaultTaskContext();

        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject("case_details", caseDetails);

        final GeneratedDocumentInfo petitionDocument = mock(GeneratedDocumentInfo.class);
        final String petitionData = "This is a mini petition";
        when(petitionDocument.getBytes()).thenReturn(petitionData.getBytes());

        final GeneratedDocumentInfo aosInvitation = mock(GeneratedDocumentInfo.class);
        final String aosData = "This is a aos invitation";
        when(aosInvitation.getBytes()).thenReturn(aosData.getBytes());

        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", petitionDocument);
        generatedDocuments.put("aos", aosInvitation);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenThrow(RuntimeException.class);

        classUnderTest.execute(context, emptyMap());

        assertThat(context.getStatus(), is(true));
        assertThat(context.getTransientObject(classUnderTest.getClass().getName() + "_Error"),
            is("Bulk print failed for respondent pack"));
    }

    @Test
    public void respondentAndCoRespondentPackIsSent() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn("true");
        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);

        final DefaultTaskContext context = new DefaultTaskContext();

        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject("case_details", caseDetails);

        final GeneratedDocumentInfo petitionDocument = mock(GeneratedDocumentInfo.class);
        final String petitionData = "This is a mini petition";
        when(petitionDocument.getBytes()).thenReturn(petitionData.getBytes());

        final GeneratedDocumentInfo aosInvitation = mock(GeneratedDocumentInfo.class);
        final String aosData = "This is a aos invitation";
        when(aosInvitation.getBytes()).thenReturn(aosData.getBytes());

        final GeneratedDocumentInfo coRespondentLetter = mock(GeneratedDocumentInfo.class);
        final String coRespondentLetterData = "This is a co-respondent letter";
        when(coRespondentLetter.getBytes()).thenReturn(coRespondentLetterData.getBytes());

        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", petitionDocument);
        generatedDocuments.put("aos", aosInvitation);
        generatedDocuments.put("aoscr", coRespondentLetter);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenReturn(new SendLetterResponse(randomUUID()));

        classUnderTest.execute(context, emptyMap());

        final String stringifiedMiniPetition = getEncoder().encodeToString(petitionData.getBytes());
        final String stringifiedAosLetter = getEncoder().encodeToString(aosData.getBytes());
        final String stringifiedCoRespondentLetter = getEncoder().encodeToString(coRespondentLetterData.getBytes());
        final ArgumentCaptor<LetterWithPdfsRequest> letterRequestCaptor = ArgumentCaptor.forClass(LetterWithPdfsRequest.class);
        verify(sendLetterApi, times(2)).sendLetter(eq(authToken), letterRequestCaptor.capture());

        final String letterType = "DIV001";
        final ImmutableMap<String, Object> expectedRespondentPackAdditionalParameters = ImmutableMap.of(
            "letterType", "respondent-aos-pack",
            "caseIdentifier", caseId,
            "caseReferenceNumber", caseId);
        final ImmutableMap<String, Object> expectedCoRespondentPackAdditionalParameters = ImmutableMap.of(
            "letterType", "co-respondent-aos-pack",
            "caseIdentifier", caseId,
            "caseReferenceNumber", caseId);

        assertThat(letterRequestCaptor.getAllValues(), contains(
            allOf(hasProperty("type", equalTo(letterType)),
                  hasProperty("documents", equalTo(asList(stringifiedAosLetter, stringifiedMiniPetition))),
                  hasProperty("additionalData", equalTo(expectedRespondentPackAdditionalParameters))),
            allOf(hasProperty("type", equalTo(letterType)),
                  hasProperty("documents", equalTo(asList(stringifiedCoRespondentLetter, stringifiedMiniPetition))),
                  hasProperty("additionalData", equalTo(expectedCoRespondentPackAdditionalParameters)))));
    }

    @Test
    public void errorsSendingCoRespondentPackAreReported() {
        final FeatureToggle featureToggle = mock(FeatureToggle.class);
        when(featureToggle.getEnable()).thenReturn("true");
        when(featureToggleServiceClient.getToggle(FEATURE_TOGGLE_NAME)).thenReturn(featureToggle);

        final String authToken = "auth-token";
        when(authTokenGenerator.generate()).thenReturn(authToken);

        final DefaultTaskContext context = new DefaultTaskContext();

        final String caseId = "case-id";
        final CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).build();
        context.setTransientObject("case_details", caseDetails);

        final GeneratedDocumentInfo petitionDocument = mock(GeneratedDocumentInfo.class);
        final String petitionData = "This is a mini petition";
        when(petitionDocument.getBytes()).thenReturn(petitionData.getBytes());

        final GeneratedDocumentInfo aosInvitation = mock(GeneratedDocumentInfo.class);
        final String aosData = "This is a aos invitation";
        when(aosInvitation.getBytes()).thenReturn(aosData.getBytes());

        final GeneratedDocumentInfo coRespondentLetter = mock(GeneratedDocumentInfo.class);
        final String coRespondentLetterData = "This is a co-respondent letter";
        when(coRespondentLetter.getBytes()).thenReturn(coRespondentLetterData.getBytes());

        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put("petition", petitionDocument);
        generatedDocuments.put("aos", aosInvitation);
        generatedDocuments.put("aoscr", coRespondentLetter);
        context.setTransientObject("DocumentsGenerated", generatedDocuments);

        when(sendLetterApi.sendLetter(eq(authToken), any(LetterWithPdfsRequest.class))).thenThrow(RuntimeException.class);

        classUnderTest.execute(context, emptyMap());

        assertThat(context.getStatus(), is(true));
        assertThat(context.getTransientObject(classUnderTest.getClass().getName() + "_Error"),
            is("Bulk print failed for co-respondent pack"));
    }
}
