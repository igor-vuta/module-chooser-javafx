package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import model.Block;
import model.Course;
import model.Module;
import model.Name;
import model.StudentProfile;
import view.ModuleChooserRootPane;
import view.OverviewSelectionPane;
import view.CreateStudentProfilePane;

public class ModuleChooserController {

	private StudentProfile model;
	private ModuleChooserRootPane view;
	
	private CreateStudentProfilePane cspp;

	public ModuleChooserController(StudentProfile model, ModuleChooserRootPane view) {

		this.model = model;
		this.view = view;
		
		cspp = view.getCreateStudentProfilePane();

		cspp.addCourseDataToComboBox(generateAndGetCourses());

		this.attachCreateStudentProfileHandler();
		this.attachResetHandler();
		this.attachAddHandlers();
		this.attachRemoveHandlers();
		this.attachSubmitHandler();
		this.attachConfirmHandler();
		this.attachSaveOverviewHandler();
		this.attachSaveHandler();
		this.attachLoadHandler();
		this.attachAboutHandler();
		this.attachExitHandler();
	}
	
	// Create Student Profile Handler
	private void attachCreateStudentProfileHandler() {
	    cspp.addCreateStudentProfileHandler(event -> {
	        Course selectedCourse = cspp.getSelectedCourse();
	        String pNumber = cspp.getStudentPnumber();
	        Name studentName = cspp.getStudentName();
	        String email = cspp.getStudentEmail();
	        LocalDate date = cspp.getStudentDate();

	        if (pNumber.isEmpty() || !pNumber.toLowerCase().startsWith("p")) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Invalid P Number");
	            alert.setHeaderText(null);
	            alert.setContentText("The P number must start with the letter 'p'.");
	            alert.showAndWait();
	            return;
	        }

	        if (!studentName.getFirstName().matches("^[A-Z][a-zA-Z-]*$") || 
	            !studentName.getFamilyName().matches("^[A-Z][a-zA-Z-]*$")) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Invalid Name");
	            alert.setHeaderText(null);
	            alert.setContentText("First name and surname must start with a capital letter and contain only latin characters.");
	            alert.showAndWait();
	            return;
	        }

	        if (!email.contains("@")) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Invalid Email");
	            alert.setHeaderText(null);
	            alert.setContentText("The email must contain an '@' symbol.");
	            alert.showAndWait();
	            return;
	        }
	        
	        if (date == null) {
	        	Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Empty date");
	            alert.setHeaderText(null);
	            alert.setContentText("You should select date.");
	            alert.showAndWait();
	            return;
	        }

