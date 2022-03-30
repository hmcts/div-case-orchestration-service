package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByReference;
import uk.gov.hmcts.reform.divorce.orchestration.util.csv.CaseReference;
import uk.gov.hmcts.reform.divorce.orchestration.util.csv.CaseReferenceCsvLoader;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrintRespondentAosPackService {

    private final CaseReferenceCsvLoader csvLoader;
    private final FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    private final RespondentAosPackPrinterTask respondentAosPackPrinterTask;
    private final SearchForCaseByReference searchForCaseByReference;
    @Value("${aos.bulkprint.batchsize}")
    private final Integer bulkPrintBatchSize;
    @Value("${aos.bulkprint.wait-time-mins}")
    private final Integer bulkPrintWaitTime;

    public void printAosPacks() throws InterruptedException {
        log.info("In the Print Respondent AOS Pack service job");
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("printAosPackCaseReferenceList.csv");
        int count = 0;
        for (CaseReference caseReference : caseReferences) {
            count++;
            if (count == bulkPrintBatchSize) {
                TimeUnit.MINUTES.sleep(bulkPrintWaitTime);
                count = 0;
            }
            try {
                log.info("Search for case reference {}", caseReference.getCaseReference());
                Optional<List<CaseDetails>> caseDetailsListOpt =
                    searchForCaseByReference.searchCasesByCaseReference(caseReference.getCaseReference());
                if (caseDetailsListOpt.isPresent()) {
                    TaskContext taskContext = new DefaultTaskContext();
                    CaseDetails caseDetails = caseDetailsListOpt.get().get(0);
                    taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
                    log.info("Got the caseDetails for {}, now fetch the docs", caseReference.getCaseReference());
                    Map<String, Object> caseData = caseDetails.getCaseData();
                    fetchPrintDocsFromDmStoreTask.execute(taskContext, caseData);
                    log.info("Got the docs for {}, now print the aos pack", caseReference.getCaseReference());
                    respondentAosPackPrinterTask.execute(taskContext, caseData);
                }

            } catch (RuntimeException e) {
                log.error("Error processing caseRef {} ", caseReference.getCaseReference());
                continue;
            }
        }
    }
}
