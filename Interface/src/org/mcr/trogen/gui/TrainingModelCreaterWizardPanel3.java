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
package org.mcr.trogen.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismAlgorithmParameters;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismModelSet;
import org.mcr.trogen.utils.Utilities;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class TrainingModelCreaterWizardPanel3 implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TrainingModelCreaterVisualPanel3 component;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean success;
    private List<Sequence> r5TrainingSeqs;
    private List<Sequence> x4TrainingSeqs;
    private List<Sequence> r5ValidationSeqs;
    private List<Sequence> x4ValidationSeqs;
    private final TropismModelSet model;

    public TrainingModelCreaterWizardPanel3() {
        super();
        success = false;
        r5TrainingSeqs = new LinkedList<Sequence>();
        x4TrainingSeqs = new LinkedList<Sequence>();
        r5ValidationSeqs = new LinkedList<Sequence>();
        x4ValidationSeqs = new LinkedList<Sequence>();
        model = new TropismModelSet();
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TrainingModelCreaterVisualPanel3 getComponent() {
        if (component == null) {
            component = new TrainingModelCreaterVisualPanel3();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return success;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
        component.addPropertyChangeListener(this);
        String modelName = (String) wiz.getProperty("ModelName");
        model.setName(modelName);
        File r5TrainingData = (File) wiz.getProperty("R5TrainingData");
        File x4TrainingData = (File) wiz.getProperty("X4TrainingData");
        File r5ValidationData = (File) wiz.getProperty("R5ValidationData");
        File x4ValidationData = (File) wiz.getProperty("X4ValidationData");
        List<TropismAlgorithm> ta = (List<TropismAlgorithm>) wiz.getProperty("Algorithms");
        new Thread(new TrainingModelCreaterWizardPanel3.TrainTropism(component, r5TrainingData, x4TrainingData, r5ValidationData, x4ValidationData, ta)).start();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty("TropismModel", model);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    private class TrainTropism implements Runnable {

        private TrainingModelCreaterVisualPanel3 component;
        private File r5train;
        private File x4train;
        private File r5validate;
        private File x4validate;
        private List<TropismAlgorithm> algorithms;

        public TrainTropism(TrainingModelCreaterVisualPanel3 component, File r5train, File x4train, File r5validate, File x4validate, List<TropismAlgorithm> algorithms) {
            this.component = component;
            this.r5train = r5train;
            this.x4train = x4train;
            this.r5validate = r5validate;
            this.x4validate = x4validate;
            this.algorithms = algorithms;
        }

        @Override
        public void run() {
            String textField = " •\tReading Sequence Data ...\n";
            component.setTextField(textField);
            try {
                r5TrainingSeqs = Utilities.readFasta(r5train.getAbsolutePath());
                x4TrainingSeqs = Utilities.readFasta(x4train.getAbsolutePath());
                r5ValidationSeqs = Utilities.readFasta(r5validate.getAbsolutePath());
                x4ValidationSeqs = Utilities.readFasta(x4validate.getAbsolutePath());
            } catch (Utilities.InvalidFASTAFileException ex) {
                //Exceptions.printStackTrace(ex);
                textField = " ✘\tError Reading Sequence Data\n";
                component.setTextField(textField);
                return;
            }
            textField = " ✔\tReading Sequence Data\n •\tPairwise Aligning Sequences ...";
            component.setTextField(textField);
            for (Sequence seq : r5TrainingSeqs) {
                seq.pairwiseAlignTo2B4C();
            }
            for (Sequence seq : x4TrainingSeqs) {
                seq.pairwiseAlignTo2B4C();
            }
            for (Sequence seq : r5ValidationSeqs) {
                seq.pairwiseAlignTo2B4C();
            }
            for (Sequence seq : x4ValidationSeqs) {
                seq.pairwiseAlignTo2B4C();
            }
            model.setR5TrainingSeqs(r5TrainingSeqs);
            model.setR5ValidationSeqs(r5ValidationSeqs);
            model.setX4TrainingSeqs(x4TrainingSeqs);
            model.setX4ValidationSeqs(x4ValidationSeqs);
            
            textField = " ✔\tReading Sequence Data\n ✔\tPairwise Aligning Sequences";
            component.setTextField(textField);

            for (TropismAlgorithm ta : algorithms) {
                System.out.println("Algorithm: " + ta.getAlgorithmName());
                String textFieldRun = textField + "\n •\t" + ta.getAlgorithmName();
                String textFieldSuccess = textField + "\n ✔\t" + ta.getAlgorithmName();
                String textFieldError = textField + "\n ✘\t" + ta.getAlgorithmName();
                textField = textFieldRun;
                component.setTextField(textField);
                TropismModel m = ta.trainModel(new TropismAlgorithmParameters(), r5TrainingSeqs, x4TrainingSeqs, r5ValidationSeqs, x4ValidationSeqs);
                model.addModel(ta.getAlgorithmName(), m);
                textField = textFieldSuccess;
                component.setTextField(textField);
            }
            
            success = true;
            changeSupport.fireChange();
        }
    }
}
