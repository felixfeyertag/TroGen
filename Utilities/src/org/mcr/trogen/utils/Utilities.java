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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @filename Utilities.java
 * @date 17-Jul-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc 
 */
public class Utilities {
    public static final char[] AATABLE = 
     {'A','R','N','D','C','Q','E','G','H','I','L','K','M','F','P','S','T','W','Y','V'};
    public static final char[] NUCTABLE = 
     {'A', 'C', 'G', 'T'};
    private static final int[][] BLOSUM62 =
     {{ 4,-1,-2,-2, 0,-1,-1, 0,-2,-1,-1,-1,-1,-2,-1, 1, 0,-3,-2, 0},
     {-1, 5, 0,-2,-3, 1, 0,-2, 0,-3,-2, 2,-1,-3,-2,-1,-1,-3,-2,-3},
     {-2, 0, 6, 1,-3, 0, 0, 0, 1,-3,-3, 0,-2,-3,-2, 1, 0,-4,-2,-3},
     {-2,-2, 1, 6,-3, 0, 2,-1,-1,-3,-4,-1,-3,-3,-1, 0,-1,-4,-3,-3},
     { 0,-3,-3,-3, 9,-3,-4,-3,-3,-1,-1,-3,-1,-2,-3,-1,-1,-2,-2,-1},
     {-1, 1, 0, 0,-3, 5, 2,-2, 0,-3,-2, 1, 0,-3,-1, 0,-1,-2,-1,-2},
     {-1, 0, 0, 2,-4, 2, 5,-2, 0,-3,-3, 1,-2,-3,-1, 0,-1,-3,-2,-2},
     { 0,-2, 0,-1,-3,-2,-2, 6,-2,-4,-4,-2,-3,-3,-2, 0,-2,-2,-3,-3},
     {-2, 0, 1,-1,-3, 0, 0,-2, 8,-3,-3,-1,-2,-1,-2,-1,-2,-2, 2,-3},
     {-1,-3,-3,-3,-1,-3,-3,-4,-3, 4, 2,-3, 1, 0,-3,-2,-1,-3,-1, 3},
     {-1,-2,-3,-4,-1,-2,-3,-4,-3, 2, 4,-2, 2, 0,-3,-2,-1,-2,-1, 1},
     {-1, 2, 0,-1,-3, 1, 1,-2,-1,-3,-2, 5,-1,-3,-1, 0,-1,-3,-2,-2},
     {-1,-1,-2,-3,-1, 0,-2,-3,-2, 1, 2,-1, 5, 0,-2,-1,-1,-1,-1, 1},
     {-2,-3,-3,-3,-2,-3,-3,-3,-1, 0, 0,-3, 0, 6,-4,-2,-2, 1, 3,-1},
     {-1,-2,-2,-1,-3,-1,-1,-2,-2,-3,-3,-1,-2,-4, 7,-1,-1,-4,-3,-2},
     { 1,-1, 1, 0,-1, 0, 0, 0,-1,-2,-2, 0,-1,-2,-1, 4, 1,-3,-2,-2},
     { 0,-1, 0,-1,-1,-1,-1,-2,-2,-1,-1,-1,-1,-2,-1, 1, 5,-2,-2, 0},
     {-3,-3,-4,-4,-2,-2,-3,-2,-2,-3,-2,-3,-1, 1,-4,-3,-2,11, 2,-3},
     {-2,-2,-2,-3,-2,-1,-2,-3, 2,-1,-1,-2,-1, 3,-3,-2,-2, 2, 7,-1},
     { 0,-3,-3,-3,-1,-2,-2,-3,-3, 3, 1,-2, 1,-1,-2,-2, 0,-3,-1, 4}};
    private static final Map<String,String> translationTable;
    static {
        translationTable = new HashMap<String,String>();
        
        translationTable.put("TTT","F"); translationTable.put("TCT","S"); translationTable.put("TAT","Y"); translationTable.put("TGT","C" );
        translationTable.put("TTC","F"); translationTable.put("TCC","S"); translationTable.put("TAC","Y"); translationTable.put("TGC","C" );
        translationTable.put("TTA","L"); translationTable.put("TCA","S"); translationTable.put("TAA","*"); translationTable.put("TGA","*" );
        translationTable.put("TTG","L"); translationTable.put("TCG","S"); translationTable.put("TAG","*"); translationTable.put("TGG","W" );

        translationTable.put("CTT","L"); translationTable.put("CCT","P"); translationTable.put("CAT","H"); translationTable.put("CGT","C" );
        translationTable.put("CTC","L"); translationTable.put("CCC","P"); translationTable.put("CAC","H"); translationTable.put("CGC","C" );
        translationTable.put("CTA","L"); translationTable.put("CCA","P"); translationTable.put("CAA","Q"); translationTable.put("CGA","R" );
        translationTable.put("CTG","L"); translationTable.put("CCG","P"); translationTable.put("CAG","Q"); translationTable.put("CGG","R" );

        translationTable.put("ATT","I"); translationTable.put("ACT","T"); translationTable.put("AAT","N"); translationTable.put("AGT","S" );
        translationTable.put("ATC","I"); translationTable.put("ACC","T"); translationTable.put("AAC","N"); translationTable.put("AGC","S" );
        translationTable.put("ATA","I"); translationTable.put("ACA","T"); translationTable.put("AAA","K"); translationTable.put("AGA","R" );
        translationTable.put("ATG","M"); translationTable.put("ACG","T"); translationTable.put("AAG","K"); translationTable.put("AGG","R" );

        translationTable.put("GTT","V"); translationTable.put("GCT","A"); translationTable.put("GAT","D"); translationTable.put("GGT","G" );
        translationTable.put("GTC","V"); translationTable.put("GCC","A"); translationTable.put("GAC","D"); translationTable.put("GGC","G" );
        translationTable.put("GTA","V"); translationTable.put("GCA","A"); translationTable.put("GAA","E"); translationTable.put("GGA","G" );
        translationTable.put("GTG","V"); translationTable.put("GCG","A"); translationTable.put("GAG","E"); translationTable.put("GGG","G" );
    }
    private final static String v32B4C = "CTRPNQNTRKSIHIGPGRAFYTTGEIIGDIRQAHC";
    
