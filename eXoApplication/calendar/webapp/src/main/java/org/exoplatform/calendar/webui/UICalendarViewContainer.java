/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UICalendarViewContainer.gtmpl"
)
public class UICalendarViewContainer extends UIContainer  {
  public UICalendarViewContainer() throws Exception {
    addChild(UIMonthView.class, null, null) ;
    addChild(UIDayView.class, null, null) ;
    addChild(UIWeekView.class, null, null) ;
    addChild(UIYearView.class, null, null) ;
    addChild(UIListView.class, null, null) ;
    addChild(UIScheduleView.class, null, null) ;

    setRenderedChild(UIMonthView.class) ;
  }  
}
