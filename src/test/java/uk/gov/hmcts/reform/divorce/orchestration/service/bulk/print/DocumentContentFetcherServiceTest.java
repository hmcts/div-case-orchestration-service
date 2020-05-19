package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.exception.FetchingDocumentFromDmStoreException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class DocumentContentFetcherServiceTest {

    public static final String URL = "dm-store-file-url";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private DocumentContentFetcherService documentContentFetcherService;

    @Before
    public void setup() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void fetchPrintContentCallsDmStore() {
        final byte[] documentContent = new byte[100];
        ResponseEntity<byte[]> responseFromDmStore = ResponseEntity.ok(documentContent);

        when(restTemplate.exchange(eq(URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(responseFromDmStore);

        final GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
            .url(URL)
            .fileName("aaa")
            .mimeType("bbb")
            .documentType("ccc")
            .createdOn("ddd")
            .build();

        final GeneratedDocumentInfo documentWithPopulatedBytes = documentContentFetcherService.fetchPrintContent(documentInfo);

        assertThat(documentWithPopulatedBytes.getUrl(), is(URL));
        assertThat(documentWithPopulatedBytes.getBytes(), is(documentContent));
        assertDocsHaveTheSameMetadata(documentInfo, documentWithPopulatedBytes);
        // make sure it's immutable
        assertThat(documentInfo, not(sameInstance(documentWithPopulatedBytes)));
    }

    @Test(expected = FetchingDocumentFromDmStoreException.class)
    public void fetchPrintContentCallsDmStoreButFails() {
        ResponseEntity<byte[]> dmStoreBadRequestResponse = ResponseEntity.badRequest().build();

        when(restTemplate.exchange(eq(URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(dmStoreBadRequestResponse);

        documentContentFetcherService.fetchPrintContent(GeneratedDocumentInfo.builder().url(URL).build());
    }

    private void assertDocsHaveTheSameMetadata(GeneratedDocumentInfo doc1, GeneratedDocumentInfo doc2) {
        assertThat(doc2.getUrl(), is(doc1.getUrl()));
        assertThat(doc2.getFileName(), is(doc1.getFileName()));
        assertThat(doc2.getDocumentType(), is(doc1.getDocumentType()));
        assertThat(doc2.getCreatedOn(), is(doc1.getCreatedOn()));
        assertThat(doc2.getMimeType(), is(doc1.getMimeType()));
    }
}
