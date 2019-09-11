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
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
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

    private static final DateTimeFormatter FILE_NAME_DATE_FORMAT = ofPattern("ddMMyyyy000000");
    private static final String TEST_AUTH_TOKEN = "testAuthToken";

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
    }

    @Test
    public void shouldEmailCsvFileWithCase_ForDecreeAbsoluteIssued() throws Exception {
        //Mock CMS to return a case like Elastic search will
        maintenanceServiceServer.stubFor(WireMock.post("/casemaintenance/version/1/search")
            .withHeader("Authorization", equalTo(TEST_AUTH_TOKEN))
            .willReturn(okJson("{"
                + "  \"cases\": [{"
                + "    \"case_data\": {"
                + "      \"D8caseReference\": \"CR12345\","
                + "      \"DecreeAbsoluteApplicationDate\": \"2017-03-06T16:49:00.015\","
                + "      \"DecreeNisiGrantedDate\": \"2017-07-06\""
                + "    }"
                + "  }]"
                + "}"))
        );

        dataExtractionService.requestDataExtractionForPreviousDay();

        await().untilAsserted(() -> {
            //Make sure it's only called once until all the files are ready to be extracted
            verify(mockEmailClient, times(1)).sendEmailWithAttachment(any(), any());

            String expectedAttachmentName = String.format("DA_%s.csv", LocalDate.now().minusDays(1).format(FILE_NAME_DATE_FORMAT));
            verify(mockEmailClient).sendEmailWithAttachment(eq(expectedAttachmentName), attachmentCaptor.capture());
            File attachmentFile = attachmentCaptor.getValue();
            assertThat(attachmentFile, is(notNullValue()));
            List<String> lines = Files.readAllLines(attachmentFile.toPath());
            assertThat(lines, hasSize(greaterThan(1)));
            assertThat(lines.get(0), Matchers.equalTo("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
            assertThat(lines.get(1), Matchers.equalTo("CR12345,06/03/2017,06/07/2017,petitioner"));
        });
    }

}