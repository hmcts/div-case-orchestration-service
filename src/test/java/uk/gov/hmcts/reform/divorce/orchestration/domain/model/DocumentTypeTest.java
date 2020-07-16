package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class DocumentTypeTest {

    @Test
    public void testCoe() {
        Optional<DocumentType> coe = DocumentType.getEnum("coe");
        assertThat("match", coe.get(), CoreMatchers.is(DocumentType.COE));
    }

    @Test
    public void testInvalid() {
        Optional<DocumentType> unknown = DocumentType.getEnum("unknown");
        assertFalse("match not found",  unknown.isPresent());
    }
}