    public static List<Sequence> readFasta(String filename) throws InvalidFASTAFileException {
        BufferedReader br = null;
        LinkedList<Sequence> sequences = new LinkedList<Sequence>();
        System.out.println("Opening: " + filename);
        try {
            String line;
            int i = 1;
            Sequence seq = null;
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                if (line.matches("^\\w*>.*")) {
                    if (seq!=null) {
                        sequences.add(seq);
                    }
                    seq = new Sequence();
                    line = line.replaceFirst(">", "");
                    seq.setName(line);
                    //seq.setIdentifier("Sequence" + i);
                    i++;
                }
                else if (seq!=null) {
                    seq.setSequence(seq.getSequence() + line);
                }
            }
            if (seq!=null) {
                sequences.add(seq);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) { ex.printStackTrace(); }
        }
        
        if (sequences.size()<=0) {
            throw new InvalidFASTAFileException();
        }
        
        return sequences;
    }
    
    public static String[] pairwiseAlign(String seq1, String seq2) {
        return pairwiseAlign(seq1, seq2, -3);
    }
    
    public static String[] pairwiseAlign(String seq1, String seq2, int gapPenalty) {
        int DIAG = 0;
        int LEFT = 1;
        int UP = 2;
        char[] seq1c = seq1.toCharArray();
        char[] seq2c = seq2.toCharArray();
        int n = seq1c.length;
        int m = seq2c.length;
        int[][] costTable = new int[n+1][m+1];
        int[][] traceback = new int[n+1][m+1];
        for (int i=0; i<=n; i++) {
            //Ends free alignment
            costTable[i][0] = 0; //i*gapPenalty;
            traceback[i][0] = UP;
        }
        for (int i=0; i<=m; i++) {
            //Ends free alignment
            costTable[0][i] = 0; //i*gapPenalty;
            traceback[0][i] = LEFT;
        }
        traceback[0][0] = -1;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                int cost1 = costTable[i-1][j-1] + cost(seq1c[i-1], seq2c[j-1], gapPenalty);
                int cost2 = costTable[i-1][j] + gapPenalty;
                int cost3 = costTable[i][j-1] + gapPenalty;
                costTable[i][j] = cost1;
                traceback[i][j] = DIAG;
                if (cost2>costTable[i][j]) {
                    costTable[i][j] = cost2;
                    traceback[i][j] = UP; 
                }
                if (cost3>costTable[i][j]) {
                    costTable[i][j] = cost3;
                    traceback[i][j] = LEFT;
                }
            }
        }
        int tbi = n;
        int tbj = m;
        // Ends free alignment
        for (int i=1; i<=m; i++) {
            if (costTable[n][i]>costTable[n][tbj]) {
                tbj = i;
            }
        }
        //
        String aln1 = "";
        String aln2 = "";
        while (tbi>=0 && tbj>=0) {
            if (traceback[tbi][tbj]==DIAG) {
                tbi--;
                tbj--;
                aln1 = seq1c[tbi] + aln1;
                aln2 = seq2c[tbj] + aln2;
            }
            else if (traceback[tbi][tbj]==UP) {
                tbi--;
                aln1 = seq1c[tbi] + aln1;
                aln2 = '-' + aln2;
            }
            else if (traceback[tbi][tbj]==LEFT) {
                tbj--;
                aln1 = '-' + aln1;
                aln2 = seq2c[tbj] + aln2;
            }
            else if (traceback[tbi][tbj]==-1) {
                break;
            }
        }
        
        System.out.println(aln1);
        System.out.println(aln2);
        System.out.println();
        
        return new String[] {aln1, aln2};
        
    }
    
    public static String mapAlignment(String[] alignment) {
        String retSeq = "";
        for (int i=0; i<alignment[0].length(); i++) {
            char r1 = alignment[0].charAt(i);
            char r2 = alignment[1].charAt(i);
            if (r1 != '-') {
                retSeq += r2;
            }
        }
        return retSeq;
    }
    
    public static int[] indexAlignment(String seq) {
        LinkedList<Integer> index = new LinkedList<Integer>();
        for (int i=0; i<seq.length(); i++) {
            char res = seq.charAt(i);
            if (res!='-') {
                index.add(i);
            }
        }
        int[] retIndex = new int[index.size()];
        int j=0;
        for (Integer i : index) {
            retIndex[j++] = i;
        }
        return retIndex;
    }
    
    public static Map<String,int[]> mapAlignmentNoGaps (String seq1, String seq2) {
        String retSeq = "";
        for (int i=0; i<seq1.length(); i++) {
            char res = seq2.charAt(i);
            if (seq1.charAt(i)!='-') {
                retSeq += res;
            }
        }
        LinkedList<Integer> index = new LinkedList<Integer>();
        int j=0;
        for (int i=0; i<retSeq.length(); i++) {
            if (retSeq.charAt(i)!='-') {
                index.add(j++);
            }
        }
        int[] retIndex = new int[index.size()];
        j=0;
        for (Integer i : index) {
            retIndex[j++] = i;
        }
        HashMap<String,int[]> ret = new HashMap<String,int[]>();
        ret.put(retSeq, retIndex);
        return ret;
    }

    private static int cost(char residue1, char residue2, int gapPenalty) {
        char r1 = Character.toUpperCase(residue1);
        int r1idx = -1;
        char r2 = Character.toUpperCase(residue2);
        int r2idx = -1;
        for (int i=0; i<20; i++) {
            if (AATABLE[i]==r1) {
                r1idx = i;
            }
            if (AATABLE[i]==r2) {
                r2idx = i;
            }
        }
        if (r1idx==-1 || r2idx == -1) {
            return gapPenalty;
        }
        else {
            return BLOSUM62[r1idx][r2idx];
        }
    }

    public static class InvalidFASTAFileException extends Exception {
        public InvalidFASTAFileException() {
        }
    }
    
    public static String translateDNAExtractV3(String dnaSequence) {
        String[] translatedFrames = translateAllFrames(dnaSequence);
        
        String[] frame1 = pairwiseAlign(v32B4C, translatedFrames[0], -3);
        String[] frame2 = pairwiseAlign(v32B4C, translatedFrames[1], -3);
        String[] frame3 = pairwiseAlign(v32B4C, translatedFrames[2], -3);
        
        int frame1Score = scoreAlignment(frame1);
        int frame2Score = scoreAlignment(frame2);
        int frame3Score = scoreAlignment(frame3);
        
        if (frame1Score >= frame2Score && frame1Score >= frame3Score) {
            return frame1[1];
        }
        if (frame2Score > frame1Score && frame2Score > frame3Score) {
            return frame2[1];
        }
        if (frame3Score > frame1Score && frame3Score > frame2Score) {
            return frame3[1];
        }
        
        return null;
    }
    
    private static int scoreAlignment(String[] alignment) {
        int score = 0;
        char[] aln1 = alignment[0].toCharArray();
        char[] aln2 = alignment[1].toCharArray();
        for (int i=0; i<aln1.length; i++) {
            score += cost(aln1[i], aln2[i], -5);
        }
        return score;
    }
    
    private static String[] translateAllFrames(String s) {
        String[] frames = new String[] {"","",""};
        
        for (int i=0; i<s.length()-3; i++) {
            String codon = s.substring(i, i+2).toUpperCase();
            if (translationTable.containsKey(codon)) {
                frames[i%3] += translationTable.get(codon);
            }
            else {
                System.err.println("Invalid codon: " + codon);
                frames[i%3] += "X";
            }
        }
        
        return frames;
    }
}
