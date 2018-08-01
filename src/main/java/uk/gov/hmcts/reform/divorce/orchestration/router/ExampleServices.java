package uk.gov.hmcts.reform.divorce.orchestration.router;

/**
 * A Mock class to show how some other layer
 * (a persistence layer, for instance)
 * could be used inside Camel.
 */
public class ExampleServices {

    public static void example(MyBean bodyIn) {
        bodyIn.setName( "Hello, " + bodyIn.getName() );
        bodyIn.setId(bodyIn.getId() * 10);
    }
}
