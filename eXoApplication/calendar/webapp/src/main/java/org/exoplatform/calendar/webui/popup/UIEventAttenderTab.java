/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
@ComponentConfig(template = "app:/templates/calendar/webui/UIPopup/UIEventAttenderTab.gtmpl")
public class UIEventAttenderTab extends UIFormInputWithActions {
  final public static String FIELD_FROM_TIME = "timeFrom".intern() ;
  final public static String FIELD_TO_TIME = "timeTo".intern();
  final public static String FIELD_CHECK_TIME = "checkTime".intern();

  final public static String FIELD_DATEALL = "dateAll".intern();
  final public static String FIELD_CURRENTATTENDER = "currentAttender".intern() ;

  protected Map<String, String> parMap_ = new HashMap<String, String>() ;
  private Calendar calendar_ ;
  public UIEventAttenderTab(String arg0) {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    calendar_ = CalendarUtils.getInstanceTempCalendar() ;

    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, getTimes())) ;
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME, getTimes())) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_DATEALL, FIELD_DATEALL, false)) ;
    UIFormCheckBoxInput<Boolean> checkFreeInput = new UIFormCheckBoxInput<Boolean>(FIELD_CHECK_TIME, FIELD_CHECK_TIME, false) ;
    checkFreeInput.setOnChange("OnChange") ;
    addUIFormInput(checkFreeInput) ;
  }

  private List<SelectItemOption<String>> getTimes() {
    return CalendarUtils.getTimesSelectBoxOptions(CalendarUtils.TIMEFORMAT) ;
  }

  protected UIFormComboBox getUIFormComboBox(String id) {
    return findComponentById(id) ;
  }
  
  protected void updateParticipants(String values) throws Exception{
  	Map<String, String> tmpMap = new HashMap<String, String>() ;
  	tmpMap.putAll(parMap_) ;
  	for(String id : parMap_.keySet()) {
  		removeChildById(id) ;
  	}
  	List<String> newPars = new ArrayList<String>() ;
  	parMap_.clear() ;
  	if(values != null && values.length() > 0) {
  		for(String par : values.split(",")) {
  			String vl = tmpMap.get(par) ;
  			parMap_.put(par, vl) ;
  			if(vl == null) newPars.add(par) ;  			
  		}
  	}
  	
  	for(String id : parMap_.keySet()) {
  		addUIFormInput(new UIFormCheckBoxInput<Boolean>(id, id, false)) ;
  	}
  	
  	System.out.println("newPars =====>" + newPars.size()) ;
  	if(newPars.size() > 0) {
  		EventQuery eventQuery = new EventQuery() ;
    	eventQuery.setFromDate(CalendarUtils.getBeginDay(calendar_)) ;
    	eventQuery.setToDate(CalendarUtils.getEndDay(calendar_)) ;
    	eventQuery.setParticipants(newPars.toArray(new String[]{})) ;
    	eventQuery.setNodeType("exo:calendarPublicEvent") ;
    	Map<String, String> parsMap = 
    		CalendarUtils.getCalendarService().checkFreeBusy(SessionsUtils.getSystemProvider(), eventQuery) ;
    	parMap_.putAll(parsMap) ;
  	}
  	
  }

  /*protected void updateData(Map<String, List<String>> data) throws Exception{
    parMap_.putAll(data) ;
  }*/
  private Map<String, String> getMap(){ return parMap_ ; }
  
  protected String[] getParticipants() { 
  	System.out.println("====>getParticipants") ;
  	return parMap_.keySet().toArray(new String[]{}) ; } 

  protected void moveNextDay() throws Exception{
    calendar_.add(Calendar.DATE, 1) ;
    StringBuilder values = new StringBuilder(); 
    for(String par : parMap_.keySet()) {
    	if(values != null && values.length() > 0) values.append(",") ;
    	values.append(par) ;    	
    }
    parMap_.clear() ;
    updateParticipants(values.toString()) ;
  }
  protected void movePreviousDay() throws Exception{
    calendar_.add(Calendar.DATE, -1) ;
    StringBuilder values = new StringBuilder(); 
    for(String par : parMap_.keySet()) {
    	if(values != null && values.length() > 0) values.append(",") ;
    	values.append(par) ;    	
    }
    parMap_.clear() ;
    updateParticipants(values.toString()) ;
  }
  /*protected List<String> getDisplayTimes(String timeFormat, int timeInterval) {
    List<String> times = new ArrayList<String>() ;
    Calendar cal = CalendarUtils.getBeginDay(GregorianCalendar.getInstance()) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    for(int i = 0; i < 24*(60/timeInterval); i++) {
      times.add(df.format(cal.getTime())) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval) ;
    }
    return times ;
  }
  */
  private UIForm getParentFrom() {
    return getAncestorOfType(UIForm.class) ;
  }
  private String getFormName() { 
    UIForm uiForm = getAncestorOfType(UIForm.class);
    return uiForm.getId() ; 
  }

  private String getFromFieldValue() {
    return getUIFormComboBox(FIELD_FROM_TIME).getValue() ;
  }

  private boolean isAllDateFieldChecked() {
    return getUIFormCheckBoxInput(FIELD_DATEALL).isChecked() ;
  }
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }


}
