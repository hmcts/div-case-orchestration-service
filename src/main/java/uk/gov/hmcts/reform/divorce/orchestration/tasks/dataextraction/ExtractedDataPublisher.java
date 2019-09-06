package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.MessagingException;

import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.DATE_TO_EXTRACT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;

@Component
public class ExtractedDataPublisher implements Task<Void> {

    private static final DateTimeFormatter DATA_EXTRACTION_DATE_FORMAT_FOR_FILE_NAME = DateTimeFormatter.ofPattern("ddMMyyyy000000");

    @Autowired
    private DataExtractionEmailClient emailClient;

    @Override
    public Void execute(TaskContext context, Void payload) throws TaskException {

        LocalDate dateToExtract = context.getTransientObject(DATE_TO_EXTRACT_KEY);
        String attachmentFileName = String.format("DA_%s.csv", dateToExtract.format(DATA_EXTRACTION_DATE_FORMAT_FOR_FILE_NAME));

        try {
            emailClient.sendEmailWithAttachment(attachmentFileName, context.getTransientObject(FILE_TO_PUBLISH));
        } catch (MessagingException e) {
            throw new TaskException("Error sending e-mail with data extraction file.", e);
        }

        return payload;
    }

}