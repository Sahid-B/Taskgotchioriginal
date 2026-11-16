package ec.edu.espe.pa.taskgotchi.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Bean(name = "taskgotchiExecutor")
    public Executor taskgotchiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("tg-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        executor.initialize();
        return executor;
    }

    static class ContextPropagatingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                Map<String, String> previousMdc = MDC.getCopyOfContextMap();
                SecurityContext previousSecurity = SecurityContextHolder.getContext();
                try {
                    if (mdc != null) {
                        MDC.setContextMap(mdc);
                    } else {
                        MDC.clear();
                    }
                    SecurityContextHolder.setContext(copySecurityContext(securityContext));
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                    MDC.clear();
                    if (previousSecurity != null) {
                        SecurityContextHolder.setContext(previousSecurity);
                    }
                    if (previousMdc != null) {
                        MDC.setContextMap(previousMdc);
                    }
                }
            };
        }

        private SecurityContext copySecurityContext(SecurityContext source) {
            SecurityContext copy = SecurityContextHolder.createEmptyContext();
            if (source != null) {
                copy.setAuthentication(source.getAuthentication());
            }
            return copy;
        }
    }
}
