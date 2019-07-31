package com.xavier.snmp.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SimulatorConfig
 *
 * @author NewGr8Player
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@ConfigurationProperties(prefix = "xavier.snmp")
public class SimulatorConfig {

    List<SimulatorBean> configs;

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulatorBean {

        private String alias;

        private String device;

        private String ip;

        private String port;

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("alias", alias)
                    .append("device", device)
                    .append("ip", ip)
                    .append("port", port)
                    .toString();
        }
    }
}
