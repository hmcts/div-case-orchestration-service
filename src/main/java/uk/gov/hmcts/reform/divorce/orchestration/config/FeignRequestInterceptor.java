package uk.gov.hmcts.reform.divorce.orchestration.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.reform.logging.HttpHeaders;
import uk.gov.hmcts.reform.logging.MdcFields;
import uk.gov.hmcts.reform.logging.tracing.RequestIdGenerator;

import java.util.function.Supplier;

public class FeignRequestInterceptor implements RequestInterceptor {

    private Supplier<String> nextRequestId;

    @Override
    public void apply(RequestTemplate template) {
        template.header(HttpHeaders.REQUEST_ID, nextRequestId.get());
        template.header(HttpHeaders.ROOT_REQUEST_ID, MdcFields.getRootRequestId());
        template.header(HttpHeaders.ORIGIN_REQUEST_ID, MdcFields.getRequestId());
    }

    public FeignRequestInterceptor() {
        this(RequestIdGenerator::next);
    }

    public FeignRequestInterceptor(Supplier<String> nextRequestId) {
        this.nextRequestId = nextRequestId;
    }
}
