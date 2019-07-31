package com.xavier.snmp.adapter;

import com.xavier.snmp.entity.SNMPBasic;
import org.snmp4j.smi.VariableBinding;

import java.util.Vector;

/**
 * SNMP适配器接口
 *
 * @author NewGr8Player
 */
public interface SNMPAdapter {

    /**
     * Get current.
     *
     * @return
     */
    Vector<? extends VariableBinding> get();

    /**
     * Get batch.
     *
     * @return
     */
    Vector<? extends VariableBinding> getBulk();

    /**
     * Get next.
     *
     * @return
     */
    Vector<? extends VariableBinding> getNext();
}
