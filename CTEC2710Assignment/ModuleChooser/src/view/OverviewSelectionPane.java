package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class OverviewSelectionPane extends VBox {

    private TextArea profileDetailsArea;
    private TextArea selectedModulesArea;
    private TextArea reservedModulesArea;
    private Button btnSaveOverview;

    public OverviewSelectionPane() {
    	
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));

        profileDetailsArea = new TextArea();
        profileDetailsArea.setEditable(false);
        profileDetailsArea.setPrefHeight(100);

        selectedModulesArea = new TextArea();
        selectedModulesArea.setEditable(false);
        selectedModulesArea.setPrefHeight(150);

        reservedModulesArea = new TextArea();
        reservedModulesArea.setEditable(false);
        reservedModulesArea.setPrefHeight(150);

        btnSaveOverview = new Button("Save Overview");

        this.getChildren().addAll(
            new Label("Profile Details:"),
            profileDetailsArea,
            new Label("Selected modules:"),
            selectedModulesArea,
            new Label("Reserved modules:"),
            reservedModulesArea,
            btnSaveOverview
        );
    }

    public TextArea getProfileDetailsArea() {
        return profileDetailsArea;
    }

    public TextArea getSelectedModulesArea() {
        return selectedModulesArea;
    }

    public TextArea getReservedModulesArea() {
        return reservedModulesArea;
    }

    public Button getSaveOverviewButton() {
        return btnSaveOverview;
    }
}