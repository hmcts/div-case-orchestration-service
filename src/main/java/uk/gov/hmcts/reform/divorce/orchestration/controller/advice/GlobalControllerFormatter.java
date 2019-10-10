package uk.gov.hmcts.reform.divorce.orchestration.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorcePartyNotFoundException;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
@Slf4j
/**
 * This class is responsible for transforming "primitive" values into object before they ever get to our Spring-annotated methods
 * (i.e. transforming a String into an Enum before it is received as a parameter by a controller)
 */
public class GlobalControllerFormatter {

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {

        webdataBinder.registerCustomEditor(DivorceParty.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(final String text) throws IllegalArgumentException {
                try {
                    setValue(DivorceParty.getDivorcePartyByDescription(text));
                } catch (DivorcePartyNotFoundException exception) {
                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
            }
        });

    }

}
