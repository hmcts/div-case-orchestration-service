package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.MessagingException;

import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.DATE_TO_EXTRACT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;

@Component
@Slf4j
public class ExtractedDataPublisher implements Task<Void> {

    private static final DateTimeFormatter DATA_EXTRACTION_DATE_FORMAT_FOR_FILE_NAME = DateTimeFormatter.ofPattern("ddMMyyyy000000");

    @Autowired
    private DataExtractionEmailClient emailClient;

    @Autowired
    private CSVExtractorFactory csvExtractorFactory;

    @Override
    public Void execute(TaskContext context, Void payload) throws TaskException {
        Status status = context.getTransientObject("status");
        CSVExtractorStrategy csvExtractorStrategy = csvExtractorFactory.getCSVExtractorForStatus(status);
        String dateToProcess = getDateToProcess(context);
        String attachmentFileName = String.format("%s_%s.csv", csvExtractorStrategy.getFileNamePrefix(), dateToProcess);

        try {
            emailClient.sendEmailWithAttachment(csvExtractorStrategy.getDestinationEmailAddress(),
                attachmentFileName,
                context.getTransientObject(FILE_TO_PUBLISH));
            log.info("Sent extracted data to {}", csvExtractorStrategy.getDestinationEmailAddress());
        } catch (MessagingException e) {
            throw new TaskException("Error sending e-mail with data extraction file.", e);
        }

        return payload;
    }

    private String getDateToProcess(TaskContext context) {
        LocalDate dateToExtract = context.getTransientObject(DATE_TO_EXTRACT_KEY);
        return dateToExtract.format(DATA_EXTRACTION_DATE_FORMAT_FOR_FILE_NAME);
    }

}