package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Files;
import javax.mail.MessagingException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;

/**
 * This test can be run to test (locally) that our e-mail is sent according to our expectations.
 * This assertion has to be done manually for now, using MailHog.
 * All we have to do is run MailHog in Docker and run this test (which will be ignored by default).
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataExtractionEmailClientTest {

    private File file;

    @Rule
    public ExpectedException expectedException = none();

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

    @Test
    public void shouldThrowExceptionWhenEmailIsEmpty() throws MessagingException {
        expectedException.expect(instanceOf(Exception.class));

        dataExtractionEmailClient.sendEmailWithAttachment("", "myFileName.csv", file);
    }

}