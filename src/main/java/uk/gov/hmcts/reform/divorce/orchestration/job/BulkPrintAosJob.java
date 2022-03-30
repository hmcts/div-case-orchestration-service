package uk.gov.hmcts.reform.divorce.orchestration.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.PrintRespondentAosPackService;

@Slf4j
public class BulkPrintAosJob implements Job {

    @Autowired
    PrintRespondentAosPackService printRespondentAosPackService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Starting PrintRespondentAosPackService Job...");
        try {
            printRespondentAosPackService.printAosPacks();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("PrintRespondentAosPackService Job successfully executed");
    }
}
