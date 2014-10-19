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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.mcr.trogen.filetype.TroGenResultsDataObject;
import org.mcr.trogen.utils.Sequence;
import org.mcr.trogen.utils.TropismModel;
import org.mcr.trogen.utils.TropismModelSet;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 */
public class TrainingModelManagerJFX extends javax.swing.JPanel {

    private static JFXPanel fxContainer;
    private ListView<String> modelListView;
    private ObservableList<String> modelListItems;
    private FileObject configModel;
    private Map<String, TropismModelSet> modelSets;
    private SplitPane modelPane;
    private ScrollPane viewPane;

    /**
     * Creates new form TrainingModelManagerJFX
     */
    public TrainingModelManagerJFX() {
        //initComponents();
        fxContainer = new JFXPanel();
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fxContainer, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fxContainer, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addContainerGap()));


        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                createScene();
            }
        });
    }

    private void createScene() {
        modelSets = new HashMap<String, TropismModelSet>();
        modelPane = new SplitPane();

        modelListView = new ListView<String>();
        modelListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                modelListViewSelectionChanged(oldValue, newValue);
            }
        });

        modelListView.setPrefWidth(300);

        FileObject configRoot = FileUtil.getConfigRoot();
        configModel = configRoot.getFileObject("TropismModels");
        if (configModel == null) {
            try {
                configModel = configRoot.createFolder("TropismModels");
                URL defaultModel = this.getClass().getResource("Library/Default.model");
                FileUtil.copyFile(FileUtil.toFileObject(new File(defaultModel.getFile())), configModel, "Default.model");
                
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /*configModel.addFileChangeListener(new FileChangeListener() {
         @Override
         public void fileFolderCreated(FileEvent fe) {
         updateModelList();
         }

         @Override
         public void fileDataCreated(FileEvent fe) {
         updateModelList();
         }

         @Override
         public void fileChanged(FileEvent fe) {
         updateModelList();
         }

         @Override
         public void fileDeleted(FileEvent fe) {
         updateModelList();
         }

         @Override
         public void fileRenamed(FileRenameEvent fre) {
         updateModelList();
         }

         @Override
         public void fileAttributeChanged(FileAttributeEvent fae) {
         updateModelList();
         }
         });*/

        updateModelList();

        viewPane = new ScrollPane();
        modelPane.getItems().addAll(modelListView, viewPane);
        modelPane.setDividerPositions(0.2);


        FlowPane buttonPane = new FlowPane();
        Button createModelButton = new Button("Create New Training Model");
        createModelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createModelButtonAction(event);
            }
        });
        createModelButton.setDefaultButton(true);
        Button deleteModelButton = new Button("Delete Selected Model");
        deleteModelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteModelButtonAction(event);
            }
        });
        Button loadModelButton = new Button("Load Model From File");
        loadModelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadModelButtonAction(event);
            }
        });
        Button saveModelButton = new Button("Save Selected Model");
        saveModelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveModelButtonAction(event);
            }
        });

        createModelButton.setPrefWidth(250);
        deleteModelButton.setPrefWidth(250);
        loadModelButton.setPrefWidth(250);
        saveModelButton.setPrefWidth(250);

        buttonPane.setStyle("-fx-background-color: white;");

        buttonPane.setHgap(20);
        buttonPane.setVgap(20);
        buttonPane.getChildren().addAll(createModelButton, deleteModelButton, loadModelButton, saveModelButton);

        buttonPane.setPadding(new Insets(10));

        modelPane.setDividerPosition(1, 0.1);

        BorderPane fullPane = new BorderPane();
        fullPane.setCenter(modelPane);
        fullPane.setBottom(buttonPane);
        fxContainer.setScene(new Scene(fullPane));
    }

    private void createModelButtonAction(ActionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panels.add(new TrainingModelCreaterWizardPanel1());
                panels.add(new TrainingModelCreaterWizardPanel2());
                panels.add(new TrainingModelCreaterWizardPanel3());
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
                final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
                // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
                wiz.setTitleFormat(new MessageFormat("{0}"));
                wiz.setTitle("Create New Training Model Wizard");
                if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TropismModelSet tropismModels = (TropismModelSet) wiz.getProperty("TropismModel");
                                FileObject modelFileObject = configModel.createData(tropismModels.getName(), "model");
                                System.out.println("Writing model file: " + modelFileObject.getPath());
                                TropismModelSet.writeModelSetToFile(tropismModels, modelFileObject);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            updateModelList();
                        }
                    });
                }
            }
        });
    }

    private void updateModelList() {
        modelListItems = FXCollections.observableArrayList();
        //FileObject configRoot = FileUtil.getConfigRoot();
        //FileObject configModel = configRoot.getFileObject("TropismModels");
        //System.out.println("Loading TropismModels config folder in " + configRoot.getPath());
        if (configModel == null) {
            //System.out.println("Could not find TropismModels config folder in " + configRoot.getPath());
            return;
        }

        for (FileObject fo : configModel.getChildren()) {
            modelListItems.add(fo.getName());
            if (!modelSets.containsKey(fo.getName())) {
                System.out.println("Reading model set file: " + fo.getPath() + "/ " + fo.getName());
                modelSets.put(fo.getName(), TropismModelSet.readModelSetFromFile(fo));
            }
        }
        modelListView.setItems(modelListItems);
    }

    private void deleteModelButtonAction(ActionEvent event) {
        //FileObject configRoot = FileUtil.getConfigRoot();
        //FileObject configModel = configRoot.getFileObject("TropismModels");
        if (configModel == null) {
            return;
        }
        String name = modelListView.getSelectionModel().getSelectedItems().get(0);
        FileObject modelFile = configModel.getFileObject(name, "model");
        if (modelFile == null) {
            System.out.println("Could not find Tropism Model file: " + configModel.getPath() + "/" + name);
            return;
        }
        try {
            modelFile.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        updateModelList();
    }

    private void loadModelButtonAction(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void saveModelButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //FileObject configRoot = FileUtil.getConfigRoot();
        //FileObject configModel = configRoot.getFileObject("TropismModels");
        if (configModel == null) {
            return;
        }
        String name = modelListView.getSelectionModel().getSelectedItems().get(0);
        FileObject modelFile = configModel.getFileObject(name);
        if (modelFile == null) {
            System.out.println("Could not find Tropism Model file: " + configModel.getPath() + "/" + name);
            return;
        }

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TroGen Model Files (*.model)", "*.model");
        fileChooser.getExtensionFilters().add(extFilter);

        //fileChooser.setInitialDirectory(new File(name));

        //Show save file dialog
        File file;
        file = fileChooser.showSaveDialog(fxContainer.getScene().getWindow());
    }

    private void modelListViewSelectionChanged(String oldValue, String newValue) {
        final TropismModelSet model = modelSets.get(newValue);
        int r5TrainingCount = model.getR5TrainingSeqs().size();
        int x4TrainingCount = model.getX4TrainingSeqs().size();
        int r5ValidationCount = model.getR5ValidationSeqs().size();
        int x4ValidationCount = model.getX4ValidationSeqs().size();
        System.out.println(model.getName());
        System.out.println("R5 training sequences: " + r5TrainingCount);
        System.out.println("X4 training sequences: " + x4TrainingCount);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(0, 10, 0, 10));

        Text r5TrainingSequenceText = new Text("CCR5 Training Sequence Count: ");
        Hyperlink r5TrainingLink = new Hyperlink();
        r5TrainingLink.setText("(View)");
        r5TrainingLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                openFasta("R5Training", model.getR5TrainingSeqs());
                System.out.println("R5 training link is clicked");
            }
        });
        gridPane.add(r5TrainingSequenceText, 0, 0);
        gridPane.add(new Text(Integer.toString(r5TrainingCount)), 1, 0);
        gridPane.add(r5TrainingLink, 2, 0);

        Text x4TrainingSequenceText = new Text("CXCR4 Training Sequence Count: ");
        Hyperlink x4TrainingLink = new Hyperlink();
        x4TrainingLink.setText("(View)");
        x4TrainingLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                openFasta("X4Training", model.getX4TrainingSeqs());
                System.out.println("R5 training link is clicked");
            }
        });
        gridPane.add(x4TrainingSequenceText, 0, 1);
        gridPane.add(new Text(Integer.toString(x4TrainingCount)), 1, 1);
        gridPane.add(x4TrainingLink, 2, 1);

        Text r5ValidationSequenceText = new Text("CCR5 Validation Sequence Count: ");
        Hyperlink r5ValidationLink = new Hyperlink();
        r5ValidationLink.setText("(View)");
        r5ValidationLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                openFasta("R5Validation", model.getR5ValidationSeqs());
                System.out.println("R5 validation link is clicked");
            }
        });
        gridPane.add(r5ValidationSequenceText, 0, 2);
        gridPane.add(new Text(Integer.toString(r5ValidationCount)), 1, 2);
        gridPane.add(r5ValidationLink, 2, 2);

        Text x4ValidationSequenceText = new Text("CXCR4 Validation Sequence Count: ");
        Hyperlink x4ValidationLink = new Hyperlink();
        x4ValidationLink.setText("(View)");
        x4ValidationLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                openFasta("X4Validation", model.getX4ValidationSeqs());
                System.out.println("X4 validation link is clicked");
            }
        });
        gridPane.add(x4ValidationSequenceText, 0, 3);
        gridPane.add(new Text(Integer.toString(x4ValidationCount)), 1, 3);
        gridPane.add(x4ValidationLink, 2, 3);

        GridPane graphGrid = new GridPane();
        ObservableList<PieChart.Data> trainPieChartData =
                FXCollections.observableArrayList(
                new PieChart.Data("CCR5", r5TrainingCount),
                new PieChart.Data("CXCR4", x4TrainingCount));
        final PieChart trainChart = new PieChart(trainPieChartData);
        trainChart.setTitle("Training Data");
        trainChart.setPrefHeight(200);

        ObservableList<PieChart.Data> validationPieChartData =
                FXCollections.observableArrayList(
                new PieChart.Data("CCR5", r5ValidationCount),
                new PieChart.Data("CXCR4", x4ValidationCount));
        final PieChart validateChart = new PieChart(validationPieChartData);
        validateChart.setTitle("Validation Data");
        validateChart.setPrefHeight(200);

        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 24 arial;");

        for (final PieChart.Data data : trainChart.getData()) {
            data.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                    new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    System.out.println("mouse pressed");
                    caption.setTranslateX(e.getSceneX());
                    caption.setTranslateY(e.getSceneY());
                    caption.setText(String.valueOf(data.getPieValue()) + "%");
                }
            });
        }

        graphGrid.add(trainChart, 0, 0);
        graphGrid.add(validateChart, 1, 0);

        HBox sequenceSummary = new HBox(16);


        sequenceSummary.getChildren().add(gridPane);
        sequenceSummary.getChildren().add(graphGrid);

        /*for (Node n : sequenceSummary.getChildren()) {
         n.setStyle("-fx-border-style: solid;"
         + "-fx-border-width: 1;"
         + "-fx-border-color: black");
         }*/

        /*HBox r5TrainingLabel = new HBox();
         r5TrainingLabel.getChildren().add(new Text("CCR5 Training Sequence Count: " + r5TrainingCount));
        
         r5TrainingLabel.getChildren().add(r5TrainingLink);

         HBox x4TrainingLabel = new HBox();
         x4TrainingLabel.getChildren().add(new Text("Number of CXCR4 Training Sequences: " + x4TrainingCount));
         Hyperlink x4TrainingLink = new Hyperlink();
         x4TrainingLink.setText("(View Sequences)");
         x4TrainingLink.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
         System.out.println("X4 training link is clicked");
         }
         });
         x4TrainingLabel.getChildren().add(x4TrainingLink);
        
         HBox r5ValidationLabel = new HBox();
         r5ValidationLabel.getChildren().add(new Text("Number of CCR5 Validation Sequences: " + r5ValidationCount));
         Hyperlink r5ValidationLink = new Hyperlink();
         r5ValidationLink.setText("(View Sequences)");
         r5ValidationLink.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
         System.out.println("R5 validation link is clicked");
         }
         });
         r5ValidationLabel.getChildren().add(r5ValidationLink);
        
         HBox x4ValidationLabel = new HBox();
         x4ValidationLabel.getChildren().add(new Text("Number of CXCR4 Validation Sequences: " + x4ValidationCount));
         Hyperlink x4ValidationLink = new Hyperlink();
         x4ValidationLink.setText("(View Sequences)");
         x4ValidationLink.setOnAction(new EventHandler<ActionEvent>() {
         @Override
         public void handle(ActionEvent e) {
         System.out.println("X4 validation link is clicked");
         }
         });
         x4ValidationLabel.getChildren().add(x4ValidationLink);
        
         VBox sequenceSummary = new VBox();
         Text title = new Text("Training and Validition Data");
         title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
         sequenceSummary.getChildren().add(title);
         VBox.setMargin(r5TrainingLabel, new Insets(0, 0, 0, 8));
         sequenceSummary.getChildren().add(r5TrainingLabel);
         VBox.setMargin(x4TrainingLabel, new Insets(0,0,0,8));
         sequenceSummary.getChildren().add(x4TrainingLabel);
         VBox.setMargin(r5ValidationLabel, new Insets(0,0,0,8));
         sequenceSummary.getChildren().add(r5ValidationLabel);
         VBox.setMargin(x4ValidationLabel, new Insets(0,0,0,8));
         sequenceSummary.getChildren().add(x4ValidationLabel);*/

        final VBox summaryPanes = new VBox();
        final TitledPane titledPane = new TitledPane("Training and Validation Sequence Information", sequenceSummary);

        //summaryPanes.setPrefWidth(viewPane.getWidth());

        
        summaryPanes.getChildren().add(titledPane);

        for (String s : model.getModels().keySet()) {

            //final TitledPane algoPane = new TitledPane(s, new Button("Hello"));
            //algoPane.setPrefHeight(200);

            VBox algorithmPane = new VBox(20);
            final TropismModel tm = model.getModels().get(s);



            double[] x4ValidationScores = tm.getX4ValidationScores();
            double[] r5ValidationScores = tm.getR5ValidationScores();

            final double min = x4ValidationScores[0] < r5ValidationScores[0] ? x4ValidationScores[0] : r5ValidationScores[0];
            final double max = x4ValidationScores[x4ValidationScores.length - 1] > r5ValidationScores[r5ValidationScores.length - 1]
                    ? x4ValidationScores[x4ValidationScores.length - 1] : r5ValidationScores[r5ValidationScores.length - 1];

            int[] r5buckets = new int[17];
            int[] x4buckets = new int[17];

            double step = (max - min) / 16;
            double current = min;

            int b = 0;
            for (double d : x4ValidationScores) {
                System.out.println(d + "  " + current);
                while (d > current) {
                    b++;
                    current += step;
                    if (b > 16) {
                        b = 16;
                    }
                }
                x4buckets[b]++;
            }
            current = min;
            b = 0;
            for (double d : r5ValidationScores) {
                while (d > current) {
                    b++;
                    current += step;
                    if (b > 16) {
                        b = 16;
                    }
                }
                r5buckets[b]++;
            }
            current = min;
            XYChart.Series seriesX4 = new XYChart.Series();
            seriesX4.setName("CXCR4 Validation Scores");
            for (int i : x4buckets) {
                seriesX4.getData().add(new XYChart.Data(current, (double) i / (double) x4ValidationScores.length));
                current += step;
            }
            current = min;
            XYChart.Series seriesR5 = new XYChart.Series();
            seriesR5.setName("CCR5 Validation Scores");
            for (int i : r5buckets) {
                seriesR5.getData().add(new XYChart.Data(current, (double) i / (double) r5ValidationScores.length));
                current += step;
            }

            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            final AreaChart<Number, Number> ac =
                    new AreaChart<Number, Number>(xAxis, yAxis);
            ac.setTitle("Distribution of Validation Scores");
            ac.setPrefHeight(250);

            ac.getData().addAll(seriesR5, seriesX4);

            BorderPane chartBox = new BorderPane();
            final Line LV = LineBuilder.create()
                    .strokeWidth(2)
                    .stroke(Color.FORESTGREEN)
                    .build();
            chartBox.setCenter(ac);
            chartBox.getChildren().add(LV);


            //TableView fprTable = new TableView();


            BorderPane fprPane = new BorderPane();
            
            GridPane numericStatPane = new GridPane();
            
            final Label tprLabel = new Label("True Positive");
            final Tooltip tprTooltip = new Tooltip();
            tprTooltip.setText("TP: CCR5 sequences correctly classified as CCR5");
            tprLabel.setTooltip(tprTooltip);
            final Label tprCount = new Label();
            
            final Label fprLabel = new Label("False Positive");
            final Tooltip fprTooltip = new Tooltip();
            fprTooltip.setText("FP: CCR5 sequences incorrectly classified as CXCR4");
            fprLabel.setTooltip(fprTooltip);
            final Label fprCount = new Label();
            
            final Label tnrLabel = new Label("True Negative");
            final Tooltip tnrTooltip = new Tooltip();
            tnrTooltip.setText("True Negative: CXCR4 sequences correctly classified as CXCR4");
            tnrLabel.setTooltip(tnrTooltip);
            final Label tnrCount = new Label();
            
            final Label fnrLabel = new Label("False Negative");
            final Tooltip fnrTooltip = new Tooltip();
            fnrTooltip.setText("False Negative: CXCR4 sequences incorrectly classified as CCR5");
            fnrLabel.setTooltip(fnrTooltip);
            final Label fnrCount = new Label();
            
            final Label senLabel = new Label("Sensitivity");
            final Tooltip senTooltip = new Tooltip();
            senTooltip.setText("TP / (TP+FN)");
            senLabel.setTooltip(senTooltip);
            final Label sensitivity = new Label();
            
            final Label speLabel = new Label("Specificity");
            final Tooltip speTooltip = new Tooltip();
            speTooltip.setText("");
            speLabel.setTooltip(speTooltip);
            final Label specificity = new Label();
            
            final Label ppvLabel = new Label("PPV");
            final Tooltip ppvTooltip = new Tooltip();
            ppvTooltip.setText("Positive Predictive Value");
            ppvLabel.setTooltip(ppvTooltip);
            final Label ppv = new Label();
            
            final Label npvLabel = new Label("NPV");
            final Tooltip npvTooltip = new Tooltip();
            npvTooltip.setText("Negative Predictive Value");
            npvLabel.setTooltip(npvTooltip);
            final Label npv = new Label();
            
            final Label cutoffLabel = new Label("Cutoff");
            final Label cutoff = new Label();
            
            numericStatPane.setPadding(new Insets(10, 10, 10, 10));
            numericStatPane.setVgap(10);
            numericStatPane.setHgap(10);
            
            numericStatPane.add(tprLabel, 0, 0);
            numericStatPane.add(tprCount, 1, 0);
            
            numericStatPane.add(fprLabel, 0, 1);
            numericStatPane.add(fprCount, 1, 1);
            
            numericStatPane.add(tnrLabel, 0, 2);
            numericStatPane.add(tnrCount, 1, 2);
            
            numericStatPane.add(fnrLabel, 0, 3);
            numericStatPane.add(fnrCount, 1, 3);
            
            numericStatPane.add(senLabel, 0, 4);
            numericStatPane.add(sensitivity, 1, 4);
            
            numericStatPane.add(speLabel, 0, 5);
            numericStatPane.add(specificity, 1, 5);
            
            numericStatPane.add(ppvLabel, 0, 6);
            numericStatPane.add(ppv, 1, 6);
            
            numericStatPane.add(npvLabel, 0, 7);
            numericStatPane.add(npv, 1, 7);
            
            numericStatPane.add(cutoffLabel, 0, 8);
            numericStatPane.add(cutoff, 1, 8);
            
            DropShadow ds = new DropShadow();
            ds.setOffsetY(3.0);
            ds.setOffsetX(3.0);
            ds.setColor(Color.GRAY);
            
            final String css = "-fx-background-color: white;\n"
                + "-fx-border-insets: 5;\n"
                + "-fx-border-width: 3;\n";
            
            //numericStatPane.setStyle(css);
            numericStatPane.setEffect(ds);
            //numericStatPane.setGridLinesVisible(true);
            
            GridPane graphStatGrid = new GridPane();
            final ObservableList<PieChart.Data> ccr5ChartData =
                    FXCollections.observableArrayList(
                    new PieChart.Data("CCR5 classed as CCR5 (TP)", 0),
                    new PieChart.Data("CCR5 classed as CXCR4 (FP)", 0));
            final PieChart ccr5Chart = new PieChart(ccr5ChartData);
            ccr5Chart.setTitle("CCR5 Validation Data");
            ccr5Chart.setLabelsVisible(false);
            ccr5Chart.setPrefSize(250,250);
            ccr5Chart.setMinSize(250,250);
            ccr5Chart.setMaxSize(250,250);
            
            
            final ObservableList<PieChart.Data> cxcr4ChartData =
                    FXCollections.observableArrayList(
                    new PieChart.Data("CXCR4 classed as CXCR4 (TN)", 0),
                    new PieChart.Data("CXCR4 classed as CCR5 (FN)", 0));
            final PieChart cxcr4Chart = new PieChart(cxcr4ChartData);
            cxcr4Chart.setTitle("CXCR4 Validation Data");
            cxcr4Chart.setLabelsVisible(false);
            cxcr4Chart.setPrefSize(250,250);
            cxcr4Chart.setMinSize(250,250);
            cxcr4Chart.setMaxSize(250,250);

            final NumberAxis xAxis2 = new NumberAxis();
            final NumberAxis yAxis2 = new NumberAxis();
            xAxis2.setLabel("PPV (CCR5)");
            yAxis2.setLabel("NPV (CXCR4)");
            //creating the chart
            final LineChart<Number,Number> rocCurve = 
                    new LineChart<Number,Number>(xAxis2,yAxis2);

            rocCurve.setTitle("ROC Curve");
            XYChart.Series series = new XYChart.Series();
            series.setName(s);
            
            for (double i=0; i<=1; i+=0.01) {
                Number[] stats = tm.getStatsforFPR(i);
                series.getData().add(new XYChart.Data(stats[7],stats[8]));
            }

            rocCurve.getData().add(series);
            rocCurve.setCreateSymbols(false);
            rocCurve.setPrefSize(300,250);
            rocCurve.setMaxSize(300,250);
            rocCurve.setMinSize(300,250);
            rocCurve.setLegendVisible(false);
            
            graphStatGrid.add(ccr5Chart, 0, 0);
            graphStatGrid.add(cxcr4Chart, 1, 0);
            graphStatGrid.add(rocCurve, 2, 0);
            graphStatGrid.setAlignment(Pos.CENTER);
            
            
            
            
            GridPane sliderPane = new GridPane();
            //sliderPane.setSpacing(10);
            final Label sliderLabel = new Label("False Positive Rate:");
            sliderLabel.setFont(Font.font(null, FontWeight.BOLD, 20));
            final Slider fprSlider = new Slider();
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
            final Label fprValue = new Label(String.format("%.2f", fprSlider.getValue()));
            fprSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    fprValue.setText(String.format("%.2f", newValue));
                    NumberAxis xAxis = (NumberAxis) ac.getXAxis();
                    NumberAxis yAxis = (NumberAxis) ac.getYAxis();
                    double xLower = xAxis.getLowerBound();
                    double xUpper = xAxis.getUpperBound();
                    double yLower = yAxis.getLowerBound();
                    double yUpper = yAxis.getUpperBound();
                    Node chartPlotArea = ac.lookup(".chart-plot-background");
                    double xAxisShift = chartPlotArea.getLayoutX();
                    double yAxisShift = chartPlotArea.getLayoutY();

                    Number[] stats = tm.getStatsforFPR(newValue.doubleValue());
                    double score = (Double) stats[0];
                    double scoreFpr = (score - min) / (max - min);

                    LV.setStartX(xAxis.getDisplayPosition(xLower) + xAxisShift + 5 + scoreFpr * xAxis.getDisplayPosition(xUpper));
                    LV.setEndX(xAxis.getDisplayPosition(xLower) + xAxisShift + 5 + scoreFpr * xAxis.getDisplayPosition(xUpper));
                    LV.setStartY(yAxis.getDisplayPosition(0) + yAxisShift + 27);
                    LV.setEndY(yAxis.getDisplayPosition(yUpper) + yAxisShift + 27);
                    
                    tprCount.setText(Integer.toString((Integer)stats[1]));
                    fprCount.setText(Integer.toString((Integer)stats[2]));
                    tnrCount.setText(Integer.toString((Integer)stats[3]));
                    fnrCount.setText(Integer.toString((Integer)stats[4]));
                    sensitivity.setText(String.format("%.2f", (Double)stats[5]));
                    specificity.setText(String.format("%.2f", (Double)stats[6]));
                    ppv.setText(String.format("%.2f", (Double)stats[7]));
                    npv.setText(String.format("%.2f", (Double)stats[8]));
                    cutoff.setText(String.format("%.2f", (Double)stats[0]));
                    
                    
                    ccr5ChartData.get(0).setPieValue((Integer)stats[1]);
                    ccr5ChartData.get(1).setPieValue((Integer)stats[2]);
                    
                    cxcr4ChartData.get(0).setPieValue((Integer)stats[3]);
                    cxcr4ChartData.get(1).setPieValue((Integer)stats[4]);
                    
                    /*ObservableList<PieChart.Data> ccr5ChartData =
                            FXCollections.observableArrayList(
                            new PieChart.Data("CCR5 classed as CCR5 (TP)", (Integer)stats[1]),
                            new PieChart.Data("CCR5 classed as CXCR4 (FP)", (Integer)stats[2]));
                    ccr5Chart.setData(ccr5ChartData);

                    ObservableList<PieChart.Data> cxcr4ChartData =
                            FXCollections.observableArrayList(
                            new PieChart.Data("CXCR4 classed as CXCR4 (TN)", (Integer)stats[3]),
                            new PieChart.Data("CXCR4 classed as CCR5 (FN)", (Integer)stats[4]));
                    cxcr4Chart.setData(cxcr4ChartData);*/
                }
            });
            
            fprValue.setText(String.format("%.2f", 0.05));
            xAxis = (NumberAxis) ac.getXAxis();
            yAxis = (NumberAxis) ac.getYAxis();
            double xLower = xAxis.getLowerBound();
            double xUpper = xAxis.getUpperBound();
            double yLower = yAxis.getLowerBound();
            double yUpper = yAxis.getUpperBound();
            Node chartPlotArea = ac.lookup(".chart-plot-background");
            double xAxisShift = chartPlotArea.getLayoutX();
            double yAxisShift = chartPlotArea.getLayoutY();

            Number[] stats = tm.getStatsforFPR(0.05);
            double score = (Double) stats[0];
            double scoreFpr = (score - min) / (max - min);

            LV.setStartX(xAxis.getDisplayPosition(xLower) + xAxisShift + 5 + scoreFpr * xAxis.getDisplayPosition(xUpper));
            LV.setEndX(xAxis.getDisplayPosition(xLower) + xAxisShift + 5 + scoreFpr * xAxis.getDisplayPosition(xUpper));
            LV.setStartY(yAxis.getDisplayPosition(0) + yAxisShift + 27);
            LV.setEndY(yAxis.getDisplayPosition(yUpper) + yAxisShift + 27);

            tprCount.setText(Integer.toString((Integer)stats[1]));
            fprCount.setText(Integer.toString((Integer)stats[2]));
            tnrCount.setText(Integer.toString((Integer)stats[3]));
            fnrCount.setText(Integer.toString((Integer)stats[4]));
            sensitivity.setText(String.format("%.2f", (Double)stats[5]));
            specificity.setText(String.format("%.2f", (Double)stats[6]));
            ppv.setText(String.format("%.2f", (Double)stats[7]));
            npv.setText(String.format("%.2f", (Double)stats[8]));
            cutoff.setText(String.format("%.2f", (Double)stats[0]));


            ccr5ChartData.get(0).setPieValue((Integer)stats[1]);
            ccr5ChartData.get(1).setPieValue((Integer)stats[2]);

            cxcr4ChartData.get(0).setPieValue((Integer)stats[3]);
            cxcr4ChartData.get(1).setPieValue((Integer)stats[4]);
            
            sliderPane.setPadding(new Insets(10, 10, 10, 10));
            sliderPane.setVgap(10);
            sliderPane.setHgap(70);
            sliderPane.add(sliderLabel, 0, 0);
            sliderPane.add(fprSlider, 1, 0);
            sliderPane.add(fprValue, 2, 0);
            
            DropShadow ds2 = new DropShadow();
            ds.setOffsetY(3.0);
            ds.setOffsetX(3.0);
            ds.setColor(Color.GRAY);
            
            final String css2 = "-fx-background-color: white;\n"
                + "-fx-border-insets: 5;\n"
                + "-fx-border-width: 3;\n";
            
            sliderPane.setStyle(css2);
            sliderPane.setEffect(ds2);
            
            //sliderPane.getChildren().addAll(sliderLabel, fprSlider, fprValue);
            fprPane.setTop(sliderPane);
            fprPane.setLeft(numericStatPane);
            fprPane.setCenter(graphStatGrid);
            algorithmPane.getChildren().add(fprPane);


            algorithmPane.getChildren().add(chartBox);

            summaryPanes.getChildren().add(new TitledPane(s, algorithmPane));
            
            //summaryPanes.getChildren().add(algoPane);
        }

        viewPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                summaryPanes.setPrefWidth(bounds.getWidth());
            }
        });

        viewPane.setContent(summaryPanes);

        /*final ScrollPane scroll = new ScrollPane();
         scroll.setContent(summaryPanes);
        
         scroll.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
         @Override
         public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
         summaryPanes.setPrefWidth(bounds.getWidth());
         summaryPanes.setPrefHeight(bounds.getHeight());
         }
         });
        
         scroll.setPrefHeight(viewPane.getHeight());
         scroll.setMaxHeight(Short.MAX_VALUE);
         scroll.setPrefWidth(viewPane.getWidth());
         scroll.setMaxWidth(Short.MAX_VALUE);
        
        
         viewPane.getChildren().clear();
         viewPane.getChildren().add(scroll);*/
    }

    private static double getSceneShift(Node node) {
        double shift = 0;
        do {
            shift += node.getLayoutX();
            node = node.getParent();
        } while (node != null);
        return shift;
    }

    private void openFasta(String name, List<Sequence> seqs) {
        FileSystem mFS = FileUtil.createMemoryFileSystem();
        try {
            String fastaString = "";
            for (Sequence s : seqs) {
                fastaString += ">" + s.getName() + "\n";
                fastaString += s.getSequence() + "\n";
            }
            FileObject fastaFile = mFS.getRoot().createData(name, "fasta");
            PrintWriter pw = new PrintWriter(fastaFile.getOutputStream());
            pw.write(fastaString);
            pw.close();
            DataObject fastaDataObject = TroGenResultsDataObject.find(fastaFile);
            EditorCookie cookie = fastaDataObject.getLookup().lookup(EditorCookie.class);
            cookie.open();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
