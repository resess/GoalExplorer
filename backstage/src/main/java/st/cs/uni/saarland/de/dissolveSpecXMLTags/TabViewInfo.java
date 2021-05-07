package st.cs.uni.saarland.de.dissolveSpecXMLTags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import st.cs.uni.saarland.de.entities.Listener;
import st.cs.uni.saarland.de.helpClasses.Info;

public class TabViewInfo extends Info {
	
//	   $r2 = virtualinvoke $r0.<com.example.Testapp.TabViewAct: android.app.ActionBar getActionBar()>();
	//   ( virtualinvoke $r2.<android.app.ActionBar: void setNavigationMode(int)>(2); )
		
//	    $r3 = virtualinvoke $r2.<android.app.ActionBar: android.app.ActionBar$Tab newTab()>();
//	    $r5 = specialinvoke $r0.<com.example.Testapp.TabViewAct: java.lang.String getTextOfTab()>();
//	    $r3 = virtualinvoke $r3.<android.app.ActionBar$Tab: android.app.ActionBar$Tab setText(java.lang.CharSequence)>($r5);
//	    $r4 = new com.example.Testapp.TabListener;
//	    specialinvoke $r4.<com.example.Testapp.TabListener: void <init>(android.app.Activity,java.lang.String,java.lang.Class)>($r0, "Tab22", class "com/example/Testapp/Fragment2TabView");
//	    $r3 = virtualinvoke $r3.<android.app.ActionBar$Tab: android.app.ActionBar$Tab setTabListener(android.app.ActionBar$TabListener)>($r4);
//	    virtualinvoke $r2.<android.app.ActionBar: void addTab(android.app.ActionBar$Tab)>($r3);
	
//	private String actionBarReg = "";
	private String listenerReg = "";
	private Set<Listener> listener = new HashSet<Listener>();
	private String fragmentClassName = "";
	private String fragmentClassReg = "";
//	private boolean setNavigationMode2;
	private List<String> activityClassName = new ArrayList<String>();
	
	// text is text of the TabTitle
	// searchedUiReg = TabReg
	public TabViewInfo(String registerTab) {
		super(registerTab);
//		setNavigationMode2 = false;
	}

//	@Override
//	public boolean shouldRunOnInitMethod() {
//		if((!fragmentClassReg.equals("")) || (!searchedEReg.equals("")) || (!textReg.equals("")))
//			return true;
//		else 
//			return false;
//	}

//	public String getActionBarReg() {
//		return actionBarReg;
//	}
//
//	public void setActionBarReg(String actionBarReg) {
//		this.actionBarReg = actionBarReg;
//	}

	public String getListenerReg() {
		return listenerReg;
	}

	public void setListenerReg(String listenerReg) {
		this.listenerReg = listenerReg;
	}

	public Set<Listener> getListener() {
		return listener;
	}

	public void addListener(Set<Listener> listener2) {
		this.listener.addAll(listener2);
	}

	public String getFragmentClassName() {
		return fragmentClassName;
	}

	public void setFragmentClassName(String fragmentClassName) {
		this.fragmentClassName = fragmentClassName.replace("/", ".");
	}

	public String getFragmentClassReg() {
		return fragmentClassReg;
	}

	public void setFragmentClassReg(String fragmentClassReg) {
		this.fragmentClassReg = fragmentClassReg;
	}

	public List<String> getActivityClassName() {
		return activityClassName;
	}

	public void addActivityClassName(List<String> activityClassName) {
		this.activityClassName.addAll(activityClassName);
	}
	
	public void addListener(Listener l){
		this.listener.add(l);
	}

	@Override
	public Info clone() {
		TabViewInfo newInfo = new TabViewInfo(searchedEReg);
		newInfo.addActivityClassName(activityClassName);
		newInfo.setFragmentClassName(fragmentClassName);
		newInfo.setFragmentClassReg(fragmentClassReg);
		newInfo.addListener(listener);
		newInfo.setListenerReg(listenerReg);
		newInfo.setText(text);
		newInfo.setTextReg(textReg);		
		return newInfo;
	}

	@Override
	public boolean allValuesFound() {
		return (!activityClassName.equals("") && !fragmentClassName.equals("") && listenerReg.equals("") && textReg.equals("") && searchedEReg.equals(""));
	}

//	public boolean isSetNavigationMode2() {
//		return setNavigationMode2;
//	}
//
//	public void setSetNavigationMode2() {
//		this.setNavigationMode2 = true;
//	}
	
	

}
