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
package org.mcr.trogen.filetype;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.misc.IOUtils;

@Messages({
    "LBL_TroGenResults_LOADER=Files of TroGenResults"
})
@MIMEResolver.NamespaceRegistration(
        displayName = "#LBL_TroGenResults_LOADER",
        mimeType = "text/trogen+xml",
        elementNS = {"http://www.bioinf.manchester.ac.uk/robertson/trogen"})
@DataObject.Registration(
        mimeType = "text/trogen+xml",
        iconBase = "org/mcr/trogen/filetype/tg.png",
        displayName = "Tropism Classification Results",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300),
    @ActionReference(
            path = "Loaders/text/trogen+xml/Actions",
            id
            = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class TroGenResultsDataObject extends MultiDataObject {

    private Document xmlData;
    
    // MultiView window 1
    private final TableView<ObservableList<StringProperty>> resultsTable;
    private final Label fprValue;
    private final Slider fprSlider;
    
    // MultiView window 2
    private final Map<String,Label> x4CountLabel;
    private final Map<String,Label> r5CountLabel;
    private final Map<String,Label> totalCountLabel;
    private final Label fprValue2;
    private final Slider fprSlider2;
    private final Map<String,PieChart> pieCharts;
    private final Map<String,AreaChart> areaCharts;

    public TroGenResultsDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/trogen+xml", true);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            xmlData = db.parse(this.getPrimaryFile().getInputStream());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }

        final NodeList resultNodes = xmlData.getChildNodes().item(0).getChildNodes();

        final List<String> algorithmNames = new ArrayList<String>();

        pieCharts = new HashMap<String,PieChart>();
        areaCharts = new HashMap<String,AreaChart>();
        x4CountLabel = new HashMap<String,Label>();
        r5CountLabel = new HashMap<String,Label>();
        totalCountLabel = new HashMap<String,Label>();
        
        NodeList firstNode = resultNodes.item(1).getChildNodes();
        for (int i = 0; i < firstNode.getLength(); i++) {
            if (firstNode.item(i).getNodeName().toLowerCase().equals("tropismalgorithm")) {
                NodeList algorithmNode = firstNode.item(i).getChildNodes();
                for (int j = 0; j < algorithmNode.getLength(); j++) {
                    if (algorithmNode.item(j).getNodeName().toLowerCase().equals("name")) {
                        String algorithmName = algorithmNode.item(j).getTextContent();
                        algorithmNames.add(algorithmName);
                        PieChart pieChart = new PieChart();
                        pieChart.setLabelsVisible(false);
                        pieChart.setPrefSize(250, 250);
                        pieChart.setMinSize(250, 250);
                        pieChart.setMaxSize(250, 250);
                        pieCharts.put(algorithmName, pieChart);
                        areaCharts.put(algorithmName, new AreaChart(new NumberAxis(), new NumberAxis()));
                        x4CountLabel.put(algorithmName, new Label());
                        r5CountLabel.put(algorithmName, new Label());
                        totalCountLabel.put(algorithmName, new Label());
                    }
                }
            }
        }

        Collections.sort(algorithmNames);
        
        resultsTable = new TableView<ObservableList<StringProperty>>();
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultsTable.getItems().clear();
        resultsTable.getColumns().clear();
        resultsTable.setPlaceholder(new Label("Loading TroGen Results..."));
        Task<Void> task
                = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                        final ProgressHandle progr = ProgressHandleFactory.createHandle("Loading results");
                        progr.start(resultNodes.getLength());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                resultsTable.getColumns().add(
                                        createColumn(0, "Name"));
                                resultsTable.getColumns().add(
                                        createColumn(1, "Sequence"));
                                resultsTable.getColumns().add(
                                        createColumn(2, "Mapped Sequence"));
                                int i = 3;
                                for (String s : algorithmNames) {
                                    TableColumn algorithmColumn = new TableColumn(s);

                                    TableColumn<ObservableList<StringProperty>, String> scoreColumn = createColumn(i++, "Score");
                                    TableColumn<ObservableList<StringProperty>, String> fprColumn = createColumn(i++, "FPR");
                                    TableColumn<ObservableList<StringProperty>, String> callColumn = createColumn(i++, "Call");

                                    algorithmColumn.getColumns().addAll(scoreColumn, fprColumn, callColumn);
                                    resultsTable.getColumns().add(algorithmColumn);
                                   
                                }
                            }
                        });

                        for (int i = 1; i < resultNodes.getLength(); i += 2) {
                                                        
                            final NodeList currentNode = resultNodes.item(i).getChildNodes();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    ObservableList<StringProperty> data = readResultsRow(currentNode);
                                    resultsTable.getItems().add(data);
                                    initialiseCharts();
                                }

                                private ObservableList<StringProperty> readResultsRow(NodeList currentNode) {
                                    ObservableList<StringProperty> ret = FXCollections.observableArrayList();

                                    String[] resultRow = new String[3 + 3 * algorithmNames.size()];
                                    for (int j = 0; j < currentNode.getLength(); j++) {
                                        if (currentNode.item(j).getNodeName().toLowerCase().equals("name")) {
                                            resultRow[0] = currentNode.item(j).getTextContent();
                                        } else if (currentNode.item(j).getNodeName().toLowerCase().equals("sequence")) {
                                            resultRow[1] = currentNode.item(j).getTextContent();
                                        } else if (currentNode.item(j).getNodeName().toLowerCase().equals("mappedalignment")) {
                                            resultRow[2] = currentNode.item(j).getTextContent();
                                        } else if (currentNode.item(j).getNodeName().toLowerCase().equals("tropismalgorithm")) {
                                            NodeList algorithmNode = currentNode.item(j).getChildNodes();
                                            int position = -1;
                                            String fpr = "-1";
                                            String score = "-1";
                                            for (int k = 0; k < algorithmNode.getLength(); k++) {
                                                if (algorithmNode.item(k).getNodeName().toLowerCase().equals("name")) {
                                                    position = algorithmNames.indexOf(algorithmNode.item(k).getTextContent());
                                                } else if (algorithmNode.item(k).getNodeName().toLowerCase().equals("score")) {
                                                    score = algorithmNode.item(k).getTextContent();
                                                } else if (algorithmNode.item(k).getNodeName().toLowerCase().equals("fpr")) {
                                                    fpr = algorithmNode.item(k).getTextContent();
                                                }
                                            }
                                            if (position > -1) {
                                                resultRow[3 + position * 3] = score;
                                                resultRow[4 + position * 3] = fpr;
                                                resultRow[5 + position * 3] = "Undefined";
                                            }
                                        }
                                    }

                                    for (String s : resultRow) {
                                        ret.add(new SimpleStringProperty(s));
                                    }

                                    return ret;
                                }
                            });
                            
                            progr.progress(2);
                        }
                        
                        progr.finish();
                        
                        
                    }
                        });
                        return null;
                    }
                };
        
        fprSlider = new Slider();
        fprSlider.setPrefWidth(300);
        fprSlider.setMin(0.0);
        fprSlider.setMax(1.0);
        fprSlider.setValue(0.0);
        fprSlider.setShowTickLabels(true);
        fprSlider.setShowTickMarks(true);
        fprSlider.setMajorTickUnit(0.1);
        fprSlider.setMinorTickCount(20);
        fprSlider.setBlockIncrement(0.05);
        fprSlider.setValue(0.05);
        fprValue = new Label(String.format("%.2f", fprSlider.getValue()));
        fprSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                /*fprValue.setText(String.format("%.2f", newValue));
                fprSlider2.setValue(newValue.doubleValue());
                for (ObservableList<StringProperty> result : resultsTable.getItems()) {
                    for (int i = 2 + 2; i < result.size(); i += 3) {
                        if (Double.valueOf(result.get(i).getValue()) < newValue.doubleValue()) {
                            result.get(i + 1).set("X4");
                        } else {
                            result.get(i + 1).set("R5");
                        }
                    }
                }*/
                
                updateFPR(newValue);
            }
        });
        
        fprSlider2 = new Slider();
        fprSlider2.setPrefWidth(300);
        fprSlider2.setMin(0.0);
        fprSlider2.setMax(1.0);
        fprSlider2.setValue(0.0);
        fprSlider2.setShowTickLabels(true);
        fprSlider2.setShowTickMarks(true);
        fprSlider2.setMajorTickUnit(0.1);
        fprSlider2.setMinorTickCount(20);
        fprSlider2.setBlockIncrement(0.05);
        fprSlider2.setValue(0.05);
        fprValue2 = new Label(String.format("%.2f", fprSlider2.getValue()));
        fprSlider2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //fprValue2.setText(String.format("%.2f", newValue));
                //fprSlider.setValue(newValue.doubleValue());
                //updateFPR(newValue);
            }
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        
        int counter = 0;
        while (thread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            System.out.println("Thread running: " + counter++);
        }

        
        
       
        
        //updateFPR(0.05);
        //fprSlider.setValue(0.05);
    }
    
    private void initialiseCharts() {
        Double newValue = 0.05;
        fprValue.setText(String.format("%.2f", newValue));
        fprValue2.setText(String.format("%.2f", newValue));
        fprSlider.setValue(newValue.doubleValue());
        fprSlider2.setValue(newValue.doubleValue());
        Map<String,Integer> x4Calls = new HashMap<String,Integer>();
        Map<String,Integer> r5Calls = new HashMap<String,Integer>();
        Map<String,Double> min = new HashMap<String,Double>();
        Map<String,Double> max = new HashMap<String,Double>();
        Map<String,int[]> buckets = new HashMap<String,int[]>();
        for (String algorithm : pieCharts.keySet()) {
            x4Calls.put(algorithm, 0);
            r5Calls.put(algorithm, 0);
            min.put(algorithm, Double.POSITIVE_INFINITY);
            max.put(algorithm, Double.NEGATIVE_INFINITY);
            buckets.put(algorithm, new int[17]);
        }
        for (ObservableList<StringProperty> result : resultsTable.getItems()) {
            for (int i = 2 + 2; i < result.size(); i += 3) {
                int j = (i-4)/3+3;
                String algorithmName = resultsTable.getColumns().get(j).getText();
                //System.out.println("A1: " + algorithmName);
                if (Double.valueOf(result.get(i).getValue()) < newValue.doubleValue()) {
                    result.get(i + 1).set("X4");
                    x4Calls.put(algorithmName, x4Calls.get(algorithmName).intValue()+1);
                } else {
                    result.get(i + 1).set("R5");
                    r5Calls.put(algorithmName, r5Calls.get(algorithmName).intValue()+1);
                }
                if (Double.valueOf(result.get(i).getValue()) < min.get(algorithmName).doubleValue()) {
                    min.put(algorithmName, Double.valueOf(result.get(i).getValue()));
                }
                
                if (Double.valueOf(result.get(i).getValue()) > max.get(algorithmName).doubleValue()) {
                    max.put(algorithmName, Double.valueOf(result.get(i).getValue()));
                }
            }
        }
        for (ObservableList<StringProperty> result : resultsTable.getItems()) {
            for (int i = 2 + 2; i < result.size(); i += 3) {
                int j = (i-4)/3+3;
                String algorithmName = resultsTable.getColumns().get(j).getText();
                double minVal = min.get(algorithmName);
                double step = (max.get(algorithmName) - min.get(algorithmName)) / 16.0;
                double currentValue = Double.valueOf(result.get(i).getValue());
                int k = 0;
                for (k=0; k<17; k++) {
                    if (minVal + k*step > currentValue) {
                        break;
                    }
                }
                double k2 = (currentValue-minVal)/step+1;
                //System.out.println(k + "    " + (int)k2);  
                int[] bucket = buckets.get(algorithmName);
                if (k>16) {k=16;}
                if (k<0) {k=0;}
                bucket[k] += 1;
                buckets.put(algorithmName, bucket);
            }
        }
        for (String algorithm : pieCharts.keySet()) {
            assert (x4Calls.containsKey(algorithm) && r5Calls.containsKey(algorithm));
            ObservableList<PieChart.Data> pieChartData = 
                    FXCollections.observableArrayList(
                    new PieChart.Data("CXCR4-using", x4Calls.get(algorithm)),
                    new PieChart.Data("CCR5", r5Calls.get(algorithm)));
            pieCharts.get(algorithm).setData(pieChartData);
            
            XYChart.Series series = new XYChart.Series();
            double current = min.get(algorithm);
            series.setName("Distribution of test scores");
            for (int i : buckets.get(algorithm)) {
                series.getData().add(new XYChart.Data(current, (double) i));
                current += (max.get(algorithm)-min.get(algorithm))/16.0;
                
            }
            //areaCharts.get(algorithm).getData().add(series);
            
            int x4count = (int) pieCharts.get(algorithm).getData().get(0).getPieValue();
            int r5count = (int) pieCharts.get(algorithm).getData().get(1).getPieValue();
            
            x4CountLabel.get(algorithm).setText(Integer.toString(x4count));
            r5CountLabel.get(algorithm).setText(Integer.toString(r5count));
            totalCountLabel.get(algorithm).setText(Integer.toString(x4count+r5count));
        }
    }
    
    private void updateFPR(Number newValue) {
        fprValue.setText(String.format("%.2f", newValue));
        fprValue2.setText(String.format("%.2f", newValue));
        //fprSlider.setValue(newValue.doubleValue());
        //fprSlider2.setValue(newValue.doubleValue());
        Map<String,Integer> x4Calls = new HashMap<String,Integer>();
        Map<String,Integer> r5Calls = new HashMap<String,Integer>();
        for (String algorithm : pieCharts.keySet()) {
            x4Calls.put(algorithm, 0);
            r5Calls.put(algorithm, 0);
        }
        for (ObservableList<StringProperty> result : resultsTable.getItems()) {
            for (int i = 2 + 2; i < result.size(); i += 3) {
                int j = (i-4)/3+3;
                String algorithmName = resultsTable.getColumns().get(j).getText();
                //System.out.println("A1: " + algorithmName);
                if (Double.valueOf(result.get(i).getValue()) < newValue.doubleValue()) {
                    result.get(i + 1).set("X4");
                    x4Calls.put(algorithmName, x4Calls.get(algorithmName).intValue()+1);
                    
                } else {
                    result.get(i + 1).set("R5");
                    r5Calls.put(algorithmName, r5Calls.get(algorithmName).intValue()+1);
                }
            }
        }
        for (String algorithm : pieCharts.keySet()) {
            assert (x4Calls.containsKey(algorithm) && r5Calls.containsKey(algorithm));
            //ObservableList<PieChart.Data> pieChartData = 
            //        FXCollections.observableArrayList(
            //        new PieChart.Data("CXCR4-using", x4Calls.get(algorithm)),
            //        new PieChart.Data("CCR5", r5Calls.get(algorithm)));
            //pieCharts.get(algorithm).setData(pieChartData);
            pieCharts.get(algorithm).getData().get(0).setPieValue(x4Calls.get(algorithm));
            pieCharts.get(algorithm).getData().get(1).setPieValue(r5Calls.get(algorithm));
        
        
            int x4count = (int) pieCharts.get(algorithm).getData().get(0).getPieValue();
            int r5count = (int) pieCharts.get(algorithm).getData().get(1).getPieValue();
            
            x4CountLabel.get(algorithm).setText(Integer.toString(x4count));
            r5CountLabel.get(algorithm).setText(Integer.toString(r5count));
            totalCountLabel.get(algorithm).setText(Integer.toString(x4count+r5count));
        }
        
    }

    private TableColumn<ObservableList<StringProperty>, String> createColumn(final int columnIndex, String columnTitle) {
        TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<ObservableList<StringProperty>, String>();
        String title = (columnTitle == null || columnTitle.trim().length() == 0) ? "Column " + (columnIndex + 1) : columnTitle;
        column.setText(title);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
                ObservableList<StringProperty> values = cellDataFeatures.getValue();
                for (int i = values.size(); i <= columnIndex; i++) {
                    values.add(i, new SimpleStringProperty(""));
                }
                return cellDataFeatures.getValue().get(columnIndex);
            }
        });
        column.setCellFactory(TextFieldTableCell.<ObservableList<StringProperty>>forTableColumn());
        return column;

    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /*@MultiViewElement.Registration(
            displayName = "#LBL_TroGenResults_EDITOR",
            iconBase = "org/mcr/trogen/filetype/tg.png",
            mimeType = "text/trogen+xml",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "TroGenResults",
            position = 1000)
    @Messages("LBL_TroGenResults_EDITOR=Source")*/
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

    public Document getXmlData() {
        return xmlData;
    }

    public TableView<ObservableList<StringProperty>> getResultsTable() {
        return resultsTable;
    }

    public Label getFprValue() {
        return fprValue;
    }

    public Slider getFprSlider() {
        return fprSlider;
    }
    
    public Label getFprValue2() {
        return fprValue2;
    }

    public Slider getFprSlider2() {
        return fprSlider2;
    }
    
    public Map<String,PieChart> getPieCharts() {
        return pieCharts;
    }
    
    public Map<String,AreaChart> getAreaCharts() {
        return areaCharts;
    }

    public Map<String,Label> getX4CountLabel() {
        return x4CountLabel;
    }

    public Map<String,Label> getR5CountLabel() {
        return r5CountLabel;
    }

    public Map<String,Label> getTotalCountLabel() {
        return totalCountLabel;
    }
}
