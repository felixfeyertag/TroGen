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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.mcr.trogen.filetype.TroGenResultsDataObject;
import org.mcr.trogen.filetype.TroGenResultsVisualElement;
import org.netbeans.api.actions.Openable;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category="File", id="org.mcr.trogen.gui.ClassifyTropismWizardAction")
@ActionRegistration(displayName="Perform Tropism Classification")
@ActionReferences({
    @ActionReference(path="Menu/File", position=100),
    @ActionReference(path = "Shortcuts", name = "D-T")
})
public final class ClassifyTropismWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject configRoot = FileUtil.getConfigRoot();
        FileObject configModel = configRoot.getFileObject("TropismModels");
        if (configModel == null) {
            try {
                configModel = configRoot.createFolder("TropismModels");
                URL defaultModel = this.getClass().getResource("Library/Default.model");
                FileUtil.copyFile(FileUtil.toFileObject(new File(defaultModel.getFile())), configModel, "Default.model");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new ClassifyTropismWizardPanel1());
        panels.add(new ClassifyTropismWizardPanel2());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("Perform Tropism Classification");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            /*try {
                //http://wiki.netbeans.org/EditorCookieOrOpenCookie
                // do something
                //TroGenResultsVisualElement troGenResultsVisualElement = new TroGenResultsVisualElement(Lookup.getDefault());
                //FileSystem fs = FileUtil.createMemoryFileSystem();
                //FileObject fob = fs.getRoot().createData("TroGenResults", "xml");
                //DataObject data = TroGenResultsDataObject.find(fob);
                //OpenCookie cookie = (OpenCookie)data.getCookie(OpenCookie.class);
                //cookie.open();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }*/
            DataObject troGenResults = (DataObject) wiz.getProperty("TroGenResults");
            //System.out.println("DATA OBJECT: " + troGenResults.getClass().toString());
            EditorCookie cookie = (EditorCookie) troGenResults.getLookup().lookup(EditorCookie.class);
            //System.out.println("OPEN COOKIE: " + cookie.getClass().toString());
            cookie.open();
        }
    }
}
