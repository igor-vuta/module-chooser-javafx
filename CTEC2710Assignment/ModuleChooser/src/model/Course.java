package model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Course implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String courseName;
	private Map<String, Module> modulesOnCourse;
	
	public Course(String courseName) {
		this.courseName = courseName;
		modulesOnCourse = new HashMap<String, Module>();
	}
	
	public String getCourseName() {
		return courseName;
	}
	
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	
	public void addModule(Module m) {
		modulesOnCourse.put(m.getModuleCode(), m);
	}
	
	public Module getModuleByCode(String code) {
		return modulesOnCourse.get(code);
	}
	
	public Collection<Module> getAllModulesOnCourse() {
		return modulesOnCourse.values();
	}
	
	@Override
	public String toString() { 
		return courseName;
	}
	
	public String actualToString() {
		return "Course:[courseName=" + courseName + ", modulesOnCourse=" + modulesOnCourse + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Course other)
				&& this.courseName.equals(other.courseName) 
				&& this.modulesOnCourse.equals(other.modulesOnCourse);
	}
	
}
