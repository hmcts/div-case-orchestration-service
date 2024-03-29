package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByReference;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
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
    private final CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;
    private final SearchForCaseByReference searchForCaseByReference;
    private final CaseDataUtils caseDataUtils;
    @Value("${aos.bulkprint.batchsize:500}")
    private int bulkPrintBatchSize;
    @Value("${aos.bulkprint.wait-time-mins:10}")
    private int bulkPrintWaitTime;

    public void printAosPacks() throws InterruptedException {
        log.info("In the Print Respondent AOS Pack service job");
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList("printAosPackCaseReferenceList.csv");
        int count = 0;
        int batchCount = 1;
        for (CaseReference caseReference : caseReferences) {
            count++;
            if (count == bulkPrintBatchSize) {
                log.info("Batch {} limit reached {}, pausing for {} minutes", batchCount, bulkPrintBatchSize, bulkPrintWaitTime);
                TimeUnit.MINUTES.sleep(bulkPrintWaitTime);
                count = 0;
                batchCount++;
            }
            try {
                log.info("Process case reference {}, batch {}, count {}", caseReference.getCaseReference(), batchCount, count);
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
                    if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
                        coRespondentAosPackPrinterTask.execute(taskContext, caseData);
                    }
                }

            } catch (RuntimeException e) {
                log.error("Error processing caseRef {} ", caseReference.getCaseReference());
                continue;
            }
        }
    }
}
