package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Module;

public class SelectModulesPane extends VBox {

    private ListView<Module> block1Modules;
    private ListView<Module> block2Modules;
    private ListView<Module> block3_4ModulesSelected;
    private ListView<Module> block3_4ModulesUnselected;
    private Button btnAdd, btnRemove, btnReset, btnSubmit;
    private TextField currentCreditstxt;

    public SelectModulesPane() {

        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));

        GridPane mainGridPane = new GridPane();
        mainGridPane.setHgap(10);
        mainGridPane.setVgap(10);
        mainGridPane.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        mainGridPane.getColumnConstraints().addAll(col1, col2);

        block1Modules = new ListView<>();
        block2Modules = new ListView<>();
        block3_4ModulesUnselected = new ListView<>();
        block3_4ModulesSelected = new ListView<>();

        Label lblBlock1 = new Label("Selected Block 1 modules");
        Label lblBlock2 = new Label("Selected Block 2 modules");

        mainGridPane.add(lblBlock1, 0, 0);
        mainGridPane.add(block1Modules, 0, 1);
        mainGridPane.add(lblBlock2, 0, 2);
        mainGridPane.add(block2Modules, 0, 3);

        VBox secondColumnVBox = new VBox(10);
        secondColumnVBox.setAlignment(Pos.CENTER);
        secondColumnVBox.setFillWidth(true);

        Label lblBlock3_4Unselected = new Label("Unselected Block 3/4 modules");
        Label lblBlock3_4Selected = new Label("Selected Block 3/4 modules");

        btnAdd = new Button("Add");
        btnRemove = new Button("Remove");
        btnReset = new Button("Reset");
        btnSubmit = new Button("Submit");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        buttonBox.getChildren().addAll(btnAdd, btnRemove);
        
        secondColumnVBox.getChildren().addAll(lblBlock3_4Unselected, block3_4ModulesUnselected, buttonBox, lblBlock3_4Selected, block3_4ModulesSelected);

        mainGridPane.add(secondColumnVBox, 1, 0, 1, 4);

        GridPane.setHalignment(lblBlock1, HPos.CENTER);
        GridPane.setHalignment(lblBlock2, HPos.CENTER);
        GridPane.setHalignment(lblBlock3_4Unselected, HPos.CENTER);
        GridPane.setHalignment(lblBlock3_4Selected, HPos.CENTER);

        HBox creditsBox = new HBox(10);
        creditsBox.setAlignment(Pos.CENTER_RIGHT);
        Label currentCreditsLbl = new Label("Current credits:");
        currentCreditstxt = new TextField("0");
        currentCreditstxt.setEditable(false);
        creditsBox.getChildren().addAll(currentCreditsLbl, currentCreditstxt);
        creditsBox.setAlignment(Pos.CENTER);

        HBox bottomButtonBox = new HBox(10, btnReset, btnSubmit);
        bottomButtonBox.setAlignment(Pos.CENTER);

        this.getChildren().addAll(mainGridPane, creditsBox, bottomButtonBox);
    }

    public ListView<Module> getBlock1Modules() { return block1Modules; }
    public ListView<Module> getBlock2Modules() { return block2Modules; }
    public ListView<Module> getBlock3_4ModulesSelected() { return block3_4ModulesSelected; }
    public ListView<Module> getBlock3_4ModulesUnselected() { return block3_4ModulesUnselected; }

    public Button getAddButton() { return btnAdd; }
    public Button getRemoveButton() { return btnRemove; }
    public Button getResetButton() { return btnReset; }
    public Button getSubmitButton() { return btnSubmit; }
    public TextField getCurrentCreditsField() { return currentCreditstxt; }

    public void setAddButtonHandler(EventHandler<ActionEvent> handler) {
        btnAdd.setOnAction(handler);
    }

    public void setRemoveButtonHandler(EventHandler<ActionEvent> handler) {
        btnRemove.setOnAction(handler);
    }

    public void setResetButtonHandler(EventHandler<ActionEvent> handler) {
        btnReset.setOnAction(handler);
    }

    public void setSubmitButtonHandler(EventHandler<ActionEvent> handler) {
        btnSubmit.setOnAction(handler);
    }

}