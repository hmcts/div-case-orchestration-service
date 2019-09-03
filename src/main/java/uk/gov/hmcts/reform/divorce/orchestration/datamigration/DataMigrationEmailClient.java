package uk.gov.hmcts.reform.divorce.orchestration.datamigration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class DataMigrationEmailClient {

    private static final String SUBJECT = "Divorce data migration";
    private static final String EMAIL_TEXT = "Data migration file";

    private final String emailFrom;
    private final String destinationEmailAddress;

    private final JavaMailSenderImpl mailSender;

    public DataMigrationEmailClient(@Value("${dataMigration.emailFrom}")String emailFrom,
                                    @Value("${dataMigration.migrationStatus.DA.emailTo}") String destinationEmailAddress,
                                    @Autowired JavaMailSenderImpl mailSender) {
        this.emailFrom = emailFrom;
        this.destinationEmailAddress = destinationEmailAddress;
        this.mailSender = mailSender;
    }

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