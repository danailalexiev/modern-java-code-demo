package com.example.accounts.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;

@AnalyzeClasses(packages = "com.example.accounts")
class OutcomeArchitectureTest {

    @ArchTest
    static final ArchRule noBusinessExceptionsInServices =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..exception..");

    @ArchTest
    static final ArchRule errorTypesAreSealed =
        classes().that().haveSimpleNameEndingWith("Error").and().areInterfaces()
            .should(beSealed());

    @ArchTest
    static final ArchRule noExceptionHandlersInControllers =
        noMethods().that().areDeclaredInClassesThat().resideInAPackage("..api..")
            .should().beAnnotatedWith(ExceptionHandler.class);

    static ArchCondition<JavaClass> beSealed() {
        return new ArchCondition<>("be sealed") {
            @Override
            public void check(JavaClass c, ConditionEvents events) {
                boolean sealed = c.reflect().isSealed();
                events.add(new SimpleConditionEvent(c, sealed,
                    c.getName() + (sealed ? " is" : " is not") + " sealed"));
            }
        };
    }
}
