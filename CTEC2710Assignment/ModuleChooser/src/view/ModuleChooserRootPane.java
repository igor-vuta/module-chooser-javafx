package view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;


public class ModuleChooserRootPane extends BorderPane {

	private CreateStudentProfilePane cspp;
	private ModuleChooserMenuBar mstmb;
	private TabPane tp;
	private SelectModulesPane smp;
	private ReserveModulesPane rmp;
	private OverviewSelectionPane osp;
	
	public ModuleChooserRootPane() {

		tp = new TabPane();
		tp.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		cspp = new CreateStudentProfilePane();
		smp = new SelectModulesPane();
		rmp =  new ReserveModulesPane();
		osp = new OverviewSelectionPane();
		
		Tab t1 = new Tab("Create Profile", cspp);
		Tab t2 = new Tab("Select Modules", smp);
		Tab t3 = new Tab("Reserve Modules", rmp);
		Tab t4 = new Tab("Overview Selection", osp);

		tp.getTabs().addAll(t1,t2,t3,t4);
		
		mstmb = new ModuleChooserMenuBar();
		
		this.setTop(mstmb);
		this.setCenter(tp);
		
	}

	public CreateStudentProfilePane getCreateStudentProfilePane() {
		return cspp;
	}
	
	public ModuleChooserMenuBar getModuleSelectionToolMenuBar() {
		return mstmb;
	}
	
	public SelectModulesPane getSelectModulesPane() {
		return smp;
	}
	
	public ReserveModulesPane getReserveModulesPane() {
		return rmp;
	}
	public OverviewSelectionPane getOverviewSelectionPane() {
		return osp;
	}

	public void changeTab(int index) {
		tp.getSelectionModel().select(index);
	}
}
