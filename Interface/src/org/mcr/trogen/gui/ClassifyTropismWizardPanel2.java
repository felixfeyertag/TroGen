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
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.mcr.trogen.filetype.TroGenResultsDataObject;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.SequenceType;
import org.mcr.trogen.utils.TropismAlgorithm;
import org.mcr.trogen.utils.TropismAlgorithmException;
import org.mcr.trogen.utils.TropismModelSet;
import org.mcr.trogen.utils.TropismResult;
import org.mcr.trogen.utils.Utilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.w3c.dom.Document;

public class ClassifyTropismWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ClassifyTropismVisualPanel2 component;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean success;
    private List<Sequence> sequences;
    private DataObject troGenResults;
    //private SequenceType sequenceType;

    
    public ClassifyTropismWizardPanel2() {
        super();
        success = false;
        sequences = new LinkedList<Sequence>();
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ClassifyTropismVisualPanel2 getComponent() {
        if (component == null) {
            component = new ClassifyTropismVisualPanel2();
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
        File data = (File) wiz.getProperty("TestingData");
        List<TropismAlgorithm> ta = (List<TropismAlgorithm>) wiz.getProperty("Algorithms");
        SequenceType sequenceType = (SequenceType) wiz.getProperty("SequenceType");
        new Thread(new RunTropism(component, data, sequenceType, ta)).start();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty("TroGenResults", troGenResults);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        
    }
    
    private class RunTropism implements Runnable {

        private final ClassifyTropismVisualPanel2 component;
        private final File f;
        private final List<TropismAlgorithm> algorithms;
        private final SequenceType sequenceType;
        
        public RunTropism (ClassifyTropismVisualPanel2 component, File f, SequenceType sequenceType, List<TropismAlgorithm> algorithms) {
            this.component = component;
            this.f = f;
            this.algorithms = algorithms;
            this.sequenceType = sequenceType;
            for (TropismAlgorithm ta : algorithms) {
                System.out.println("run tropism: " + ta.getAlgorithmName());
            }
        }
        
        @Override
        public void run() {
            String textField = " •\tReading Sequence Data ...\n";
            component.setTextField(textField);
            try {
                sequences = Utilities.readFasta(f.getAbsolutePath());
            } catch (Utilities.InvalidFASTAFileException ex) {
                //Exceptions.printStackTrace(ex);
                textField = " ✘\tError Reading Sequence Data\n";
                component.setTextField(textField);
                return;
            }
            if (sequenceType==SequenceType.Protein) {
                textField = " ✔\tReading Sequence Data\n •\tPairwise Aligning Sequences ...";
                component.setTextField(textField);
                for (Sequence seq : sequences) {
                    seq.pairwiseAlignTo2B4C();
                }
                textField = " ✔\tReading Sequence Data\n ✔\tPairwise Aligning Sequences";
                component.setTextField(textField);
            }
            else if (sequenceType==SequenceType.DNA) {
                textField = " ✔\tReading Sequence Data\n •\tExtracting V3 Amino Acid Sequence from Nucleotide Sequence ...";
                component.setTextField(textField);
                for (Sequence seq : sequences) {
                    seq.translateAndExtractV3();
                }
                textField = " ✔\tReading Sequence Data\n ✔\tExtracting V3 Amino Acid Sequence from Nucleotide Sequence";
                component.setTextField(textField);
                
                textField = " ✔\tReading Sequence Data\n ✔\tExtracting V3 Amino Acid Sequence from Nucleotide Sequence\n •\tPairwise Aligning Sequences ...";
                component.setTextField(textField);
                for (Sequence seq : sequences) {
                    seq.pairwiseAlignTo2B4C();
                }
                textField = " ✔\tReading Sequence Data\n ✔\tExtracting V3 Amino Acid Sequence from Nucleotide Sequence\n ✔\tPairwise Aligning Sequences";
                component.setTextField(textField);
            }
            for (TropismAlgorithm ta : algorithms) {
                String textFieldRun = textField + "\n •\t" + ta.getAlgorithmName();
                String textFieldSuccess = textField + "\n ✔\t" + ta.getAlgorithmName();
                String textFieldError = textField + "\n ✘\t" + ta.getAlgorithmName();
                try {
                    component.setTextField(textFieldRun);
                    for (Sequence seq : sequences) {
                        //ta.setSequence(seq);
                        ta.classifySequence(seq);
                    }
                } catch (TropismAlgorithmException e) {
                    component.setTextField(textFieldError);
                    return;
                }
                component.setTextField(textFieldSuccess);
                textField = textFieldSuccess;
            }
            
            /*XMLFileSystem xmlResults = new XMLFileSystem();
            XMLFileSystem.Impl xmlResultsI = new XMLFileSystem.Impl(xmlResults);
            try {
                xmlResultsI.createFolder("Sequences");
                for (Sequence s : sequences) {
                    String sequencePath = "Sequences/" + s.getIdentifier() + "/" + s.getName();
                    xmlResultsI.createFolder("Sequences/" + s.getIdentifier());
                    xmlResultsI.createData(sequencePath);
                    xmlResultsI.writeAttribute(sequencePath, "Sequence", s.getSequence());
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }*/
            
            String textFieldRun = textField + "\n •\tSummarising results";
            String textFieldSuccess = textField + "\n ✔\tSummarising results";
            String textFieldError = textField + "\n ✘\tSummarising results";
            
            component.setTextField(textFieldRun);
            
            Document xmlResults = null;
            try {
                
                FileSystem mFS = FileUtil.createMemoryFileSystem();
                FileObject xmlResultsFile = mFS.getRoot().createData(f.getName(), "xml");
                //System.out.println(xmlResultsFile.getMIMEType());
                
                OutputStream os = xmlResultsFile.getOutputStream();
                //OutputStream os = mFS.getRoot().createAndOpen("Results.xml");
                
                //XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
                //XMLStreamWriter writer = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(os));
                XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
                
                //DOMSource source = new DOMSource(xmlResults);
                //Transformer transformer = TransformerFactory.newInstance().newTransformer();
                //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                //StreamResult xmlOutput = new StreamResult(new StringWriter());
                //transformer.transform(source, xmlOutput);
                //writer.writeCData(xmlOutput.getWriter().toString());
                
                String indent = "";
                String tab = "  ";
                
                writer.writeStartDocument("1.0");
                writer.writeCharacters("\n");
                writer.writeStartElement("TroGen");
                writer.writeNamespace("xmlns", "http://www.bioinf.manchester.ac.uk/robertson/trogen");
                writer.writeCharacters("\n");
                indent += tab;
                for (Sequence s : sequences) {
                    writer.writeCharacters(indent);
                    writer.writeStartElement("TroGenResult");
                    writer.writeCharacters("\n");
                    indent += tab;
                    
                    writer.writeCharacters(indent);
                    writer.writeStartElement("Name");
                    writer.writeCharacters(s.getName());
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                    
                    writer.writeCharacters(indent);
                    writer.writeStartElement("Sequence");
                    writer.writeCharacters(s.getSequence());
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                    
                    writer.writeCharacters(indent);
                    writer.writeStartElement("MappedAlignment");
                    writer.writeCharacters(s.getMappedAlignment());
                    writer.writeEndElement();
                    writer.writeCharacters("\n");

		    for (String tropismCall : s.getTropismCall().keySet()) {
			    TropismResult tc = s.getTropismCall().get(tropismCall);
			    
			    writer.writeCharacters(indent);
			    writer.writeStartElement("TropismAlgorithm");
			    writer.writeCharacters("\n");
			    indent += tab;

			    writer.writeCharacters(indent);
			    writer.writeStartElement("Name");
			    writer.writeCharacters(tropismCall);
			    writer.writeEndElement();
			    writer.writeCharacters("\n");

			    writer.writeCharacters(indent);
			    writer.writeStartElement("Score");
			    writer.writeCharacters(Double.toString(tc.getScore()));
			    writer.writeEndElement();
			    writer.writeCharacters("\n");

			    writer.writeCharacters(indent);
			    writer.writeStartElement("FPR");
			    writer.writeCharacters(Double.toString(tc.getFpr()));
			    writer.writeEndElement();
			    writer.writeCharacters("\n");

			    indent = indent.replaceFirst(tab, "");
			    writer.writeCharacters(indent);
			    writer.writeEndElement();
			    writer.writeCharacters("\n");
		    }
                    
                    indent = indent.replaceFirst(tab, "");
                    writer.writeCharacters(indent);
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                }
                
                indent = indent.replaceFirst(tab, "");
                writer.writeCharacters(indent);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndDocument();
                writer.writeCharacters("\n");
                
                /*writer.writeStartDocument();
                writer.writeStartElement("TroGenResults");
                writer.writeNamespace("xmlns", "http://www.bioinf.manchester.ac.uk/robertson/trogen");
                writer.writeCharacters("Hello World");
                writer.writeEndElement();
                writer.writeEndDocument();*/
                
                writer.flush();
                writer.close();
                os.close();
                                
                //DOMSource source = new DOMSource(xmlResults);
                //Transformer transformer = TransformerFactory.newInstance().newTransformer();
                //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                //StreamResult result = new StreamResult();
                //result.setWriter((Writer) writer);
                //transformer.transform(source, result);
                
                DataObject resultsDataObject = TroGenResultsDataObject.find(xmlResultsFile);
                
                System.out.println("name: " + resultsDataObject.getName());
                System.out.println("string: " + resultsDataObject.toString());
                System.out.println(resultsDataObject.getClass().toString());
                                               
                /*Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StreamResult xmlOutput = new StreamResult(new StringWriter());
                transformer.transform(new DOMSource(((XMLDataObject)resultsDataObject).getDocument()), xmlOutput);
                System.out.println(xmlOutput.getWriter().toString());*/
                
                troGenResults = resultsDataObject;
                //troGenResults = new TroGenResultsDataObject(resultsDataObject.getPrimaryFile(), ((XMLDataObject)resultsDataObject).getMultiFileLoader());
                

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                component.setTextField(textFieldError);
                return;
            } catch (XMLStreamException ex) {
                Exceptions.printStackTrace(ex);
                component.setTextField(textFieldError);
                return;
            }
            component.setTextField(textFieldSuccess);
            //System.out.println(xmlResults.toString());
            
            success = true;
            changeSupport.fireChange();
        }        
    }
}
