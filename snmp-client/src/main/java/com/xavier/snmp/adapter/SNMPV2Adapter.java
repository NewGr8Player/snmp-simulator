package com.xavier.snmp.adapter;

import com.xavier.snmp.entity.SNMPBasic;
import com.xavier.snmp.entity.SNMPV2;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Vector;

/**
 * SNMP V1 Adapter
 *
 * @author NewGr8Player
 */
@Slf4j
public class SNMPV2Adapter extends SNMPV2 implements SNMPAdapter {

    @Override
    public Vector<? extends VariableBinding> get() {
        String url = getUrl();
        DefaultUdpTransportMapping udpTransportMapping = null;
        Snmp snmp = null;
        try {
            udpTransportMapping = new DefaultUdpTransportMapping();
            udpTransportMapping.listen();
            snmp = new Snmp(udpTransportMapping);
            CommunityTarget target = simpleCommunityTarget(SnmpConstants.version1,new OctetString(community));
            PDU pdu = simplePDU(PDU.GET);
            mibList.forEach(
                    e -> pdu.add(new VariableBinding(new OID(e)))
            );
            ResponseEvent response = snmp.send(pdu, target);
            if (null == response || null == response.getResponse()) {
                log.error("[{}] -> SNMP request timed out", url);
                return null;
            } else {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> variableBindings = responsePDU.getVariableBindings();
                    return variableBindings;
                } else {
                    log.error("[{}] -> SNMP Error: {}", url, responsePDU.getErrorStatusText());
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                    snmp = null;
                } catch (IOException e) {
                    snmp = null;
                }
            }

            if (udpTransportMapping != null) {
                try {
                    udpTransportMapping.close();
                    udpTransportMapping = null;
                } catch (IOException e) {
                    udpTransportMapping = null;
                }
            }
        }
        return null;
    }

    @Override
    public Vector<? extends VariableBinding> getBulk() {
        return null;
    }

    @Override
    public Vector<? extends VariableBinding> getNext() {
        return null;
    }
}
