package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.dataextraction;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionEmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.mail.MessagingException;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataExtractionToFamilyManTest extends MockedFunctionalTest {

    private static final String DA_DESIRED_STATES = "[\"darequested\", \"divorcegranted\"]";
    private static final String AOS_DESIRED_STATES = "[\"awaitinglegaladvisorreferral\"]";
    private static final String DN_DESIRED_STATES = "[\"dnisrefused\", \"dnpronounced\"]";

    private static final DateTimeFormatter FILE_NAME_DATE_FORMAT = ofPattern("ddMMyyyy000000");
    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    private String yesterday;

    @Autowired
    private DataExtractionService dataExtractionService;

    @MockBean
    private DataExtractionEmailClient mockEmailClient;

    @MockBean
    private AuthUtil authUtil;

    @Captor
    private ArgumentCaptor<File> attachmentCaptor;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(TEST_AUTH_TOKEN);
        yesterday = LocalDate.now().minusDays(1).format(FILE_NAME_DATE_FORMAT);
    }

    @Test
    public void shouldEmailCsvFileWithCase_ForDecreeAbsoluteIssued() throws Exception {
        //Mock CMS to return a case like Elastic search will
        stubJsonResponse(DA_DESIRED_STATES, "{"
            + "  \"cases\": [{"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"CR12345\","
            + "      \"DecreeAbsoluteApplicationDate\": \"2017-03-06T16:49:00.015\","
            + "      \"DecreeNisiGrantedDate\": \"2017-07-06\""
            + "    }"
            + "  }]"
            + "}");
        stubJsonResponse(AOS_DESIRED_STATES, "{"
            + "  \"cases\": [{"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"LV17D90909\","
            + "      \"ReceivedAOSfromRespDate\": \"2019-07-06\","
            + "      \"ReceivedAosFromCoRespDate\": \"2019-07-15\","
            + "      \"DNApplicationSubmittedDate\": \"2019-08-01\""
            + "    }"
            + "  }]"
            + "}");
        stubJsonResponse(DN_DESIRED_STATES, "{"
            + "  \"cases\": [{"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"LV17D90909\","
            + "      \"DNApprovalDate\": \"2020-12-15\","
            + "      \"DateAndTimeOfHearing\": \"2020-12-10T15:30\","
            + "      \"CourtName\": \"This Court\","
            + "      \"D8DivorceCostsClaim\": \"Yes\","
            + "      \"WhoPaysCosts\": \"Respondent\","
            + "      \"costs claim granted\": \"Yes\","
            + "      \"OrderForAncilliaryRelief\": \"No\","
            + "      \"OrderOrCauseList\": \"Order\","
            + "      \"PronouncementJudge\": \"Judge Dave\""
            + "    }"
            + "  }]"
            + "}");

        dataExtractionService.requestDataExtractionForPreviousDay();

        await().untilAsserted(() -> {
            //Make sure it's only called once until all the files are ready to be extracted
            verify(mockEmailClient, times(2)).sendEmailWithAttachment(any(), any(), any());
        });
        verifyExtractionInteractions("DA",
            "da-extraction@divorce.gov.uk",
            DA_DESIRED_STATES,
            "CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA",
            "CR12345,06/03/2017,06/07/2017,petitioner"
        );
        verifyExtractionInteractions("AOSDN",
            "aos-extraction@divorce.gov.uk",
            AOS_DESIRED_STATES,
            "CaseReferenceNumber,ReceivedAOSFromResDate,ReceivedAOSFromCoResDate,ReceivedDNApplicationDate",
            "LV17D90909,06/07/2019,15/07/2019,01/08/2019"
        );
        verifyExtractionInteractions("DN",
            "dn-extraction@divorce.gov.uk",
            DN_DESIRED_STATES,
            "CaseReferenceNumber,CofEGrantedDate,HearingDate,HearingTime,PlaceOfHearing,OrderForCosts,"
                + "PartyToPayCosts,CostsToBeAssessed,OrderForAncilliaryRelief,OrderOrCauseList,JudgesName",
            "LV17D90909,15/12/2020,10/12/2020,15:30,This Court,Yes,Respondent,Yes,No,Order,Judge Dave"
        );
    }

    private void stubJsonResponse(String desiredStates, String caseData) {
        maintenanceServiceServer.stubFor(WireMock.post("/casemaintenance/version/1/search")
            .withHeader("Authorization", equalTo(TEST_AUTH_TOKEN))
            .withRequestBody(matchingJsonPath("$.query.bool.filter[*].terms.state[*]",
                equalToJson(desiredStates, true, false)))
            .willReturn(okJson(caseData))
        );
    }

    private void verifyExtractionInteractions(String filePrefix,
                                              String destinationEmailAddress,
                                              String desiredStates,
                                              String header,
                                              String contentFirstLine) throws MessagingException, IOException {
        maintenanceServiceServer.verify(1, postRequestedFor(urlEqualTo("/casemaintenance/version/1/search"))
            .withRequestBody(matchingJsonPath("$.query.bool.filter[*].terms.state[*]",
                equalToJson(desiredStates, true, false))));

        verify(mockEmailClient).sendEmailWithAttachment(eq(destinationEmailAddress),
            eq(format("%s_%s.csv", filePrefix, yesterday)),
            attachmentCaptor.capture());

        File attachmentFile = attachmentCaptor.getValue();
        assertThat(attachmentFile, is(notNullValue()));
        List<String> csvLines = Files.readAllLines(attachmentFile.toPath());
        assertThat(csvLines, hasSize(greaterThan(1)));
        assertThat(csvLines.get(0), Matchers.equalTo(header));
        assertThat(csvLines.get(1), Matchers.equalTo(contentFirstLine));
    }

}