	        if (selectedCourse != null) {
	            model.setStudentCourse(selectedCourse);
	            model.setStudentPnumber(pNumber);
	            model.setStudentName(studentName);
	            model.setStudentEmail(email);
	            model.setSubmissionDate(date);
	            model.clearSelectedModules();
	            model.clearReservedModules();

	            populateSelectModulesPane(selectedCourse);
	            populateOverviewPane();
	            updateCurrentCredits();
	            view.getReserveModulesPane().getUnselectedModulesListView().getItems().clear();
	            view.getReserveModulesPane().getReservedModulesListView().getItems().clear();
	            view.changeTab(1);
	        }
	    });
	}

	// Reset Handler
	private void attachResetHandler() {
	    view.getSelectModulesPane().setResetButtonHandler(event -> {
	        view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().clear();
	        view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().clear();
	        populateSelectModulesPane(model.getStudentCourse());
	        updateCurrentCredits();
	    });
	}
	
	// Add Handlers
	private void attachAddHandlers() {
	    view.getSelectModulesPane().setAddButtonHandler(event -> {
	        Module selectedModule = view.getSelectModulesPane().getBlock3_4ModulesUnselected().getSelectionModel().getSelectedItem();
	        if (selectedModule != null) {
	            view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().remove(selectedModule);
	            view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().add(selectedModule);
	            updateCurrentCredits();
	        }
	    });
	    
	    view.getReserveModulesPane().getAddButton().setOnAction(event -> {
	        Module selectedModule = view.getReserveModulesPane().getUnselectedModulesListView().getSelectionModel().getSelectedItem();
	        if (selectedModule != null) {
	            view.getReserveModulesPane().getUnselectedModulesListView().getItems().remove(selectedModule);
	            view.getReserveModulesPane().getReservedModulesListView().getItems().add(selectedModule);
	        }
	    });
	}
	
	// Remove Handlers
	private void attachRemoveHandlers() {
		view.getSelectModulesPane().setRemoveButtonHandler(event -> {
	        Module selectedModule = view.getSelectModulesPane().getBlock3_4ModulesSelected().getSelectionModel().getSelectedItem();
	        if (selectedModule != null) {
	            view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().remove(selectedModule);
	            view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().add(selectedModule);
	            updateCurrentCredits();
	        }
	    });
		
		view.getReserveModulesPane().getRemoveButton().setOnAction(event -> {
	        Module selectedModule = view.getReserveModulesPane().getReservedModulesListView().getSelectionModel().getSelectedItem();
	        if (selectedModule != null) {
	            view.getReserveModulesPane().getReservedModulesListView().getItems().remove(selectedModule);
	            view.getReserveModulesPane().getUnselectedModulesListView().getItems().add(selectedModule);
	        }
	    });
	}
	
	// Submit Handler
	private void attachSubmitHandler() {
	    view.getSelectModulesPane().setSubmitButtonHandler(event -> {
	        int totalCredits;
	        try {
	            totalCredits = Integer.parseInt(view.getSelectModulesPane().getCurrentCreditsField().getText());
	        } catch (NumberFormatException e) {
	            totalCredits = 0;
	        }

	        if (totalCredits != 120) {
	            Platform.runLater(() -> {
	                Alert alert = new Alert(Alert.AlertType.ERROR);
	                alert.setTitle("Score error.");
	                alert.setHeaderText(null);
	                alert.setContentText("Adjust your modules so score is 120.");
	                alert.showAndWait();
	            });
	        } else {
	            model.clearSelectedModules();
	            view.getSelectModulesPane().getBlock1Modules().getItems().forEach(model::addSelectedModule);
	            view.getSelectModulesPane().getBlock2Modules().getItems().forEach(model::addSelectedModule);
	            view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().forEach(model::addSelectedModule);

	            view.getReserveModulesPane().getUnselectedModulesListView().getItems().clear();
	            view.getReserveModulesPane().getReservedModulesListView().getItems().clear();
	            ObservableList<Module> unselectedModules = view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems();
	            view.getReserveModulesPane().getUnselectedModulesListView().getItems().addAll(unselectedModules);
	            model.getAllReservedModules().clear();
	            populateOverviewPane();
	            view.changeTab(2);
	        }
	    });
	}

	// Confirm Handler
	private void attachConfirmHandler() {
	    view.getReserveModulesPane().getConfirmButton().setOnAction(event -> {
	        Platform.runLater(() -> {
	            if (view.getReserveModulesPane().getReservedModulesListView().getItems().size() != 1) {
	                Alert alert = new Alert(Alert.AlertType.ERROR);
	                alert.setTitle("Only one reserved module.");
	                alert.setHeaderText(null);
	                alert.setContentText("You should choose only one module to be reserved.");
	                alert.showAndWait();
	            } else {
	                model.clearReservedModules();
	                view.getReserveModulesPane().getReservedModulesListView().getItems().forEach(model::addReservedModule);
	                populateOverviewPane();
	                view.changeTab(3);
	            }
	        });
	    });
	}

	// Save Overview Handler
	private void attachSaveOverviewHandler() {
	    view.getOverviewSelectionPane().getSaveOverviewButton().setOnAction(event -> {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Save Overview");
	        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
	        fileChooser.setInitialFileName("overview.txt");

	        File file = fileChooser.showSaveDialog(null);

	        if (file != null) {
	            try (PrintWriter out = new PrintWriter(file)) {
	                String overviewText = view.getOverviewSelectionPane().getProfileDetailsArea().getText() + "\n\n"
	                                    + view.getOverviewSelectionPane().getSelectedModulesArea().getText() + "\n\n"
	                                    + view.getOverviewSelectionPane().getReservedModulesArea().getText();
	                out.println(overviewText);

	                Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
	                saveAlert.setTitle("Save Successful");
	                saveAlert.setHeaderText(null);
	                saveAlert.setContentText("Overview was saved to " + file.getAbsolutePath());
	                saveAlert.showAndWait();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                errorAlert.setTitle("Save Failed");
	                errorAlert.setHeaderText(null);
	                errorAlert.setContentText("An error occurred while saving the overview.");
	                errorAlert.showAndWait();
	            }
	        }
	    });
	}

	// Save Profile Handler
	private void attachSaveHandler() {
	    view.getModuleSelectionToolMenuBar().addSaveHandler(e -> {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Save Student Profile");
	        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary Files", "*.bin"));
	        fileChooser.setInitialFileName("student_profile.bin");

	        File file = fileChooser.showSaveDialog(null);

	        if (file != null) {
	            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
	                oos.writeObject(model);
	                Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
	                saveAlert.setTitle("Save Successful");
	                saveAlert.setHeaderText(null);
	                saveAlert.setContentText("Student data was saved successfully.");
	                saveAlert.showAndWait();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                errorAlert.setTitle("Save Failed");
	                errorAlert.setHeaderText(null);
	                errorAlert.setContentText("An error occurred.");
	                errorAlert.showAndWait();
	            }
	        }
	    });
	}

	// Load Student Data Handler
	private void attachLoadHandler() {
	    view.getModuleSelectionToolMenuBar().addLoadHandler(e -> {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Open Student Profile");
	        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary Files", "*.bin"));

	        File file = fileChooser.showOpenDialog(null);

	        if (file != null) {
	            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
	                model = (StudentProfile) ois.readObject();
	                populateViewWithProfileData();
	                Alert loadAlert = new Alert(Alert.AlertType.INFORMATION);
	                loadAlert.setTitle("Load Successful");
	                loadAlert.setHeaderText(null);
	                loadAlert.setContentText("Data was loaded successfully.");
	                loadAlert.showAndWait();
	            } catch (IOException | ClassNotFoundException ex) {
	                ex.printStackTrace();
	                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
	                errorAlert.setTitle("Load Failed");
	                errorAlert.setHeaderText(null);
	                errorAlert.setContentText("An error occurred.");
	                errorAlert.showAndWait();
	            }
	        }
	    });
	}
	
	// About Handler
	private void attachAboutHandler() {
	    view.getModuleSelectionToolMenuBar().addAboutHandler(e -> {
	        Alert aboutAlert = new Alert(AlertType.INFORMATION);
	        aboutAlert.setTitle("About");
	        aboutAlert.setHeaderText("Course and Module Selection Tool");
	        aboutAlert.setContentText("This application allows students to select their modules, based on the course "
	                                  + "of the study, and saves their profile.\n\n"
	                                  + "Developer: Igor Vuta");
	        aboutAlert.showAndWait(); 
	    });
	}
	
	// Exit Handler
	private void attachExitHandler() {
	    view.getModuleSelectionToolMenuBar().addExitHandler(e -> System.exit(0));
	}
	
	// Method that is utilized to populate view when reset button is pressed
	private void populateSelectModulesPane(Course selectedCourse) {

	    view.getSelectModulesPane().getBlock1Modules().getItems().clear();
	    view.getSelectModulesPane().getBlock2Modules().getItems().clear();
	    view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().clear();
	    view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().clear();

	    for (Module module : selectedCourse.getAllModulesOnCourse()) {
	        if (module.getRunPlan() == Block.BLOCK_1) {
	            view.getSelectModulesPane().getBlock1Modules().getItems().add(module);
	        } else if (module.getRunPlan() == Block.BLOCK_2) {
	            view.getSelectModulesPane().getBlock2Modules().getItems().add(module);
	        } else if (module.getRunPlan() == Block.BLOCK_3_4) {
	            view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().add(module);
	        }
	    }
	}
	
	// This method populates overview pane with data from previous panes
		private void populateOverviewPane() {
			
		    OverviewSelectionPane overviewPane = view.getOverviewSelectionPane();

		    String profileDetails = "Name: " + model.getStudentName().getFullName() + "\n"
		                          + "PNo: " + model.getStudentPnumber() + "\n"
		                          + "Email: " + model.getStudentEmail() + "\n"
		                          + "Date: " + model.getSubmissionDate() + "\n"
		                          + "Course: " + model.getStudentCourse().getCourseName();
		    overviewPane.getProfileDetailsArea().setText(profileDetails);
		    
		    StringBuilder selectedModulesText = new StringBuilder("Selected modules:\n==========\n");
		    
		    for (Module module : model.getAllSelectedModules()) {
		        selectedModulesText.append("Module code: ").append(module.getModuleCode())
		                           .append(", Module name: ").append(module.getModuleName())
		                           .append(", Credits: ").append(module.getModuleCredits())
		                           .append(", Mandatory on your course? ").append(module.isMandatory())
		                           .append(", Block: ").append(module.getRunPlan()).append("\n");
		    }
		    overviewPane.getSelectedModulesArea().setText(selectedModulesText.toString());

		    StringBuilder reservedModulesText = new StringBuilder("Reserved modules:\n==========\n");
		    for (Module module : model.getAllReservedModules()) {
		        reservedModulesText.append("Module code: ").append(module.getModuleCode())
		                           .append(", Module name: ").append(module.getModuleName())
		                           .append(", Credits: ").append(module.getModuleCredits())
		                           .append(", Block: ").append(module.getRunPlan()).append("\n");
		    }
		    overviewPane.getReservedModulesArea().setText(reservedModulesText.toString());
		}
		
	// Method to populate view with data from model
	private void populateViewWithProfileData() {

	    view.getCreateStudentProfilePane().setStudentPnumber(model.getStudentPnumber());
	    view.getCreateStudentProfilePane().setStudentName(model.getStudentName());
	    view.getCreateStudentProfilePane().setStudentEmail(model.getStudentEmail());
	    view.getCreateStudentProfilePane().setSubmissionDate(model.getSubmissionDate());

	    view.getCreateStudentProfilePane().setSelectedCourse(model.getStudentCourse());

	    view.getSelectModulesPane().getBlock1Modules().getItems().setAll(
	        model.getAllSelectedModules().stream()
	            .filter(module -> module.getRunPlan() == Block.BLOCK_1)
	            .toList()
	    );

	    view.getSelectModulesPane().getBlock2Modules().getItems().setAll(
	        model.getAllSelectedModules().stream()
	            .filter(module -> module.getRunPlan() == Block.BLOCK_2)
	            .toList()
	    );

	    view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().setAll(
	        model.getAllSelectedModules().stream()
	            .filter(module -> module.getRunPlan() == Block.BLOCK_3_4)
	            .toList()
	    );

	    view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems().setAll(
	        model.getStudentCourse().getAllModulesOnCourse().stream()
	            .filter(module -> module.getRunPlan() == Block.BLOCK_3_4 && 
	                !model.getAllSelectedModules().contains(module))
	            .toList()
	    );

	    model.clearSelectedModules();
		view.getSelectModulesPane().getBlock1Modules().getItems().forEach(model::addSelectedModule);
		view.getSelectModulesPane().getBlock2Modules().getItems().forEach(model::addSelectedModule);
		view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().forEach(model::addSelectedModule);
        view.getReserveModulesPane().getReservedModulesListView().getItems().addAll(model.getAllReservedModules());
        
        ObservableList<Module> unselectedModules = view.getSelectModulesPane().getBlock3_4ModulesUnselected().getItems();
        Set<Module> reservedModules = model.getAllReservedModules();

        ObservableList<Module> filteredUnselectedModules = FXCollections.observableArrayList(unselectedModules);
        filteredUnselectedModules.removeAll(reservedModules);

        view.getReserveModulesPane().getUnselectedModulesListView().getItems().clear();
        view.getReserveModulesPane().getUnselectedModulesListView().getItems().addAll(filteredUnselectedModules);
        
	    int totalCredits = model.getAllSelectedModules().stream()
	        .mapToInt(Module::getModuleCredits)
	        .sum();
	    view.getSelectModulesPane().getCurrentCreditsField().setText(String.valueOf(totalCredits));

	    populateOverviewPane();
	}
	
	//method that updates current credits
	private void updateCurrentCredits() {
        int totalCredits = view.getSelectModulesPane().getBlock3_4ModulesSelected().getItems().stream()
                .mapToInt(e -> e.getModuleCredits())
                .sum() + view.getSelectModulesPane().getBlock1Modules().getItems().stream()
                .mapToInt(e -> e.getModuleCredits())
                .sum() + view.getSelectModulesPane().getBlock2Modules().getItems().stream()
                .mapToInt(e -> e.getModuleCredits())
                .sum();
        view.getSelectModulesPane().getCurrentCreditsField().setText(String.valueOf(totalCredits));
    }
	
	//helper method - generates modules and course data and returns courses within an array
	private Course[] generateAndGetCourses() {
		Module ctec3701 = new Module("CTEC3701", "Software Development: Methods & Standards", 30, true, Block.BLOCK_1);

		Module ctec3702 = new Module("CTEC3702", "Big Data and Machine Learning", 30, true, Block.BLOCK_2);
		Module ctec3703 = new Module("CTEC3703", "Mobile App Development and Big Data", 30, true, Block.BLOCK_2);

		Module ctec3451 = new Module("CTEC3451", "Development Project", 30, true, Block.BLOCK_3_4);

		Module ctec3704 = new Module("CTEC3704", "Functional Programming", 30, false, Block.BLOCK_3_4);
		Module ctec3705 = new Module("CTEC3705", "Advanced Web Development", 30, false, Block.BLOCK_3_4);

		Module imat3711 = new Module("IMAT3711", "Privacy and Data Protection", 30, false, Block.BLOCK_3_4);
		Module imat3722 = new Module("IMAT3722", "Fuzzy Logic and Inference Systems", 30, false, Block.BLOCK_3_4);

		Module ctec3706 = new Module("CTEC3706", "Embedded Systems and IoT", 30, false, Block.BLOCK_3_4);


		Course compSci = new Course("Computer Science");
		compSci.addModule(ctec3701);
		compSci.addModule(ctec3702);
		compSci.addModule(ctec3451);
		compSci.addModule(ctec3704);
		compSci.addModule(ctec3705);
		compSci.addModule(imat3711);
		compSci.addModule(imat3722);

		Course softEng = new Course("Software Engineering");
		softEng.addModule(ctec3701);
		softEng.addModule(ctec3703);
		softEng.addModule(ctec3451);
		softEng.addModule(ctec3704);
		softEng.addModule(ctec3705);
		softEng.addModule(ctec3706);

		Course[] courses = new Course[2];
		courses[0] = compSci;
		courses[1] = softEng;

		return courses;
	}

}
