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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @filename TropismModel.java
 * @date 30-Jul-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
public class TropismModel extends HashMap<String,Object> {
    private double[][] fpr;
    private double[] r5ValidationScores;
    private double[] x4ValidationScores;

    /*public double[][] getFpr() {
        return fpr;
    }

    public void setFpr(double[][] fpr) {
        this.fpr = fpr;
    }*/

    public double[] getR5ValidationScores() {
        return r5ValidationScores;
    }

    public void setR5ValidationScores(double[] r5ValidationScores) {
        this.r5ValidationScores = r5ValidationScores;
    }

    public double[] getX4ValidationScores() {
        return x4ValidationScores;
    }

    public void setX4ValidationScores(double[] x4ValidationScores) {
        this.x4ValidationScores = x4ValidationScores;
    }
    
    public void calculateFPR() {
        Arrays.sort(x4ValidationScores);
        Arrays.sort(r5ValidationScores);
        fpr = new double[r5ValidationScores.length][2];
        for (int i=0; i<r5ValidationScores.length; i++) {
            fpr[i][1] = ((double)i+1)/((double)r5ValidationScores.length);
            fpr[i][0] = r5ValidationScores[i];
        }
    }
    
    public double getScoreforFPR(double f) {
        double score = 0;
        double pscore = 0;
        
        for (int i=0; i<fpr.length; i++) {
            if (fpr[i][1]<f || Math.abs(fpr[i][1]-f)<0.001) {
                pscore = score;
                score = fpr[i][0];
            }
            else {
                break;
            }
        }
        
        return score - ((score-pscore)/2.0);
    }
    
    public Number[] getStatsforFPR(double f) {
        //Map<String,Number> stats = new HashMap<String,Number>();
        double score = getScoreforFPR(f);
        Integer tp = 0;
        Integer fp = 0;
        Integer tn = 0;
        Integer fn = 0;
        Double sen = 0.0;
        Double spe = 0.0;
        Double ppv = 0.0;
        Double npv = 0.0;
        for (double d : r5ValidationScores) {
            if (d<=score) {
                fp++;
            }
            else {
                tp++;
            }
        }
        for (double d : x4ValidationScores) {
            if (d<=score) {
                tn++;
            }
            else {
                fn++;
            }
        }
        sen = (double)tp / ((double)tp+(double)fn);
        spe = (double)fp / ((double)fp+(double)tn);
        ppv = (double)tp / ((double)tp+(double)fp);
        npv = (double)tn / ((double)tn+(double)fn);
        
        return new Number[] {score, tp, fp, tn, fn, sen, spe, ppv, npv};
    }

    public double getFPRforScore(double score) {
        double f = 0.0;
        for (int i=0; i<fpr.length; i++) {
            if (fpr[i][0]<score || Math.abs(fpr[i][0]-score)<0.001) {
                f = fpr[i][1];
            }
            else {
                break;
            }
        }
        return f;
    }
    
    public double[][] getValidationFPRs() {
        return fpr;
    }
}
