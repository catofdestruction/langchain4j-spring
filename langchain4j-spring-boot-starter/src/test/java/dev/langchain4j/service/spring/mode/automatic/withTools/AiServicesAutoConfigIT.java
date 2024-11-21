package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.mode.automatic.withTools.aop.CustomAnnotationAspect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.ASPECT_PACKAGE;
import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.TOOL_DESCRIPTION;
import static dev.langchain4j.service.spring.mode.automatic.withTools.PackagePrivateTools.CURRENT_TIME;
import static dev.langchain4j.service.spring.mode.automatic.withTools.PublicTools.CURRENT_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiServicesAutoConfigIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_tool_which_is_public_method_in_public_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current date?");

                    // then should use PublicTools.getCurrentDate()
                    assertThat(answer).contains(String.valueOf(CURRENT_DATE.getDayOfMonth()));
                });
    }

    @Test
    void should_create_AI_service_with_tool_that_is_package_private_method_in_package_private_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current time?");

                    // then should use PackagePrivateTools.getCurrentTime()
                    assertThat(answer).contains(String.valueOf(CURRENT_TIME.getMinute()));
                });
    }

    @Test
    void should_create_AI_service_with_tool_which_is_enhanced_by_spring_aop() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("In Spring Boot, which package is the @Aspect annotation located in?");

                    // then should use AopEnhancedTools.getAspectPackage()
                    assertThat(answer).contains(ASPECT_PACKAGE);

                    // and AOP aspect should be enabled
                    CustomAnnotationAspect aspect = context.getBean(CustomAnnotationAspect.class);
                    assertTrue(aspect.isAspectEnabled());
                    assertTrue(aspect.getToolsDescription().contains(TOOL_DESCRIPTION));
                });
    }

    // TODO tools which are not @Beans?
    // TODO negative cases
    // TODO no @AiServices in app, just models
    // TODO @AiServices as inner class?
    // TODO streaming, memory, tools, etc
}