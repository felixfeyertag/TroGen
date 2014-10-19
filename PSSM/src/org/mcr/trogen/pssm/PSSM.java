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

package org.mcr.trogen.pssm;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismAlgorithmParameters;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismResult;
import org.mcr.trogen.utils.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * @filename PSSM.java
 * @date 06-Aug-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
@ServiceProvider(service=TropismAlgorithm.class)
public class PSSM implements TropismAlgorithm {

    TropismModel model;
    double[][] llRatio;
    
    @Override
    public String getAlgorithmName() {
        return "PSSM";
    }

    @Override
    public TropismAlgorithmParameters getAlgorithmParameters() {
        return new TropismAlgorithmParameters();
    }

    @Override
    public Sequence classifySequence(Sequence seq) throws TropismAlgorithmException {
        double logLikelihoodRatio = 0.0;
        for (int i=0; i<seq.getMappedAlignment().length(); i++) {
            char residue = seq.getMappedAlignment().charAt(i);
            int position = Utilities.AATABLE.length;
            for (int j=0; j<Utilities.AATABLE.length; j++) {
                if (Utilities.AATABLE[j]==residue) {
                    position = j;
                    break;
                }
            }
            logLikelihoodRatio += llRatio[i][position];
        }
        
        TropismResult result = new TropismResult();
        result.setScore(logLikelihoodRatio);
        //result.setFpr(0.5);
        try {
            result.setFpr(model.getFPRforScore(logLikelihoodRatio));
        }
        catch (NullPointerException e) {
            result.setFpr(Double.NaN);
        }
        seq.addTropismCall("PSSM", result);
        
        return seq;
    }

    @Override
    public TropismModel trainModel(TropismAlgorithmParameters params, List<Sequence> r5Training, List<Sequence> x4Training, List<Sequence> r5validation, List<Sequence> x4validation) {
        
        double[][] r5FreqMatrix = normalisedFreq(r5Training);
        double[][] x4FreqMatrix = normalisedFreq(x4Training);
        double[][] ratioMatrix = frequencyRatios(r5FreqMatrix, x4FreqMatrix);
        
        llRatio = ratioMatrix;
        
        double[] r5ValidationCalls = new double[r5validation.size()];
        int i=0;
        for (Sequence s : r5validation) {
            try {
                r5ValidationCalls[i] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
                //System.out.println("R5: " + r5ValidationCalls[i]);
                i++;
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(PSSM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        double[] x4ValidationCalls = new double[x4validation.size()];
        i=0;
        for (Sequence s : x4validation) {
            try {
                x4ValidationCalls[i] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
                //System.out.println("X4: " + x4ValidationCalls[i]);
                i++;
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(PSSM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        model = new TropismModel();
        LinkedList<double[][]> rmList = new LinkedList<double[][]> ();
        rmList.add(ratioMatrix);
        model.put("TrainingMatrix", rmList);
        model.setR5ValidationScores(r5ValidationCalls);
        model.setX4ValidationScores(x4ValidationCalls);
        model.calculateFPR();
        
        return model;
    }

    @Override
    public void loadModel(TropismModel model) {
        llRatio = ((LinkedList<double[][]>) model.get("TrainingMatrix")).get(0);
        this.model = model;
    }
    
    private double[][] frequencyRatios(double[][] r5ResidueFreq, double[][] x4ResidueFreq) {
        double[][] trainingRatios = new double[r5ResidueFreq.length][r5ResidueFreq[0].length];
        for (int i=0; i<r5ResidueFreq.length; i++) {
            for (int j=0; j<r5ResidueFreq[0].length; j++) {
                trainingRatios[i][j] = Math.log(r5ResidueFreq[i][j]/x4ResidueFreq[i][j]);
            }
        }
        return trainingRatios;
    }

    private double[][] normalisedFreq(List<Sequence> sequences) {
        int length = sequences.get(0).getMappedAlignment().length();
        int resCount = Utilities.AATABLE.length + 1;
        int seqCount = sequences.size();
        double[][] residueCount = residueCount(sequences);
        double[][] frequency = new double[length][resCount];
        for (int i=0; i<length; i++) {
            for (int j=0; j<resCount; j++) {
                frequency[i][j] = residueCount[i][j] / ((double)seqCount+(double)resCount);
            }
        }
        return frequency;
    }
    
    private double[][] residueCount(List<Sequence> sequences) {
        int length = sequences.get(0).getMappedAlignment().length();
        double[][] residueCount = new double[length][Utilities.AATABLE.length+1];
        for (int i=0; i<length; i++) {
            for (int j=0; j<=Utilities.AATABLE.length; j++) {
                residueCount[i][j] = 1;
            }
            for (Sequence s : sequences) {
                int pos = Utilities.AATABLE.length;
                char res = s.getMappedAlignment().charAt(i);
                for (int j=0; j<Utilities.AATABLE.length; j++) {
                    if (Utilities.AATABLE[j]==res) {
                        pos = j;
                        break;
                    }
                }
                residueCount[i][pos] += 1;
            }
        }
        return residueCount;
    }

}
