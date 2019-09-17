package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionEmailClient;

import java.io.File;
import java.nio.file.Files;

import javax.mail.MessagingException;

/**
 * This test can be run to test (locally) that our e-mail is sent according to our expectations.
 * This assertion has to be done manually for now, using MailHog.
 * All we have to do is run MailHog in Docker and run this test (which will be ignored by default).
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataExtractionEmailClientTest {

    private File file;

    @Autowired
    private DataExtractionEmailClient dataExtractionEmailClient;

    @Before
    public void setUp() throws Exception {
        file = Files.createTempFile("test", ".csv").toFile();
        file.deleteOnExit();
        String content = "line1" + System.lineSeparator() + "line2" + System.lineSeparator() + "line3";
        Files.write(file.toPath(), content.getBytes());
    }

    @Test
    @Ignore
    public void sendEmailWithAttachment() throws MessagingException {
        dataExtractionEmailClient.sendEmailWithAttachment("test@divorce.gov.uk", "myFileName.csv", file);
        //Now go to MailHog and check that your e-mail has been sent as expected
    }

}