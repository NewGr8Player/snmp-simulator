package com.xavier.snmp.agent;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.agent.*;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.agent.mo.DefaultMOTable;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SnmpAgent extends BaseAgent {

    protected OID sysOID = new OID("1.2");

    private List<DefaultMOTable> moTables = new ArrayList<>();

    private String community;

    public SnmpAgent(File bootCounterFile, File configFile, List<DefaultMOTable> moTables, String community) {
        super(bootCounterFile, configFile, new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
        this.moTables = moTables;
        this.community = community;
    }

    /**
     * Creates a base agent with a {@link DefaultMOServer} as {@link MOServer}.
     * To use a different server implementation, modify the {@link #server} member
     * after construction.
     *
     * @param configURI the URI of the config file holding persistent data for this agent. If
     *                  persistent data is not used then set this parameter to
     *                  {@code null}.
     */
    public SnmpAgent(String configURI) {
        super(configURI);
    }

    /**
     * Creates a base agent with boot-counter, config file, and a CommandProcessor
     * for processing SNMP requests.
     *
     * @param bootCounterFile  a file with serialized boot-counter information (read/write). If the
     *                         file does not exist it is created on shutdown of the agent.
     * @param configFile       a file with serialized configuration information (read/write). If the
     *                         file does not exist it is created on shutdown of the agent.
     * @param commandProcessor the {@code CommandProcessor} instance that handles the SNMP
     *                         requests.
     */
    public SnmpAgent(File bootCounterFile,
                     File configFile,
                     CommandProcessor commandProcessor) {
        super(bootCounterFile, configFile, commandProcessor);
    }

    /**
     * Register additional managed objects at the agent's server.
     */
    @Override
    protected void registerManagedObjects() {
        // 取消掉自带的乱七八糟的东西
        final OID startOID = new OID(".1");
        final DefaultMOContextScope hackScope = new DefaultMOContextScope(new OctetString(), startOID, true, startOID.nextPeer(), false);
        ManagedObject query;
        while ((query = server.lookup(new DefaultMOQuery(hackScope, false))) != null) {
            server.unregister(query, new OctetString());
        }
        final DefaultMOContextScope hackNullScope = new DefaultMOContextScope(null, startOID, true, startOID.nextPeer(), false);
        while ((query = server.lookup(new DefaultMOQuery(hackNullScope, false))) != null) {
            server.unregister(query, null);
        }

        try {
            for (DefaultMOTable table : moTables) {
                server.register(table, null);
            }
        } catch (DuplicateRegistrationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregister additional managed objects from the agent's server
     */
    @Override
    protected void unregisterManagedObjects() {

    }

    /**
     * Adds all the necessary initial users to the USM.
     *
     * @param usm the USM instance used by this agent.
     */
    @Override
    protected void addUsmUser(USM usm) {

    }

    /**
     * Adds initial notification targets and filters.
     *
     * @param snmpTargetMIB       the SnmpTargetMIB holding the target configuration.
     * @param snmpNotificationMIB the SnmpNotificationMIB holding the notification (filter)
     *                            configuration.
     */
    @Override
    protected void addNotificationTargets(SnmpTargetMIB snmpTargetMIB, SnmpNotificationMIB snmpNotificationMIB) {

    }

    /**
     * Adds initial VACM configuration.
     *
     * @param vacmMIB the VacmMIB holding the agent's view configuration.
     */
    @Override
    protected void addViews(VacmMIB vacmMIB) {

    }

    /**
     * Adds community to security name mappings needed for SNMPv1 and SNMPv2c.
     *
     * @param snmpCommunityMIB the SnmpCommunityMIB holding coexistence configuration for community
     *                         based security models.
     */
    @Override
    protected void addCommunities(SnmpCommunityMIB snmpCommunityMIB) {
        Variable[] com2sec = new Variable[]{
                new OctetString(community),                 /* community name */
                new OctetString("public"),       /* security name */
                getAgent().getContextEngineID(),            /* local engine ID */
                new OctetString(community),                 /* default context name */
                new OctetString(),                          /* transport tag */
                new Integer32(StorageType.nonVolatile),     /* storage type */
                new Integer32(RowStatus.active)             /* row status */
        };
        MOTableRow row = snmpCommunityMIB.getSnmpCommunityEntry()
                .createRow(
                        new OctetString("public").toSubIndex(true)
                        , com2sec
                );
        snmpCommunityMIB
                .getSnmpCommunityEntry()
                .addRow((SnmpCommunityMIB.SnmpCommunityEntryRow) row);
    }

    /**
     * Initializes the transport mappings (ports) to be used by the agent.
     *
     * @throws IOException if an IO exception occurs while initializing the default transport mapping on all local IP addresses on
     *                     port 161.
     */
    @Override
    protected void initTransportMappings() throws IOException {
        transportMappings = new DefaultUdpTransportMapping[]{
                new DefaultUdpTransportMapping(new UdpAddress("127.0.0.1/161"))};
    }

    public void startUp() {
        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.loadConfig(ImportModes.REPLACE_CREATE);
        this.addShutdownHook();
        this.getServer().addContext(new OctetString(community));
        this.finishInit();
        this.run();
        this.sendColdStartNotification();
    }
}
