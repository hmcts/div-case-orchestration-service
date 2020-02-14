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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

/**
 * This test uses the service (just like the scheduled job will).
 */
public class DataExtractionToFamilyManTest extends MockedFunctionalTest {

    private static final String DA_DESIRED_STATES = "[\"divorcegranted\"]";
    private static final String AOS_DESIRED_STATES = "[\"awaitinglegaladvisorreferral\"]";
    private static final String DN_DESIRED_STATES = "[\"dnisrefused\", \"dnpronounced\"]";

    private static final DateTimeFormatter FILE_NAME_DATE_FORMAT = ofPattern("ddMMyyyy000000");

    private String yesterday;

    @Autowired
    private DataExtractionService dataExtractionService;

    @MockBean
    protected DataExtractionEmailClient mockEmailClient;

    @MockBean
    protected AuthUtil authUtil;

    @Captor
    private ArgumentCaptor<File> attachmentCaptor;

    @Before
    public void setUp() {
        maintenanceServiceServer.resetAll();

        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        yesterday = LocalDate.now().minusDays(1).format(FILE_NAME_DATE_FORMAT);

        //Mock CMS to return a case like Elastic search will
        stubJsonResponse(DA_DESIRED_STATES, "{"
            + "  \"cases\": [{"
            + "    \"id\": 123,"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"CR12345\","
            + "      \"DecreeAbsoluteGrantedDate\": \"2017-03-06T16:49:00.015\","
            + "      \"DecreeNisiGrantedDate\": \"2017-07-06\""
            + "    }"
            + "  }]"
            + "}");
        stubJsonResponse(AOS_DESIRED_STATES, "{"
            + "  \"cases\": [{"
            + "    \"id\": 456,"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"LV17D90909\","
            + "      \"ReceivedAOSfromRespDate\": \"2019-07-06\","
            + "      \"ReceivedAosFromCoRespDate\": \"2019-07-15\","
            + "      \"DNApplicationSubmittedDate\": \"2019-08-01\""
            + "    }"
            + "  }]"
            + "}");
        stubJsonResponse(DN_DESIRED_STATES, "{"
            + "  \"cases\": ["
            + " {"
            + "    \"id\": 789,"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"LV17D90910\","
            + "      \"DNApprovalDate\": \"2020-12-15\","
            + "      \"DateAndTimeOfHearing\": ["
            + "        {"
            + "          \"id\": \"8bde74de-7a69-411f-aaef-1a5ea8018743\","
            + "          \"value\": {"
            + "            \"DateOfHearing\": \"2020-12-10\","
            + "            \"TimeOfHearing\": \"15:30\""
            + "          }"
            + "        }"
            + "      ],"
            + "      \"CourtName\": \"invalid-court-name\","
            + "      \"D8DivorceCostsClaim\": \"Yes\","
            + "      \"WhoPaysCosts\": \"Respondent\","
            + "      \"costs claim granted\": \"Yes\","
            + "      \"OrderForAncilliaryRelief\": \"No\","
            + "      \"OrderOrCauseList\": \"Order\","
            + "      \"PronouncementJudge\": \"Judge Dave\""
            + "    }"
            + " },"
            + " {"
            + "    \"id\": 101,"
            + "    \"case_data\": {"
            + "      \"D8caseReference\": \"LV17D90911\","
            + "      \"DNApprovalDate\": \"2020-12-15\","
            + "      \"DateAndTimeOfHearing\": ["
            + "        {"
            + "          \"id\": \"8bde74de-7a69-411f-aaef-1a5ea8018743\","
            + "          \"value\": {"
            + "            \"DateOfHearing\": \"2020-12-10\","
            + "            \"TimeOfHearing\": \"15:30\""
            + "          }"
            + "        }"
            + "      ],"
            + "      \"CourtName\": \"bradford\","
            + "      \"D8DivorceCostsClaim\": \"Yes\","
            + "      \"WhoPaysCosts\": \"Respondent\","
            + "      \"costs claim granted\": \"Yes\","
            + "      \"OrderForAncilliaryRelief\": \"No\","
            + "      \"OrderOrCauseList\": \"Order\","
            + "      \"PronouncementJudge\": \"Judge Dave\""
            + "    }"
            + "  }"
            + "]"
            + "}");
    }

    @Test
    public void shouldEmailCsvFileWithCase_ForDecreeAbsoluteIssued() throws Exception {
        dataExtractionService.requestDataExtractionForPreviousDay();

        await().untilAsserted(() -> {
            //Make sure it's only called once until all the files are ready to be extracted
            verify(mockEmailClient, times(3)).sendEmailWithAttachment(any(), any(), any());
        });
        verifyExtractionInteractions("DA",
            "da-extraction@divorce.gov.uk",
            DA_DESIRED_STATES,
            "CaseReferenceNumber,DAGrantedDate,DNPronouncementDate,PartyApplyingForDA",
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
            "LV17D90910,15/12/2020,10/12/2020,15:30,invalid-court-name,Yes,Respondent,Yes,No,Order,Judge Dave",
            "LV17D90911,15/12/2020,10/12/2020,15:30,Bradford Law Courts,Yes,Respondent,Yes,No,Order,Judge Dave"
        );
    }

    private void stubJsonResponse(String desiredStates, String caseData) {
        maintenanceServiceServer.stubFor(WireMock.post("/casemaintenance/version/1/search")
            .withHeader("Authorization", equalTo(AUTH_TOKEN))
            .withRequestBody(matchingJsonPath("$.query.bool.filter[*].terms.state[*]",
                equalToJson(desiredStates, true, false)))
            .willReturn(okJson(caseData))
        );
    }

    private void verifyExtractionInteractions(String filePrefix,
                                              String destinationEmailAddress,
                                              String desiredStates,
                                              String... contentLines) throws MessagingException, IOException {
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
        for (int i = 0; i < contentLines.length; i++) {
            assertThat(csvLines.get(i), Matchers.equalTo(contentLines[i]));
        }
    }

}