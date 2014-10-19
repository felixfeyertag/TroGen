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

package org.mcr.trogen.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mcr.trogen.core.TroGenCoreScorer.ScoringMetric;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismAlgorithmParameters;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismResult;
import org.mcr.trogen.utils.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * @filename TroGenCore.java
 * @date 09-Sep-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
@ServiceProvider(service=TropismAlgorithm.class)
public class TroGenCore implements TropismAlgorithm {

    private final TroGenCoreScorer scorer;
    private TropismModel trainingModel;
    private LinkedList<double[][]> trainingRatios;
    private LinkedList<double[][]> x4TransitionMatrices;
    private LinkedList<double[][]> r5TransitionMatrices;
    
    public TroGenCore() {
        scorer = new TroGenCoreScorer();
        scorer.setScoringParameter(50);
        scorer.put("Scoring Metric", ScoringMetric.DIST_LINEAR); 
        scorer.put("Window Count", 35);
    }
    
    @Override
    public String getAlgorithmName() {
        return "TroGen Core";
    }

    @Override
    public TropismAlgorithmParameters getAlgorithmParameters() {
        return scorer;
    }

    @Override
    public Sequence classifySequence(Sequence seq) throws TropismAlgorithmException {
        
        char[] seqArray = seq.getMappedAlignment().toCharArray();
        
        double llRatio = 0.0;
        double llR5 = 0.0;
        double llX4 = 0.0;
        
        int windows = (Integer) scorer.get("Window Count");
        int windowSize = (int) ((double)seqArray.length / (double)windows);
        
        for (int i=0; i<seqArray.length; i++) {
            char residue1 = seqArray[i];
            int res1Pos = getAAPosition(residue1);
            int position = (int) ((double)i/(double)windowSize);
                        
            if (position>windows) {
                System.out.println("warning: position > window: " + position);
                position = windows-1;
            }
            
            double[][] r5tm = this.r5TransitionMatrices.get(position);
            double[][] x4tm = this.x4TransitionMatrices.get(position);
            double[][] tmr = this.trainingRatios.get(position);
            
            //double ratioScore = 0.0;
            //double r5Score = 0.0;
            //double x4Score = 0.0;
            for (int j=0; j<seqArray.length; j++) {
                char residue2 = seqArray[j];
                int res2Pos = getAAPosition(residue2);
                double score = this.scorer.score(i, j);
                if (score>0) {
                    //System.out.println("R5\t" + res1Pos + ":" + res2Pos + "\t" + score + "\t" + r5tm[res1Pos][res2Pos] + "\t" + r5tm[res1Pos][res2Pos]*score + "\t" + Math.log(Math.pow(r5tm[res1Pos][res2Pos], 1.0-score)));
                    //System.out.println("X4\t" + res1Pos + ":" + res2Pos + "\t" + score + "\t" + x4tm[res1Pos][res2Pos] + "\t" + x4tm[res1Pos][res2Pos]*score + "\t" + Math.log(Math.pow(x4tm[res1Pos][res2Pos], 1.0-score)));
                    //llR5 += r5tm[res1Pos][res2Pos] * score;
                    //llX4 += x4tm[res1Pos][res2Pos] * score;
                    //llRatio += Math.log(llR5/llX4);
                    //llR5 += Math.log(Math.pow(r5tm[res1Pos][res2Pos], 1.0-score));
                    //llX4 += Math.log(Math.pow(x4tm[res1Pos][res2Pos], 1.0-score));
                    //llRatio += (llR5-llX4);
                    llRatio += tmr[res1Pos][res2Pos]*score;
                }
            }
        }

        TropismResult result = new TropismResult();
        result.setScore(llRatio);
        try {
            result.setFpr(trainingModel.getFPRforScore(llRatio));
        }
        catch (NullPointerException e) {
            result.setFpr(Double.NaN);
        }
        seq.addTropismCall("TroGen Core", result);
        
        return seq;
    }

