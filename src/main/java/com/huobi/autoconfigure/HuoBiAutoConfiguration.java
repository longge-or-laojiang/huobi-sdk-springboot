package com.huobi.autoconfigure;

import com.huobi.constant.HuobiConstants;
import com.huobi.constant.HuobiOptions;
import com.huobi.service.huobi.connection.HuobiRestConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author jiangfeilong
 */
@Slf4j
@ConditionalOnProperty(value = "huobi.autoconfigure.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HuobiConstants.class)
@ConfigurationPropertiesScan("com.huobi.constant")
@Configuration
@ComponentScan("com.huobi.service")
public class HuoBiAutoConfiguration {


    @Bean
    public HuobiRestConnection huobiRestConnection() {
        HuobiOptions options = new HuobiOptions();
        HuobiRestConnection huobiRestConnection = new HuobiRestConnection();
        huobiRestConnection.setOptions(options);
        try {
            huobiRestConnection.setHost(new URL(options.getRestHost()).getHost());
        } catch (MalformedURLException e) {
            log.error("In HuoBiAutoConfiguration.huobiRestConnection(), message: create huobiRestConnection bean " +
                    "fail, error:{}", e.getMessage());
            e.printStackTrace();
        }
        return huobiRestConnection;
    }
}
