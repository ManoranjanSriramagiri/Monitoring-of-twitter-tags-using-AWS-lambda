package myHash;

import myHash.config.DispatcherConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Varun on 5/10/2015.
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages ="myHash")
@EnableScheduling
@Import({ WebInitializer.class, DispatcherConfig.class})
public class AppConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(AppConfiguration.class, args);
    }
}
