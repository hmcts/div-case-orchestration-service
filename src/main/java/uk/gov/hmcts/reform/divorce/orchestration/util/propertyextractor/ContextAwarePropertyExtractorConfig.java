package uk.gov.hmcts.reform.divorce.orchestration.util.propertyextractor;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Workflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

@Configuration
public class ContextAwarePropertyExtractorConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ContextAwarePropertyExtractor getContextAwarePropertyExtractor(DependencyDescriptor injectionPoint) {
        Member member = injectionPoint.getMember();
        Class<?> declaringClass = member.getDeclaringClass();

        if (Workflow.class.isAssignableFrom(declaringClass)) {
            return new WorkflowPropertyExtractor();//TODO - Make singleton
        } else if (Task.class.isAssignableFrom(declaringClass)) {
            return new TaskPropertyExtractor();//TODO - Make singleton
        } else {
//            ??
            return new TaskPropertyExtractor();//TODO - for now
        }
    }

}