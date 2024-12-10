package top.tslj.dsb2s;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoProperties.class)
@ConditionalOnProperty(prefix = "demo", name = "enabled", matchIfMissing = true)
public class DemoConfiguration {
    @Bean
    public IService iService(DemoProperties demoProperties) {
        return new IServiceImpl(demoProperties);
    }
}