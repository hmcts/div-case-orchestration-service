package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

//https://github.com/tomakehurst/wiremock/issues/485#issuecomment-382221826
public class ConnectionCloseExtension extends ResponseTransformer {
    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        return Response.Builder
            .like(response)
            .headers(HttpHeaders.copyOf(response.getHeaders())
                .plus(new HttpHeader("Connection", "Close")))
            .build();
    }

    @Override
    public String getName() {
        return "ConnectionCloseExtension";
    }
}
