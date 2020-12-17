package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class DocumentTypeTest {

    @Test
    public void shouldRetrieveDocumentTypeByTemplateName() {
        Optional<DocumentType> coe = DocumentType.getEnum("costsOrderTemplateId");
        assertThat("match", coe.get(), is(DocumentType.COSTS_ORDER_TEMPLATE_ID));
    }

    @Test
    public void testInvalid() {
        Optional<DocumentType> unknown = DocumentType.getEnum("unknown");
        assertFalse("match not found", unknown.isPresent());
    }

}