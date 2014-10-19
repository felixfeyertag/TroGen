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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.ActionID;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.NodeList;

@MultiViewElement.Registration(
        displayName = "#LBL_TroGenResults_VISUAL",
        iconBase = "org/mcr/trogen/filetype/tg.png",
        mimeType = "text/trogen+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "TroGenResultsVisual",
        position = 2000)
@TopComponent.Description(
        preferredID = "TroGenResultsViewTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TrogenResultsViewAction",
        preferredID = "TrogenResultsViewTopComponent")
@Messages({
    "LBL_TroGenResults_VISUAL=Visual",
    "CTL_TrogenResultsViewAction=Tropism Classification Results",
    "CTL_TrogenResultsViewTopComponent=Tropism Classification Results",
    "HINT_TrogenResulsViewTopComponent=Tropism Classification Results"
})
public final class TroGenResultsVisualElement extends JPanel implements MultiViewElement {

    private TroGenResultsDataObject obj;
    private JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;
    private static JFXPanel fxContainer;

    public TroGenResultsVisualElement(Lookup lkp) {
        obj = lkp.lookup(TroGenResultsDataObject.class);
        assert obj != null;
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
        //initComponents();
    }

    @Override
    public String getName() {
        return "TroGenResultsVisualElement";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 388, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 288, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void createScene() {

        GridPane sliderPane = new GridPane();
        final Label sliderLabel = new Label("False Positive Rate:");
        sliderLabel.setFont(Font.font(null, FontWeight.BOLD, 20));

        sliderPane.setPadding(new Insets(10, 10, 10, 10));
        sliderPane.setVgap(10);
        sliderPane.setHgap(70);
        sliderPane.add(sliderLabel, 0, 0);
        sliderPane.add(obj.getFprSlider(), 1, 0);
        sliderPane.add(obj.getFprValue(), 2, 0);

        DropShadow ds2 = new DropShadow();

        final String css2 = "-fx-background-color: white;\n"
                + "-fx-border-insets: 5;\n"
                + "-fx-border-width: 3;\n";

        sliderPane.setStyle(css2);
        sliderPane.setEffect(ds2);

        BorderPane resultsScreen = new BorderPane();
        resultsScreen.setTop(sliderPane);
        resultsScreen.setCenter(obj.getResultsTable());

        fxContainer.setScene(new Scene(resultsScreen));

    }

    private VBox createStackedTitledPanes() {
        final VBox stackedTitledPanes = new VBox();
        NodeList nodes = obj.getXmlData().getChildNodes().item(0).getChildNodes();

        int counter = 1;
        for (int i = 1; i < nodes.getLength(); i += 2) {
            String name = "";
            NodeList currentNode = nodes.item(i).getChildNodes();

            CheckBoxTreeItem<String> resultItem = new CheckBoxTreeItem<String>("Unnamed sequence" + counter);
            counter++;
            for (int j = 0; j < currentNode.getLength(); j++) {
                if (currentNode.item(j).getNodeName().equals("Name")) {
                    name = currentNode.item(j).getTextContent();
                } else if (currentNode.item(j).getNodeName().equals("Sequence")) {
                    //System.out.println("Name: " + currentNode.item(j).getTextContent());
                    TreeItem<String> item = new TreeItem<String>("Input Sequence");
                    TreeItem<String> itemContent = new TreeItem<String>(currentNode.item(j).getTextContent());
                    item.getChildren().add(itemContent);
                    item.setExpanded(true);
                    resultItem.getChildren().add(item);
                } else if (currentNode.item(j).getNodeName().equals("MappedAlignment")) {
                    //System.out.println("Name: " + currentNode.item(j).getTextContent());
                    TreeItem<String> item = new TreeItem<String>("Mapped Sequence");
                    TreeItem<String> itemContent = new TreeItem<String>(currentNode.item(j).getTextContent());
                    item.getChildren().add(itemContent);
                    item.setExpanded(true);
                    resultItem.getChildren().add(item);
                }
            }
            resultItem.setValue(name);
            resultItem.setExpanded(true);

            TreeView<String> tree = new TreeView<String>(resultItem);
            tree.setPrefHeight(100);
            tree.setShowRoot(false);

            stackedTitledPanes.getChildren().add(createTitledPane(name, tree));
        }
        ((TitledPane) stackedTitledPanes.getChildren().get(0)).setExpanded(true);
        stackedTitledPanes.getStyleClass().add("summary-panes");

        return stackedTitledPanes;
    }

    public TitledPane createTitledPane(String title, Node... images) {
        StackPane content = new StackPane();
        for (Node image : images) {
            content.getChildren().add(image);
            StackPane.setMargin(image, new Insets(10));
        }
        content.setAlignment(Pos.TOP_CENTER);
        TitledPane pane = new TitledPane(title, content);
        pane.getStyleClass().add("summary-pane");
        pane.setExpanded(true);

        return pane;
    }

    private ScrollPane makeScrollable(final VBox node) {
        final ScrollPane scroll = new ScrollPane();
        scroll.setContent(node);
        scroll.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
                node.setPrefWidth(bounds.getWidth());
                //node.setPrefHeight(bounds.getHeight());
            }
        });
        return scroll;
    }
}