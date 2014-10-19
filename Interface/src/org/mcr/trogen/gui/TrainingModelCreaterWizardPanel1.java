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
import java.util.List;
import javax.swing.event.ChangeListener;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismModelSet;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class TrainingModelCreaterWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TrainingModelCreaterVisualPanel1 component;
    private boolean isValid = false;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TrainingModelCreaterVisualPanel1 getComponent() {
        if (component == null) {
            component = new TrainingModelCreaterVisualPanel1();
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
        return isValid;
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
        component.addPropertyChangeListener(this);
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        List<TropismAlgorithm> algorithms = component.getTrainingAlgorithmSelection();
        //System.out.println(algorithms.size());
        //for (TropismAlgorithm ta : algorithms) {
        //    System.out.println(ta.getAlgorithmName());
        //}
        
        wiz.putProperty("Algorithms", algorithms);
        wiz.putProperty("ModelName", component.getTrainingModelName());
        
        TropismModelSet modelSet = new TropismModelSet();
        modelSet.setName(component.getTrainingModelName());
               
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        boolean wasValid = isValid;
        if (component.getTrainingAlgorithmSelection().isEmpty() || component.getTrainingModelName().isEmpty()) {
            isValid = false;
        }
        else {
            isValid = true;
        }
        //System.out.println("propertyChange: " + isValid + " " + wasValid);
        if (wasValid != isValid) {
            changeSupport.fireChange();
        }
    }
}
