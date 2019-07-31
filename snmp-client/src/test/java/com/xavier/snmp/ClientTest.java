package com.xavier.snmp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SnmpClientApplication.class})
public class ClientTest {

    public static String ip = "127.0.0.1";
    public static int port = 161;
    public static List<String> mibList = Arrays.asList(
            ".1.3.6.1.2.1.2.2.1.11.1 "
            , ".1.3.6.1.2.1.2.2.1.10.1"
            , ".1.3.6.1.2.1.3.1.1.3.2.1.192.168.104.1"
            , ".1.3.6.1.2.1.25.1.4.0"
            , ".1.3.6.1.4.1.6027.3.26.1.3.4.1.9"
    );
    public static String community = "public";

    @Test
    public void basicTest() {

        String url = "udp:" + ip + "/" + port;
        DefaultUdpTransportMapping udpTransportMapping = null;
        Snmp snmp = null;
        try {
            udpTransportMapping = new DefaultUdpTransportMapping();
            udpTransportMapping.listen();
            snmp = new Snmp(udpTransportMapping);
            // V2协议
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community)); // Community name
            target.setVersion(SnmpConstants.version2c); // Protocol version
            target.setAddress(GenericAddress.parse(url));
            target.setRetries(3); // Retry times
            target.setTimeout(1500); // Timeout
            PDU pdu = new PDU();
            pdu.setType(PDU.GET);// Method
            mibList.forEach(
                    e -> pdu.add(new VariableBinding(new OID(e)))
            );
            ResponseEvent response = snmp.send(pdu, target);
            if (null == response || null == response.getResponse()) {
                log.error("[{}] -> SNMP request timed out", url);
            } else {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> variableBindings = responsePDU.getVariableBindings();
                    System.out.println("----");
                    variableBindings.forEach(System.out::println);
                    System.out.println("----");
                } else {
                    log.error("[{}] -> SNMP Error: {}", url, responsePDU.getErrorStatusText());
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
    }

    @Test
    public void basicV3Test() {
        Auth auth = Auth.MD5;
        Privacy privacy = Privacy.AES256;
        String engineId = "";
        String usmUser = "";
        String authPassword = "";
        String privacyPassword = "";
        Level securityLevel = Level.NOAUTH_NOPRIV;
        String url = "udp:" + ip + "/" + port;
        DefaultUdpTransportMapping udpTransportMapping = null;
        Snmp snmp = null;
        try {
            udpTransportMapping = new DefaultUdpTransportMapping();
            udpTransportMapping.listen();
            snmp = new Snmp(udpTransportMapping);
            // 如果设备类型是V3
            USM usm = new USM(
                    SecurityProtocols.getInstance()
                    , new OctetString(MPv3.createLocalEngineID())
                    , 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            UsmUser usmUserObj = null;
            OID authenticationProtocol = null;
            OID privacyProtocol = null;
            OctetString localizationEngineID = null;
            switch (auth) {
                case MD5:
                    authenticationProtocol = AuthMD5.ID;
                    break;
                case SHA:
                    authenticationProtocol = AuthSHA.ID;
                    break;
                default:
                    throw new IllegalArgumentException("No such auth name.");
            }
            switch (privacy) {
                case DES:
                    privacyProtocol = PrivDES.ID;
                    break;
                case AES:
                    privacyProtocol = PrivAES128.ID;
                    break;
                case AES192:
                    privacyProtocol = PrivAES192.ID;
                    break;
                case AES256:
                    privacyProtocol = PrivAES256.ID;
                    break;
                default:
                    throw new IllegalArgumentException("No such privacy name.");
            }
            if (StringUtils.isNotBlank(engineId)) {
                localizationEngineID = new OctetString(engineId);
            }
            usmUserObj = new UsmUser(
                    new OctetString(usmUser)
                    , authenticationProtocol
                    , new OctetString(authPassword)
                    , privacyProtocol
                    , new OctetString(privacyPassword)
                    , localizationEngineID
            );
            snmp.getUSM().addUser(new OctetString(usmUser), usmUserObj);

            UserTarget userTarget = new UserTarget();
            userTarget.setVersion(SnmpConstants.version3);
            userTarget.setSecurityName(new OctetString(usmUser));
            switch (securityLevel) {
                case NOAUTH_NOPRIV: // 1: no auth, no priv(无验证,无加密)
                    userTarget.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
                    break;
                case AUTH_NOPRIV: // 2: auth, no priv(有验证,无加密)
                    userTarget.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
                    break;
                case AUTH_PRIV: // 3: auth, priv(有验证,y有加密)
                    userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                    break;
                default:
                    break;
            }
            userTarget.setRetries(3);
            userTarget.setTimeout(1500);// MS
            userTarget.setAddress(GenericAddress.parse(url));
            ScopedPDU pduV3 = new ScopedPDU();// V3独有的类型
            pduV3.setType(PDU.GET);
            mibList.forEach(
                    mib -> pduV3.add(new VariableBinding(new OID(mib)))
            );
            ResponseEvent response = snmp.send(pduV3, userTarget);
            if (null != response || null != response.getResponse()) {
                log.error("[{}] -> SNMP request timed out", url);
            } else {
                PDU responsePDU = response.getResponse();
                if (responsePDU.getErrorStatus() == PDU.noError) {
                    Vector<? extends VariableBinding> variableBindings = responsePDU.getVariableBindings();
                    System.out.println("----");
                    variableBindings.forEach(System.out::println);
                    System.out.println("----");
                } else {
                    log.error("[{}] -> SNMP Error: {}", url, responsePDU.getErrorStatusText());
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
    }

    enum Auth {
        MD5, SHA
    }

    enum Privacy {
        DES, AES, AES192, AES256
    }

    enum Level {
        NOAUTH_NOPRIV, AUTH_NOPRIV, AUTH_PRIV
    }

    public static Auth getAuth(String auth) {
        switch (auth.toUpperCase()) {
            case "MD5":
                return Auth.MD5;
            case "SHA":
                return Auth.SHA;
            default:
                return null;
        }
    }

    /**
     * 获取验证级别
     * 1 ~ NOAUTH_NOPRIV
     * 2 ~ AUTH_NOPRIV
     * 3 ~ AUTH_PRIV
     *
     * @param level
     * @return
     */
    public static Level getLevel(int level) {
        switch (level) {
            case 1:
                return Level.NOAUTH_NOPRIV;
            case 2:
                return Level.AUTH_NOPRIV;
            case 3:
                return Level.AUTH_PRIV;
            default:
                return null;
        }
    }

    public static Privacy getPrivacy(String privacy) {
        switch (privacy.toUpperCase()) {
            case "DES":
                return Privacy.DES;
            case "AES":
                return Privacy.AES;
            case "AES192":
                return Privacy.AES192;
            case "AES256":
                return Privacy.AES256;
            default:
                return null;
        }
    }

    @Test
    public void commonTest(){

    }
}
