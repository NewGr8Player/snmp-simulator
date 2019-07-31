package com.xavier.snmp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class SNMPBasic {

    /**
     * Name of protocol, default {@code udp}
     */
    protected String protocol = "udp";

    /**
     * Host ip or domain, default {@code 127.0.0.1}.
     */
    protected String host = "127.0.0.1";

    /**
     * Port, default {@code 161}.
     */
    protected int port = 161;

    /**
     * Name of community, default {@code public}.
     */
    protected String community = "public";

    /**
     * Mib(Oid) list,default empty list.
     */
    protected List<String> mibList = new LinkedList<>();

    public String getUrl(){
        return protocol + ":" + host + "/" + port;
    }

    protected CommunityTarget simpleCommunityTarget(int version, OctetString community, int retries, long timeout) {
        CommunityTarget ct = new CommunityTarget();
        ct.setCommunity(community);
        ct.setVersion(version);
        ct.setAddress(GenericAddress.parse(getUrl()));
        ct.setRetries(retries);
        ct.setTimeout(timeout);
        return ct;
    }

    protected CommunityTarget simpleCommunityTarget(int version, OctetString community) {
        return simpleCommunityTarget( version, community, 3, 3000);
    }

    protected PDU simplePDU(int method) {
        PDU pdu = new PDU();
        pdu.setType(method);
        return pdu;
    }

}
