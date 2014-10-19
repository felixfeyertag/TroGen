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

package org.mcr.trogen.cr;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.Tropism;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismAlgorithmParameters;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismResult;
import org.openide.util.lookup.ServiceProvider;

/**
 * @filename ChargeRule.java
 * @date 17-Jul-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
@ServiceProvider(service=TropismAlgorithm.class)
public class ChargeRule implements TropismAlgorithm {

    private final String ALGORITHMNAME = "Charge Rule";
    private TropismModel model;
    
    @Override
    public String getAlgorithmName() {
        return ALGORITHMNAME;
    }

    @Override
    public TropismAlgorithmParameters getAlgorithmParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Sequence classifySequence(Sequence seq) throws TropismAlgorithmException {
        Tropism tropism = Tropism.R5;
        char pos11 = seq.getMappedAlignment().charAt(10);
        char pos24 = seq.getMappedAlignment().charAt(23);
        char pos25 = seq.getMappedAlignment().charAt(24);
        if (pos11=='R' || pos11=='r' || pos11=='K' || pos11=='k' || pos11=='H' || pos11=='h') {
            tropism = Tropism.X4;
        }
        if (pos24=='R' || pos24=='r' || pos24=='K' || pos24=='k' || pos24=='H' || pos24=='h') {
            tropism = Tropism.X4;
        }
        if (pos25=='R' || pos25=='r' || pos25=='K' || pos25=='k' || pos25=='H' || pos25=='h') {
            tropism = Tropism.X4;
        }
        TropismResult result;
        result = new TropismResult();
        
        if (tropism==Tropism.R5) {
            result.setScore(1);
        }
        else {
            result.setScore(-1);
        }
        
        if (model!=null) {
            result.setFpr(model.getFPRforScore(result.getScore()));
        }
        else {
            result.setFpr(0.5);
        }
        
        seq.addTropismCall(this.getAlgorithmName(), result);
        return seq;
    }

    @Override
    public TropismModel trainModel(TropismAlgorithmParameters params, List<Sequence> r5training, List<Sequence> x4training, List<Sequence> r5validation, List<Sequence> x4validation) {

        double[] r5ValidationCalls = new double[r5validation.size()];
        int i=0;
        for (Sequence s : r5validation) {
            try {
                r5ValidationCalls[i++] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(ChargeRule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        double[] x4ValidationCalls = new double[x4validation.size()];
        i=0;
        for (Sequence s : x4validation) {
            try {
                x4ValidationCalls[i++] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(ChargeRule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        model = new TropismModel();
        model.setR5ValidationScores(r5ValidationCalls);
        model.setX4ValidationScores(x4ValidationCalls);
        model.calculateFPR();
        return model;
    }

    @Override
    public void loadModel(TropismModel model) {
        this.model = model;
    }
}
