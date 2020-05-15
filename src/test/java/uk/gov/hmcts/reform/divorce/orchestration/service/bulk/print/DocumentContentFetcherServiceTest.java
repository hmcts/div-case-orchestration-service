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

        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder().url(URL).build();

        GeneratedDocumentInfo documentWithPopulatedBytes = documentContentFetcherService.fetchPrintContent(documentInfo);

        assertThat(documentWithPopulatedBytes.getUrl(), is(URL));
        assertThat(documentWithPopulatedBytes.getBytes(), is(documentContent));
        // make sure it's immutable
        assertThat(documentInfo, not(sameInstance(documentWithPopulatedBytes)));
    }
}
