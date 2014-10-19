/*
 * Copyright (C) 2014 The University of Manchester
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2.0 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.mcr.trogen.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * @filename Sequence.java
 * @date 17-Jul-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
public class Sequence implements Serializable {
    
    private static final long serialVersionUID = 7526477295622776148L;
    
    private String name;
    private String sequence;
    private String[] alignment;
    private String mappedAlignment;
    private String uniqueIdentifier;
    private Map<String,TropismResult> tropismCall;
    
    private String v3SequenceExclInsertions;
    
    public Sequence() {
        uniqueIdentifier = UUID.randomUUID().toString();
        name = "";
        sequence = "";
        tropismCall = new HashMap<String,TropismResult> ();
    }
    
    public void pairwiseAlignTo2B4C () {
        String seq2B4C = "CTRPNQNTRKSIHIGPGRAFYTTGEIIGDIRQAHC";
        alignment = Utilities.pairwiseAlign(seq2B4C, sequence);
        mappedAlignment = Utilities.mapAlignment(alignment);
    }
    
    public String getMappedAlignment() {
        return mappedAlignment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
    
    public String getIdentifier () {
        return uniqueIdentifier;
    }
    
    public void setIdentifier (String id) {
        uniqueIdentifier = id;
    }
    
    public void addTropismCall(String tropismAlgorithm, TropismResult call) {
        tropismCall.put(tropismAlgorithm, call);
    }
    
    public Map<String,TropismResult> getTropismCall() {
        return tropismCall;
    }

    public void translateAndExtractV3() {
        sequence = Utilities.translateDNAExtractV3(sequence);
    }

}
