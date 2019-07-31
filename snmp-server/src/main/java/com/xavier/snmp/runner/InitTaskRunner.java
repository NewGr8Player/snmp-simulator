package com.xavier.snmp.runner;

import com.xavier.snmp.agent.SnmpAgent;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.OID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * InitTaskRunner
 *
 * @author NewGr8player
 */
@Slf4j
@Component
public class InitTaskRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MOTableSubIndex[] subIndices = {new MOTableSubIndex(new OID("1.2.3.1"), 1)};
        MOTableIndex moTableIndex = new MOTableIndex(subIndices);
        MOColumn[] moColumns = {new MOMutableColumn(1, 1)};
        List<DefaultMOTable> moTableList = Arrays.asList(
                new DefaultMOTable(new OID(".1.2.3"), moTableIndex, moColumns)
        );
        new SnmpAgent(new File("F:\\temp\\bootCounterFile.cfg")
                , new File("F:\\temp\\counterFile.cfg")
                , moTableList
                , "public"
        ).startUp();
    }
}
