/*
 * Copyright (C) 2014 The University of Manchester
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
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
package org.mcr.trogen.filetype;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "File",
	id = "org.mcr.trogen.filetype.ExportCSVfile"
)
@ActionRegistration(
	displayName = "#CTL_ExportCSVfile"
)
@ActionReference(path = "Menu/File", position = 1775)
@Messages("CTL_ExportCSVfile=Export Table as CSV File")
public final class ExportCSVfile extends JPanel implements ActionListener {

	private final TroGenResultsDataObject context;

	public ExportCSVfile(TroGenResultsDataObject context) {
		this.context = (TroGenResultsDataObject)context;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
        JFileChooser fileChooser = new JFileChooser ();
        if (fileChooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
            FileWriter fw = null;
            try {
                File f = fileChooser.getSelectedFile();
                fw = new FileWriter(f.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                TableView<ObservableList<StringProperty>> resultsTable = context.getResultsTable();
                
                int i=0;
                for (TableColumn<ObservableList<StringProperty>, ?> column : resultsTable.getColumns()) {
                    String columnHeader = column.getText();
                    if (i++<2) {
                        bw.write(column.getText() + ",");
                    }
                    else {
                        bw.write(column.getText() + " (Score)," + column.getText() + " (FPR)," + column.getText() + " (Call),");
                    }
                }
                
                bw.newLine();
                
                for (ObservableList<StringProperty> result : resultsTable.getItems()) {
                    for (StringProperty s : result) {
                        bw.write(s.getValue() + ",");
                    }
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
	}
}
