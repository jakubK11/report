package com.example.reportingservice.config;

import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.listener.ProxyExecutionListener;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.time.Duration;

@Configuration
@Slf4j
public class DatabaseConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        // Intercept the auto-configured ConnectionFactory and wrap it with proxy
        if (bean instanceof ConnectionFactory connectionFactory && "connectionFactory".equals(beanName)) {
            log.info("Wrapping ConnectionFactory with r2dbc-proxy for SQL timing");
            return ProxyConnectionFactory.builder(connectionFactory)
                    .listener(new SqlTimingListener())
                    .build();
        }
        return bean;
    }

    private static class SqlTimingListener implements ProxyExecutionListener {

        @Override
        public void beforeQuery(QueryExecutionInfo execInfo) {
            // no action needed
        }
        
        @Override
        public void afterQuery(QueryExecutionInfo execInfo) {
            Duration executionTime = execInfo.getExecuteDuration();
            String query = extractQuery(execInfo);
            int rowCount = execInfo.getCurrentResultCount();
            
            // Check for errors
            if (execInfo.getThrowable() != null) {
                log.error("SQL failed after {} ms | Query: {} | Error: {}", 
                    executionTime.toMillis(),
                    sanitizeQuery(query),
                    execInfo.getThrowable().getMessage());
            } else {
                log.info("SQL executed in {} ms | Rows: {} | Query: {}", 
                    executionTime.toMillis(), 
                    rowCount,
                    sanitizeQuery(query));
            }
        }

        private String extractQuery(QueryExecutionInfo execInfo) {
            return execInfo.getQueries().isEmpty() ? 
                "unknown" : 
                execInfo.getQueries().get(0).getQuery();
        }

        private String sanitizeQuery(String query) {
            if (query == null) return "";
            // Remove extra whitespace and newlines for cleaner logs
            return query.replaceAll("\\s+", " ").trim();
        }
    }
}
