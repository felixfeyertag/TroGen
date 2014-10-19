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

package org.mcr.trogen.svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mcr.libsvm.svm_predict;
import org.mcr.libsvm.svm_train;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismAlgorithmParameters;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismResult;
import org.mcr.trogen.utils.Utilities;
import org.openide.util.lookup.ServiceProvider;


/**
 * @filename SVM.java
 * @date 09-Sep-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
@ServiceProvider(service=TropismAlgorithm.class)
public class SVM implements TropismAlgorithm {
    private File svmTrainingFile;
    private TropismModel model;

    @Override
    public String getAlgorithmName() {
        return "Support Vector Machine";
    }

    @Override
    public TropismAlgorithmParameters getAlgorithmParameters() {
        return new TropismAlgorithmParameters();
    }

    @Override
    public Sequence classifySequence(Sequence seq) throws TropismAlgorithmException {
        assert svmTrainingFile != null;
        TropismResult result = new TropismResult();
        try {
            /*
                my ($self, $model, $out, @seqs) = @_;
                open TESTDATA, ">$out.ts";
                foreach my $seq (@seqs) {
                    print TESTDATA $self->libsvm_format("0", $seq->[1]);
                }
                close TESTDATA;
                system "contrib/libsvm-3.11/svm-predict -b 1 $out.ts $model $out > /dev/null";
                my @results = ();
                open SVM, $out;
                <SVM>;
                foreach my $l (<SVM>) {
                    chomp $l;
                    push @results, ([split / /, $l]);
                    #if ($l =~ /1/) {
                    #    #print "<td bgcolor=\"#87CEFF\">$l</td>";
                    #    push @results, 0;
                    #}
                    #else {
                    #    #print "<td bgcolor=\"#FF6347\">$l</td>";
                    #    push @results, 1;
                    #}
                }
                close SVM;        
             */
            File testingFile = null;
            File testingOutput = null;
            BufferedWriter writer = null;
            testingFile = File.createTempFile(Long.toString(System.nanoTime()), "ts.tmp");
            testingOutput = File.createTempFile(Long.toString(System.nanoTime()), "result.tmp");
            writer = new BufferedWriter(new FileWriter(testingFile));
            writer.write(libSVMFormat("0", seq.getMappedAlignment()));
            if (writer!=null) {
                writer.close();
                //System.out.println(testingFile.getAbsolutePath() + "   " + svmTrainingFile.getAbsolutePath() + "   " + testingOutput.getAbsolutePath());
                svm_predict.main(new String[] {"-b", "1", testingFile.getAbsolutePath(), svmTrainingFile.getAbsolutePath(), testingOutput.getAbsolutePath()});
                //System.out.println("Output:");
                BufferedReader br = new BufferedReader(new FileReader(testingOutput.getAbsolutePath()));
                String line = null;
                br.readLine();
                String res = br.readLine();
                String[] results = res.split(" ");
                result.setScore(Double.parseDouble(results[1]));
                try {
                    result.setFpr(model.getFPRforScore(Double.parseDouble(results[1])));
                } catch (NullPointerException e) {
                    //result.setFpr(Double.parseDouble(results[2]));
                    result.setFpr(Double.NaN);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        seq.addTropismCall(this.getAlgorithmName(), result);
        return seq;
    }

    @Override
    public TropismModel trainModel(TropismAlgorithmParameters params, List<Sequence> r5training, List<Sequence> x4training, List<Sequence> r5validation, List<Sequence> x4validation) {
        model = new TropismModel();
        File trainingFile = null;
        svmTrainingFile = null;
        BufferedWriter writer = null;
        try {
            trainingFile = File.createTempFile(Long.toString(System.nanoTime()), "tg.tmp");
            svmTrainingFile = File.createTempFile(Long.toString(System.nanoTime()), "svm.tmp");
            writer = new BufferedWriter(new FileWriter(trainingFile));
            for (Sequence s : r5training) {
                writer.write(libSVMFormat("1", s.getMappedAlignment()));
            }
            for (Sequence s : x4training) {
                writer.write(libSVMFormat("2", s.getMappedAlignment()));
            }
        } catch (IOException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer!=null) {
                    writer.close();
                    System.out.println(trainingFile.getAbsoluteFile());
                }
            }
            catch (IOException ex) {
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            //svmTrain(trainingFile.getAbsolutePath(), svmTrainingFile.getAbsolutePath());
            
            svm_train.main(new String[] { "-s", "0", "-t", "0", "-n", "0.5", "-c", "1", "-e", "0.001", "-p", "0.1", "-h", "1", "-b", "1", trainingFile.getAbsolutePath(), svmTrainingFile.getAbsolutePath()});
        } catch (IOException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        Path path = Paths.get(svmTrainingFile.getAbsolutePath());
        try {
            byte[] data = Files.readAllBytes(path);
            LinkedList<byte[]> dataList = new LinkedList<byte[]>();
            dataList.add(data);
            model.put("svmtrainingfile", dataList);
        } catch (IOException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        double[] r5ValidationCalls = new double[r5validation.size()];
        int i=0;
        for (Sequence s : r5validation) {
            try {
                r5ValidationCalls[i] = ((TropismResult)(classifySequence(s).getTropismCall().get(this.getAlgorithmName()))).getScore();
                //System.out.println("R5: " + r5ValidationCalls[i]);
                i++;
            } catch (TropismAlgorithmException ex) {
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        model.setR5ValidationScores(r5ValidationCalls);
        model.setX4ValidationScores(x4ValidationCalls);
        model.calculateFPR();
        
        
        return model;
    }

    @Override
    public void loadModel(TropismModel model) {
        // create temporary file + save svm data
        svmTrainingFile = null;
        try {
            byte[] bytes = ((LinkedList<byte[]>) model.get("svmtrainingfile")).get(0);
            svmTrainingFile = File.createTempFile("tgsvm", Long.toString(System.nanoTime()));
            svmTrainingFile.deleteOnExit();
            Files.write(svmTrainingFile.toPath(), bytes);
        } catch (IOException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.model = model;
    }
    
    private String libSVMFormat(String c, String seq) {
        String formattedSeq = c;
        int[] convertedSequence = convertSequence(seq);
        for (int i=1; i<=convertedSequence.length; i++) {
            formattedSeq += " " + i + ":" + convertedSequence[i-1];
        }
        formattedSeq += "\n";
        return formattedSeq;
    }
    
    private int[] convertSequence(String seq) {
        char[] seqArray = seq.toCharArray();
        int len = Utilities.AATABLE.length+1;
        int[] convertedSequence = new int[(len) * seqArray.length];
        for (int i=0; i<seqArray.length; i++) {
            int pos = residuePosition(seqArray[i]);
            int arrayPos = i*len+pos;
            //System.out.println("Residue: " + seqArray[i] + " Position: " + pos + " seqPosition: " + i + " arrayPosition " + arrayPos);
            convertedSequence[arrayPos] = 1;
        }
        return convertedSequence;
    }
    
    private int residuePosition (char res) {
        int pos = Utilities.AATABLE.length;
        for (int i=0; i<Utilities.AATABLE.length; i++) {
            if (Character.toUpperCase(res)==Utilities.AATABLE[i]) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private void svmTrain(String absolutePath, String absolutePath0) {
        String os = System.getProperty("os.name");
        URL location = SVM.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("working dir: " + location.getFile());
        if (os.startsWith("Linux")) {
            Process p = null;
            try {
                //p = Runtime.getRuntime().exec("org/mcr/trogen/svm/svm-predict-linux -s 0 -t 0 -n 0.5 -c 1 -e 0.001 -p 0.1 -h 1 -b 1 " + absolutePath + " " + absolutePath0);
                p = Runtime.getRuntime().exec("perl org/mcr/trogen/svm/helloworld.pl");
                p.waitFor();
                BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
                String line = reader.readLine();
                while (line != null) {
                      System.out.println(reader.readLine());
                }
            } catch (IOException ex) {
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if (os.startsWith("MacOSX")) {
            
        }
        else if (os.startsWith("Windows")) {
            
        }
        
    }

}
