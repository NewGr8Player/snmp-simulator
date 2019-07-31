package com.xavier.snmp.config;

import com.xavier.snmp.SimulatorServerTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SimulatorConfigTest extends SimulatorServerTest {

    @Autowired
    private SimulatorConfig simulatorConfig;

    @Test
    public void test() {
        simulatorConfig.getConfigs().forEach( System.out::println );
    }
}