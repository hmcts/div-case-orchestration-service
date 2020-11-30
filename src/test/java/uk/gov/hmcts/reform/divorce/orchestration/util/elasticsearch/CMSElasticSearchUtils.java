package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;
import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CMSElasticSearchUtils {

    public static final SearchResult EMPTY_SEARCH_RESULT = SearchResult.builder().total(0).cases(emptyList()).build();

    /**
     * Utility method to help set up the iterator to be used in sub-classes of <code>SelfPublishingAsyncTask</code>
     * that might wish to <code>CMSElasticSearchSupport</code>.
     */
    public static void mockCMSElasticSearchSupportToProduceIteratorWithCaseDetailsBatches(CMSElasticSearchSupport mockCmsElasticSearchSupport,
                                                                                          String expectedAuthToken,
                                                                                          QueryBuilder expectedQueryBuilder,
                                                                                          List<CaseDetails>... caseDetailsBatchesToReturn) {
        CMSElasticSearchIterator mockElasticSearchIterator = mock(CMSElasticSearchIterator.class);

        OngoingStubbing<List<CaseDetails>> iteratorStubbing = when(mockElasticSearchIterator.fetchNextBatch());

        for (List<CaseDetails> caseDetailsBatch : caseDetailsBatchesToReturn) {
            iteratorStubbing = iteratorStubbing.thenReturn(caseDetailsBatch);
        }
        iteratorStubbing.thenReturn(emptyList());

        when(mockCmsElasticSearchSupport.createNewCMSElasticSearchIterator(expectedAuthToken, expectedQueryBuilder))
            .thenReturn(mockElasticSearchIterator);
    }

}