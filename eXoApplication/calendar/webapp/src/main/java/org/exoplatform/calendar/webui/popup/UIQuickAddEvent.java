/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIQuickAddEvent.gtmpl",
    events = {
      @EventConfig(listeners = UIQuickAddEvent.SaveActionListener.class),
      @EventConfig(listeners = UIQuickAddEvent.MoreDetailActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIQuickAddEvent.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIQuickAddEvent extends UIForm implements UIPopupComponent{

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;
  final public static String FIELD_ALLDAY = "allDay".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final public static String UIQUICKADDTASK = "UIQuickAddTask".intern() ;


  private String calType_ = "0" ;
  private boolean isEvent_ = true ;
  public UIQuickAddEvent() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date(), false).addValidator(EmptyFieldValidator.class));
    addUIFormInput(new UIFormSelectBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormSelectBox(FIELD_TO_TIME, FIELD_TO_TIME, options));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, UIEventForm.getCategory())) ;
  }


  public void init(CalendarSetting  calendarSetting, String startTime, String endTime) throws Exception {
    List<SelectItemOption<String>> fromOptions = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat()) ;
    List<SelectItemOption<String>> toOptions = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat()) ;
    getUIFormSelectBox(FIELD_FROM_TIME).setOptions(fromOptions) ;
    getUIFormSelectBox(FIELD_TO_TIME).setOptions(toOptions) ;
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    if(startTime != null) cal.setTimeInMillis(Long.parseLong(startTime)) ;
    else {
      cal.set(java.util.Calendar.MINUTE, (cal.get(java.util.Calendar.MINUTE)/CalendarUtils.DEFAULT_TIMEITERVAL)*CalendarUtils.DEFAULT_TIMEITERVAL) ;
    }
    setEventFromDate(cal.getTime()) ;
    if(endTime != null )cal.setTimeInMillis(Long.parseLong(endTime)) ; 
    else {
      cal.add(java.util.Calendar.MINUTE, CalendarUtils.DEFAULT_TIMEITERVAL*2) ;
    }
    setEventToDate(cal.getTime()) ;
  }

  private void setEventFromDate(Date value) {
    UIFormDateTimeInput fromField = getChildById(FIELD_FROM) ;
    UIFormSelectBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    fromField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    timeFile.setValue(df.format(value)) ;
  }

  private Date getEventFromDate() throws Exception {
    UIFormDateTimeInput fromField = getChildById(FIELD_FROM) ;
    UIFormSelectBox timeFile = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    return df.parse(fromField.getValue() + " " + timeFile.getValue() ) ;
  }
  private Date getEventToDate() throws Exception {
    UIFormDateTimeInput fromField = getChildById(FIELD_TO) ;
    UIFormSelectBox timeFile = getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    return df.parse(fromField.getValue() + " " + timeFile.getValue() ) ;
  }
  private void setEventToDate(Date value) {
    UIFormDateTimeInput toField =  getChildById(FIELD_TO) ;
    UIFormSelectBox timeField =  getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    toField.setValue(df.format(value)) ;
    df = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    timeField.setValue(df.format(value)) ;
  }

  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Calendar> calendars = calendarService.getUserCalendars(username) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }
  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
    }
    return label ;
  }
  private String getEventSummary() {
    return getUIStringInput(FIELD_EVENT).getValue() ;
  }
  private String getEventDescription() {return getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;}


  private boolean getIsAllDay() {
    return getUIFormCheckBoxInput(FIELD_ALLDAY).isChecked() ;
  }
  private String getEventCalendar() {return getUIFormSelectBox(FIELD_CALENDAR).getValue() ;}
  public void setSelectedCalendar(String value) {getUIFormSelectBox(FIELD_CALENDAR).setValue(value) ;}
  private String getEventCategory() {return getUIFormSelectBox(FIELD_CATEGORY).getValue() ;}
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public void setEvent(boolean isEvent) { isEvent_ = isEvent ; }
  public boolean isEvent() { return isEvent_ ; }
  
  public void update(String calType, List<SelectItemOption<String>> options) throws Exception{
    if(options != null) {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(options) ;
    }else {
      getUIFormSelectBox(FIELD_CALENDAR).setOptions(getCalendar()) ;
    }
    calType_ = calType ;
  }
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.isEmpty(uiForm.getEventCalendar())) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.calendar-field-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(CalendarUtils.isEmpty(uiForm.getEventCategory())) {
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.category-field-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      Date fromDate = uiForm.getEventFromDate() ;
      Date toDate = uiForm.getEventToDate() ;
      if(uiForm.getIsAllDay()) {
        java.util.Calendar cal = GregorianCalendar.getInstance() ;
        cal.setTime(fromDate) ;
        cal.set(java.util.Calendar.HOUR, 0) ;
        cal.set(java.util.Calendar.MINUTE, 0) ;
        fromDate = cal.getTime() ;
        cal.setTime(toDate) ;
        cal.set(java.util.Calendar.HOUR, 0) ;
        cal.set(java.util.Calendar.MINUTE, 0) ;
        if(fromDate.equals(cal.getTime())) {
          cal.add(java.util.Calendar.DATE,1) ;
        } else if(fromDate.after(cal.getTime())) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } else {
          cal.setTime(toDate) ;
          cal.set(java.util.Calendar.HOUR, 0) ;
          cal.set(java.util.Calendar.MINUTE, 0) ;
          cal.add(java.util.Calendar.DATE,1) ;
        }
        toDate = cal.getTime() ;
      }else {      
        if(fromDate.equals(toDate) || fromDate.after(toDate)) {
          uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }  
      }
      try {
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(uiForm.getEventSummary()) ;
        calEvent.setDescription(uiForm.getEventDescription()) ;
        calEvent.setCalendarId(uiForm.getEventCalendar());
        if(uiForm.isEvent_){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        }
        calEvent.setEventCategoryId(uiForm.getEventCategory());
        calEvent.setFromDateTime(fromDate);
        calEvent.setToDateTime(toDate) ;
        calEvent.setCalType(uiForm.calType_) ;
        String username = CalendarUtils.getCurrentUser() ;
        if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
          CalendarUtils.getCalendarService().saveUserEvent(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
          CalendarUtils.getCalendarService().saveEventToSharedCalendar(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
          CalendarUtils.getCalendarService().saveGroupEvent(calEvent.getCalendarId(), calEvent, true) ;          
        }
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        UICalendarViewContainer uiContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.updateMiniCal() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        uiContainer.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
       /* uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-successfully", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;*/
      } catch (Exception e) {
        e.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-unsuccessfully", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  static  public class MoreDetailActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      CalendarSetting calendarSetting = 
        uiForm.getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
      if(uiForm.isEvent()) {
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        uiPouContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
        UIEventForm uiEventForm = uiPouContainer.addChild(UIEventForm.class, null, null) ;
        uiEventForm.update(uiForm.calType_, uiForm.getUIFormSelectBox(FIELD_CALENDAR).getOptions()) ;
        uiEventForm.initForm(calendarSetting, null) ;
        uiEventForm.setEventSumary(uiForm.getEventSummary()) ;
        uiEventForm.setEventDescription(uiForm.getEventDescription()) ;
        uiEventForm.setEventFromDate(uiForm.getEventFromDate()) ;
        uiEventForm.setEventToDate(uiForm.getEventToDate()) ;
        uiEventForm.setEventAllDate(uiForm.getIsAllDay()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        UIPopupContainer uiPouContainer  = uiPopupAction.activate(UIPopupContainer.class, 700) ;
        uiPouContainer.setId(UIPopupContainer.UITASKPOPUP) ;
        UITaskForm uiTaskForm = uiPouContainer.addChild(UITaskForm.class, null, null) ;
        uiTaskForm.update(uiForm.calType_, uiForm.getUIFormSelectBox(FIELD_CALENDAR).getOptions()) ;
        uiTaskForm.initForm(calendarSetting, null) ;
        uiTaskForm.setEventSumary(uiForm.getEventSummary()) ;
        uiTaskForm.setEventDescription(uiForm.getEventDescription()) ;
        uiTaskForm.setEventFromDate(uiForm.getEventFromDate()) ;
        uiTaskForm.setEventToDate(uiForm.getEventToDate()) ;
        uiTaskForm.setEventAllDate(uiForm.getIsAllDay()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIQuickAddEvent> {
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      event.getSource().getAncestorOfType(UICalendarPortlet.class).cancelAction() ;
    }
  }


}
