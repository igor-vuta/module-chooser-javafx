package view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Module;

public class ReserveModulesPane extends VBox {

    private ListView<Module> lvUnselectedModules;
    private ListView<Module> lvReservedModules;
    private Button btnAdd, btnRemove, btnConfirm;

    public ReserveModulesPane() {
    	
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));

        lvUnselectedModules = new ListView<>();
        lvReservedModules = new ListView<>();

        VBox.setVgrow(lvUnselectedModules, Priority.ALWAYS);
        VBox.setVgrow(lvReservedModules, Priority.ALWAYS);
        GridPane.setHgrow(lvUnselectedModules, Priority.ALWAYS);
        GridPane.setHgrow(lvReservedModules, Priority.ALWAYS);

        Label lblUnselectedModules = new Label("Unselected Block 3/4 modules");
        Label lblReservedModules = new Label("Reserved Block 3/4 modules");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(lblUnselectedModules, 0, 0);
        gridPane.add(lvUnselectedModules, 0, 1);
        gridPane.add(lblReservedModules, 1, 0);
        gridPane.add(lvReservedModules, 1, 1);

        GridPane.setHalignment(lblUnselectedModules, HPos.CENTER);
        GridPane.setHalignment(lblReservedModules, HPos.CENTER);

        GridPane.setVgrow(lvUnselectedModules, Priority.ALWAYS);
        GridPane.setVgrow(lvReservedModules, Priority.ALWAYS);

        btnAdd = new Button("Add");
        btnRemove = new Button("Remove");
        btnConfirm = new Button("Confirm");

        HBox buttonBox = new HBox(10, btnAdd, btnRemove, btnConfirm);
        buttonBox.setAlignment(Pos.CENTER);

        this.getChildren().addAll(gridPane, buttonBox);
        VBox.setVgrow(gridPane, Priority.ALWAYS); 
    }

    public ListView<Module> getUnselectedModulesListView() {
        return lvUnselectedModules;
    }

    public ListView<Module> getReservedModulesListView() {
        return lvReservedModules;
    }

    public Button getAddButton() {
        return btnAdd;
    }

    public Button getRemoveButton() {
        return btnRemove;
    }

    public Button getConfirmButton() {
        return btnConfirm;
    }
}