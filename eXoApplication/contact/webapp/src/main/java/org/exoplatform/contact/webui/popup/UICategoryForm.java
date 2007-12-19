/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.webui.popup;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICategoryForm.SaveActionListener.class),      
      @EventConfig(listeners = UICategoryForm.CancelActionListener.class)
    }
)
public class UICategoryForm extends UIForm implements UIPopupComponent {
  private boolean isNew_ = true ;
  private String groupId_ ;
  public static final String FIELD_CATEGORYNAME_INPUT = "categoryName";
  public static final String FIELD_DESCRIPTION_INPUT = "description";
  
  public UICategoryForm() {
    addUIFormInput(new UIFormStringInput(FIELD_CATEGORYNAME_INPUT, FIELD_CATEGORYNAME_INPUT, null));    
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;    
  }
  
  public String getGroupId() { return groupId_ ; }
  public void setGroupId(String group) { groupId_ = group ; }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public boolean isNew() { return isNew_ ; } 
  public void setNew(boolean isNew) { isNew_ = isNew ;}
  
  public void setValues(String groupId) throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    ContactGroup contactGroup = contactService.getGroup(SessionsUtils.getSessionProvider(), username, groupId) ;
    if (contactGroup != null) {
      groupId_ = groupId ;
      getUIStringInput(FIELD_CATEGORYNAME_INPUT).setValue(contactGroup.getName()) ;
      getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setValue(contactGroup.getDescription()) ;
    }
  }
  
  static  public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiCategoryForm = event.getSource() ;
      String  groupName = uiCategoryForm.getUIStringInput(FIELD_CATEGORYNAME_INPUT).getValue(); 
      UIApplication uiApp = uiCategoryForm.getAncestorOfType(UIApplication.class) ;
      if (ContactUtils.isEmpty(groupName)) {
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryName-required", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      ContactGroup group = new ContactGroup() ;
      if (!uiCategoryForm.isNew_) group.setId(uiCategoryForm.groupId_) ;
      group.setName(groupName) ;
      group.setDescription(uiCategoryForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue()) ;
      ContactUtils.getContactService().saveGroup(
          SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), group, uiCategoryForm.isNew_) ; 
      UIContactPortlet uiContactPortlet = uiCategoryForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupContainer popupContainer = uiCategoryForm.getAncestorOfType(UIPopupContainer.class) ;
      if (popupContainer != null) {
        UICategorySelect uiCategorySelect = popupContainer.findFirstComponentOfType(UICategorySelect.class);
        if (uiCategorySelect != null) {
          List<SelectItemOption<String>> ls = uiCategorySelect.getCategoryList();
          ls.add(new SelectItemOption<String>(groupName, group.getId())) ;
          uiCategorySelect.setCategoryList(ls);
          
          //event.getRequestContext().addUIComponentToUpdateByAjax(uiCategorySelect) ;
          
        } else {
          UIImportForm importForm = popupContainer.findFirstComponentOfType(UIImportForm.class) ;
          List<SelectItemOption<String>> ls = importForm.getCategoryList();
          ls.add(new SelectItemOption<String>(groupName, group.getId())) ;
          importForm.setCategoryList(ls);
          event.getRequestContext().addUIComponentToUpdateByAjax(importForm) ;
        }        

        UIPopupAction action = uiCategoryForm.getAncestorOfType(UIPopupAction.class) ;
        if (action != null) {
          System.out.println("\n\n khac null \n\n");
          action.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(action) ;
        } 
      } else {
        UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook) ;
        UIContacts contacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;        
        event.getRequestContext().addUIComponentToUpdateByAjax(contacts) ;
        uiContactPortlet.cancelAction() ;
      }
      
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook) ;
    
    }
  }
  
  static  public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiCategoryForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiCategoryForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;      
    }
  } 
  
}
