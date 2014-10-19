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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.openide.util.Lookup;

public final class TrainingModelCreaterVisualPanel1 extends JPanel {

    private List<TrainingAlgorithmCheckBoxPanel> checkBoxes;
    
    /**
     * Creates new form TrainingModelCreaterVisualPanel1
     */
    public TrainingModelCreaterVisualPanel1() {
        initComponents();
        checkBoxes = new LinkedList<TrainingAlgorithmCheckBoxPanel>();
        Collection<? extends TropismAlgorithm> algorithms = Lookup.getDefault().lookupAll(TropismAlgorithm.class);
        
        
        BoxLayout boxLayout = new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS);
        jPanel1.setLayout(boxLayout);
        
        for (TropismAlgorithm algorithm : algorithms) {
            TrainingAlgorithmCheckBoxPanel panel = new TrainingAlgorithmCheckBoxPanel(this, algorithm);
            jPanel1.add(panel);
        }
        
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUI();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUI();
            }
        });

    }

    @Override
    public String getName() {
        return "Select Tropism Algorithms";
    }
    
    public List<TropismAlgorithm> getTrainingAlgorithmSelection() {
        List<TropismAlgorithm> algorithms = new LinkedList<TropismAlgorithm>();
        for (Component c : jPanel1.getComponents()) {
            if (c instanceof TrainingAlgorithmCheckBoxPanel) {
                TrainingAlgorithmCheckBoxPanel ta = (TrainingAlgorithmCheckBoxPanel)c;
                if (ta.isCheckBoxEnabled()) {
                    algorithms.add(ta.algorithm);
                }
            }
        }
        return algorithms;
    }

    private class TrainingAlgorithmCheckBoxPanel extends JPanel {
        
        private boolean isEnabled;
        private TropismAlgorithm algorithm;
        private JPanel parent;

        TrainingAlgorithmCheckBoxPanel(final JPanel parent, TropismAlgorithm algorithm) {
            this.parent = parent;
            this.algorithm = algorithm;
            String algorithmName = algorithm.getAlgorithmName();
            boolean hasProperties = false; //algorithm.getAlgorithmParameters().getParams().isEmpty();
            isEnabled = true;
            
            final JCheckBox algorithmCheckBox = new JCheckBox();
            JButton editPropertiesButton = new JButton();
            
            algorithmCheckBox.setSelected(isEnabled);
            
            algorithmCheckBox.setText(algorithmName);
            editPropertiesButton.setText("Properties");
            editPropertiesButton.setEnabled(hasProperties);

            editPropertiesButton.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    editPropertiesActionPerformed(evt);
                }

                private void editPropertiesActionPerformed(ActionEvent evt) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            
            algorithmCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isEnabled = algorithmCheckBox.isSelected();
                    parent.updateUI();
                }
            });
            
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(algorithmCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                    .addComponent(editPropertiesButton)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(algorithmCheckBox)
                    .addComponent(editPropertiesButton)));
        }
        
        public boolean isCheckBoxEnabled() {
            return isEnabled;
        }
        
        public TropismAlgorithm getAlgorithm() {
            return algorithm;
        }

    }
    
    public String getTrainingModelName() {
        return jTextField1.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 489, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 261, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel1);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TrainingModelCreaterVisualPanel1.class, "TrainingModelCreaterVisualPanel1.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TrainingModelCreaterVisualPanel1.class, "TrainingModelCreaterVisualPanel1.jLabel2.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(TrainingModelCreaterVisualPanel1.class, "TrainingModelCreaterVisualPanel1.jTextField1.text")); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTextField1PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        this.updateUI();
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTextField1PropertyChange
        // TODO add your handling code here:
        this.updateUI();
    }//GEN-LAST:event_jTextField1PropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
