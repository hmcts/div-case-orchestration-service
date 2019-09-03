package uk.gov.hmcts.reform.divorce.orchestration.datamigration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class DataMigrationEmailClient {

    private static final String SUBJECT = "Divorce data migration";
    private static final String EMAIL_TEXT = "Data migration file";

    @Value("${dataMigration.emailFrom}")
    private final String emailFrom;

    @Value("${dataMigration.migrationStatus.DA.emailTo}")
    private final String destinationEmailAddress;

    @Autowired
    private final JavaMailSenderImpl mailSender;

    public void sendEmailWithAttachment(String attachmentFileName, File attachment) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(emailFrom);
        helper.setTo(destinationEmailAddress);
        helper.setSubject(SUBJECT);
        helper.setText(EMAIL_TEXT);
        helper.addAttachment(attachmentFileName, attachment);

        mailSender.send(mimeMessage);
    }

}