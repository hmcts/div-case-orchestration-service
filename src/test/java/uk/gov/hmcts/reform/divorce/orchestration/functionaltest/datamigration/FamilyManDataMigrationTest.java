package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.datamigration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.datamigration.DataMigrationEmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class FamilyManDataMigrationTest extends MockedFunctionalTest {

    private static final DateTimeFormatter FILE_NAME_DATE_FORMAT = ofPattern("ddMMyyyy000000");
    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private DataMigrationEmailClient mockEmailClient;

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
                + "      \"DecreeAbsoluteApplicationDate\": \"2017-03-06\","
                + "      \"DecreeNisiGrantedDate\": \"2017-07-06\""
                + "    }"
                + "  }]"
                + "}"))
        );

        webClient.perform(post("/cases/data-migration/family-man"))
            .andExpect(status().isOk());

        String expectedAttachmentName = String.format("DA_%s.csv", LocalDate.now().minusDays(1).format(FILE_NAME_DATE_FORMAT));
        verify(mockEmailClient).sendEmailWithAttachment(eq(expectedAttachmentName), attachmentCaptor.capture());
        File attachmentFile = attachmentCaptor.getValue();
        assertThat(attachmentFile, is(notNullValue()));
        List<String> lines = Files.readAllLines(attachmentFile.toPath());
        assertThat(lines, hasSize(greaterThan(1)));
        assertThat(lines.get(0), Matchers.equalTo("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(lines.get(1), Matchers.equalTo("CR12345,06/03/2017,06/07/2017,petitioner"));
    }

}