    @Override
    public TropismModel trainModel(TropismAlgorithmParameters params, List<Sequence> r5training, List<Sequence> x4training, List<Sequence> r5validation, List<Sequence> x4validation) {
        trainingModel = new TropismModel();
        assert (r5training.size()>0 && x4training.size()>0);
        int seqLength = r5training.get(0).getMappedAlignment().length();
        /*double[] startingProbability = new double[Utilities.AATABLE.length + 1];
        for (int i=0; i<startingProbability.length; i++) {
            startingProbability[i] = 1.0;
        }*/
        r5TransitionMatrices = new LinkedList<double[][]>();
        int windows = (Integer) scorer.get("Window Count");
        int windowSize = (int) ((double)seqLength / (double)windows);
        //System.out.println("window count: " + windows + " window size: " + windowSize);
        for (int i=0; i<windows; i++) {
            int start = i*windowSize;
            if (start>seqLength) {
                System.out.println("start>seqLength");
                break;
            }
            int length = windowSize;
            if (i==windows-1) {
                if (length>seqLength-start) {
                    length = seqLength - start;
                }
                else {
                    length += seqLength % windowSize;
                }
            }
            r5TransitionMatrices.add(createTransitionMatrix(start, length, r5training));
        }
        x4TransitionMatrices = new LinkedList<double[][]>();
        //System.out.println("window count: " + windows + " window size: " + windowSize);
        for (int i=0; i<windows; i++) {
            int start = i*windowSize;
            if (start>seqLength) {
                System.out.println("start>seqLength");
                break;
            }
            int length = windowSize;
            if (i==windows-1) {
                if (length>seqLength-start) {
                    length = seqLength - start;
                }
                else {
                    length += seqLength % windowSize;
                }
            }
            x4TransitionMatrices.add(createTransitionMatrix(start, length, x4training));
        }
        trainingModel.put("R5TrainingMatrix", r5TransitionMatrices);
        trainingModel.put("X4TrainingMatrix", x4TransitionMatrices);
        
        trainingRatios = new LinkedList<double[][]>();
        
        assert (x4TransitionMatrices.size()==r5TransitionMatrices.size());
        
        for (int i=0; i<x4TransitionMatrices.size(); i++) {
            double[][] x4tm = x4TransitionMatrices.get(i);
            double[][] r5tm = r5TransitionMatrices.get(i);
            double[][] ratio = new double[x4tm.length][x4tm[0].length];
            for (int j=0; j<x4tm.length; j++) {
                for (int k=0; k<x4tm[0].length; k++) {
                    ratio[j][k] = r5tm[j][k] / x4tm[j][k];
                }
            }
            trainingRatios.add(ratio);
        }
        
        
        double[] r5ValidationCalls = new double[r5validation.size()];
        int i=0;
        for (Sequence s : r5validation) {
            try {
                r5ValidationCalls[i] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
                //System.out.println("R5: " + r5ValidationCalls[i]);
                i++;
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(TroGenCore.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(TroGenCore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //trainingModel = new TropismModel();
        trainingModel.setR5ValidationScores(r5ValidationCalls);
        trainingModel.setX4ValidationScores(x4ValidationCalls);
        trainingModel.calculateFPR();
        trainingModel.put("TrainingRatios", trainingRatios);
        return trainingModel;
    }

    @Override
    public void loadModel(TropismModel model) {
        this.r5TransitionMatrices = (LinkedList<double[][]>) model.get("R5TrainingMatrix");
        this.x4TransitionMatrices = (LinkedList<double[][]>) model.get("X4TrainingMatrix");
        this.trainingRatios = (LinkedList<double[][]>) model.get("TrainingRatios");
        this.trainingModel = model;
    }
    
    private double[][] createTransitionMatrix (int position, int windowSize, List<Sequence> sequences) {
        double[][] totalFrequency = new double[Utilities.AATABLE.length+1][Utilities.AATABLE.length+1];
        for (Sequence s : sequences) {
            double[][] frequency = new double[Utilities.AATABLE.length+1][Utilities.AATABLE.length+1];
            double[] transitions = new double[Utilities.AATABLE.length+1];
            double totalScore = 0.0;
            for (int i=position; i<position+windowSize; i++) {
                for (int j=0; j<s.getMappedAlignment().length(); j++) {
                    totalScore += scorer.score(i, j);
                }
                char res1 = s.getMappedAlignment().charAt(i);
                int res1Position = getAAPosition(res1);
                for (int j=0; j<s.getMappedAlignment().length(); j++) {
                    char res2 = s.getMappedAlignment().charAt(j);
                    int res2Position = getAAPosition(res2);
                    if (totalScore>0) {
                        double score = scorer.score(i, j) / totalScore;
                        frequency[res1Position][res2Position] += 1.0*score;
                        transitions[res1Position] += score;
                    }
                    else {
                        //System.err.println("createTransitionMatrix totalScore: " + totalScore + " i: " + i + " j: " + j);
                    }
                }
            }
            // normalise for window length
            for (int i=0; i<frequency.length; i++) {
                for (int j=0; j<frequency[0].length; j++) {
                    frequency[i][j] /= (double) windowSize;
                }
                transitions[i] /= (double) windowSize;
            }
            // normalise all rows to sum to one
            for (int i=0; i<frequency.length; i++) {
                double rowSum = 0;
                for (int j=0; j<frequency[0].length; j++) {
                    rowSum += (frequency[i][j]+1.0);//*(transitions[j]);
                }
                //System.out.println("rowSum: " + rowSum);
                //rowSum += (21.0);
                for (int j=0; j<frequency[0].length; j++) {
                    frequency[i][j] = (frequency[i][j]+1.0)/rowSum;//(frequency[i][j]+1.0) / (transitions[i] + totalScore + 1.0);
                }
            }
            // set global frequency counter
            for (int i=0; i<totalFrequency.length; i++) {
                for (int j=0; j<totalFrequency[0].length; j++) {
                    totalFrequency[i][j] += frequency[i][j];
                }
            }
        }
        for (int i=0; i<totalFrequency.length; i++) {
            for (int j=0; j<totalFrequency[0].length; j++) {
                totalFrequency[i][j] /= (double) sequences.size();
                System.out.print(totalFrequency[i][j]+"\t");
            }
            System.out.println();
        }
        return totalFrequency;
    }

    private int getAAPosition(char res1) {
        int pos = Utilities.AATABLE.length;
        for (int i=0; i<Utilities.AATABLE.length; i++) {
            if (res1==Utilities.AATABLE[i]) {
                pos = i;
            }
        }
        if (pos==Utilities.AATABLE.length) {
            //System.out.println("Could not find character: " + res1);
        }
        return pos;
    }
    
}
