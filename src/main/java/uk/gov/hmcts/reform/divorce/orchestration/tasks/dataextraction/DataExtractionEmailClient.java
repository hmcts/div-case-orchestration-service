package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class DataExtractionEmailClient {

    private static final String SUBJECT = "Divorce data extraction";
    private static final String EMAIL_TEXT = "Data extraction file";

    private final String emailFrom;

    private final JavaMailSenderImpl mailSender;

    public DataExtractionEmailClient(@Value("${dataExtraction.emailFrom}") String emailFrom,
                                     @Autowired JavaMailSenderImpl mailSender) {
        this.emailFrom = emailFrom;
        this.mailSender = mailSender;
    }

    public void sendEmailWithAttachment(String destinationEmailAddress, String attachmentFileName, File attachment) throws MessagingException {
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