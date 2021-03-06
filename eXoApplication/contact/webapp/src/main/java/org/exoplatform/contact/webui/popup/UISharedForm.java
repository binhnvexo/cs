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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs ( {
  @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "system:/groovy/webui/form/UIForm.gtmpl",
      events = {
        @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
        @EventConfig(listeners = UISharedForm.SelectPermissionActionListener.class, phase = Phase.DECODE),  
        @EventConfig(listeners = UISharedForm.SelectGroupActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISharedForm.CancelActionListener.class, phase = Phase.DECODE)
      }
  ),
  @ComponentConfig(
                   id = "UIPopupWindowUserSelect",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
                     @EventConfig(listeners = UISharedForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UISharedForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
                   }
  )
})
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final static public String FIELD_ADDRESS = "addressName".intern() ;
  final static public String FIELD_CONTACT = "contactName".intern() ;
  
  //private Map<String, String> permissionGroup_ = new LinkedHashMap<String, String>() ;
  private AddressBook group_ = null ;
  private Contact contact_ = null ;
  private boolean isSharedGroup ;
  private boolean isNew_ = true ;
  public UISharedForm() { }
  
  public void setContact(Contact contact) { 
    isSharedGroup = false ;
    contact_ = contact ;
  }
  public void setGroup(AddressBook group) {
    isSharedGroup = true ;
    group_ = group ; 
  }
  
  public void setNew(boolean isNew) { isNew_ = isNew ; }
  public boolean isNew() { return isNew_ ; }
  
  public void init() throws Exception {
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    if (isSharedGroup) {
      UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_ADDRESS, FIELD_ADDRESS, null) ;
      formInputInfo.setValue(group_.getName()) ;
      inputset.addChild(formInputInfo) ; 
    } else {
      UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_CONTACT, FIELD_CONTACT, null) ;
      formInputInfo.setValue(ContactUtils.encodeHTML(contact_.getFullName())) ;
      inputset.addChild(formInputInfo) ;
    }
    inputset = ContactUtils.initSelectPermissions(inputset);
    addChild(inputset) ;
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;    
    ContactUtils.updateSelect(fieldInput, selectField, value);
  } 

  public String[] getActions() { return new String[] {"Save","Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  
  
  static  public class SaveActionListener extends EventListener<UISharedForm> {
    @SuppressWarnings("unchecked")
  public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String names = uiForm.getUIStringInput(ContactUtils.FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(ContactUtils.FIELD_GROUP).getValue() ;
      Map<String, String> receiveUsers = new LinkedHashMap<String, String>() ;
      Map<String, String> receiveGroups = new LinkedHashMap<String, String>() ;      
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.empty-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String username = ContactUtils.getCurrentUser() ;
      OrganizationService organizationService = 
        (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      if(!ContactUtils.isEmpty(names)) {
        StringBuilder invalidUsers = new StringBuilder() ;
        String[] array = names.split(",") ;
        for(String name : array) {
          if (organizationService.getUserHandler().findUserByName(name.trim()) != null) {
            if (!name.trim().equals(username)) {
              receiveUsers.put(name.trim() + DataStorage.HYPHEN, name.trim() + DataStorage.HYPHEN) ;
            }
          } else {
            if (invalidUsers.length() == 0) invalidUsers.append(name) ;
            else invalidUsers.append(", " + name) ;
          }
        }
        if (invalidUsers.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.not-exist-username"
              , new Object[]{invalidUsers.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      if(!ContactUtils.isEmpty(groups)) {
        StringBuilder invalidGroups = new StringBuilder() ;
        String[] array = groups.split(",") ;
        for(String name : array) {
          if(name != null && name.trim().length() != 0){
            if (organizationService.getGroupHandler().findGroupById(name.trim()) == null) {
              if (invalidGroups.length() == 0) invalidGroups.append(name) ;
              else invalidGroups.append(", " + name) ;
            }
          }
        }
        if (invalidGroups.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.not-exist-group"
              , new Object[]{invalidGroups.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      ContactService contactService = ContactUtils.getContactService() ;
      //SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;      
      Map<String, String>  receiveUsersByGroups = new LinkedHashMap<String, String>() ;
      if (!ContactUtils.isEmpty(groups)) {
        String[] arrayGroups = groups.split(",") ; 
        for (String group : arrayGroups) {
          if(group != null && group.trim().length() != 0){
            group = group.trim() ;
            receiveGroups.put(group, group) ;
              List<User> users = organizationService.getUserHandler().findUsersByGroup(group.trim()).getAll() ;
            for (User user : users) {
              receiveUsersByGroups.put(user.getUserName(), user.getUserName()) ;
            }
          }
        }
      }      
      receiveUsersByGroups.remove(ContactUtils.getCurrentUser()) ;

      if (receiveGroups.size() == 0 && receiveUsers.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.shared-yourself", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }

      if (uiForm.isSharedGroup) {
        AddressBook contactGroup = uiForm.group_ ;
        if(uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
          contactGroup = ContactUtils.setEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);
        } else {
          ContactUtils.removeEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);
        }
        if (uiForm.isNew_) {
          contactGroup = ContactUtils.setViewPermissionAddress(contactGroup, receiveUsers, receiveGroups);
          if (receiveUsers.size() > 0 ) {
            contactService.shareAddressBook(
                username, contactGroup.getId(), Arrays.asList(receiveUsers.keySet().toArray(new String[] {}))) ;
          }
          contactService.shareAddressBook(
              username, contactGroup.getId(), Arrays.asList(receiveUsersByGroups.keySet().toArray(new String[] {}))) ;         
        } else {
          if (!uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
            contactGroup = ContactUtils.removeEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);             
          }
          uiForm.getUIStringInput(ContactUtils.FIELD_USER).setEditable(true) ;
        }
        contactService.saveAddressBook(username, contactGroup, false) ;
        UIAddEditPermission uiAddEdit = uiForm.getParent() ;          
        uiAddEdit.updateGroupGrid(contactGroup);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;          
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      } else { // shared contact 
        if (uiForm.isNew_) {
          Map<String, String> viewMapUsers = new LinkedHashMap<String, String>() ; 
          for (String user : receiveUsers.keySet()) viewMapUsers.put(user, user) ;
          Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ;
          if(uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
            for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
          }
          Map<String, String> viewMapGroups = new LinkedHashMap<String, String>() ; 
          for (String user : receiveGroups.keySet()) viewMapGroups.put(user, user) ; 
          Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ;
          if(uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
            for (String user : receiveGroups.keySet()) editMapGroups.put(user, user) ;
          }
          // add to fix bug cs-1326
          Contact contact = contactService.getContact(username, uiForm.contact_.getId()) ;
          //Contact contact = uiForm.contact_ ;
          addPerUsers(contact, viewMapUsers, editMapUsers) ;
          addPerGroups(contact, viewMapGroups, editMapGroups) ;
          
          if(!uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) { // cs-1570
            if (contact.getEditPermissionUsers() != null) {
              List<String> oldPers = new ArrayList<String>() ;
              oldPers.addAll(Arrays.asList(contact.getEditPermissionUsers())) ;
              for (String user : receiveUsers.keySet()) {
                oldPers.remove(user) ;              
              }
              contact.setEditPermissionUsers(oldPers.toArray(new String[] {})) ;            
            }
            if (contact.getEditPermissionGroups() != null) {
              List<String> oldPers = new ArrayList<String>() ;
              oldPers.addAll(Arrays.asList(contact.getEditPermissionGroups())) ;
              for (String group : receiveGroups.keySet()) {
                oldPers.remove(group) ;              
              }
              contact.setEditPermissionGroups(oldPers.toArray(new String[] {})) ;            
            }
          }
          
          
          // add to fix bug cs-1300
          UIContacts uiContacts = uiForm.getAncestorOfType(
              UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
          if (uiContacts.getContactMap().get(contact.getId()) != null) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
          }
          try {
            contactService.saveContact(username, contact, false) ;
          }  catch (PathNotFoundException e) {
            uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.contact-not-existed", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;              
          } 
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          uiAddEdit.updateContactGrid(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ; 
          
          if (receiveUsers.size() > 0)
            contactService.shareContact(username, new String[] {contact.getId()}, Arrays.asList(receiveUsers.keySet().toArray(new String[] {}))) ;
          if (receiveUsersByGroups.size() > 0)
            contactService.shareContact(username, new String[] {contact.getId()}, Arrays.asList(receiveUsersByGroups.keySet().toArray(new String[] {}))) ; 
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ; 
        } else {
          Contact contact = uiForm.contact_ ;
          if (uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
            String[] editPerUsers = contact.getEditPermissionUsers() ;
            Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ;
            if (editPerUsers != null)
              for (String edit : editPerUsers) editMapUsers.put(edit, edit) ; 
            for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
            contact.setEditPermissionUsers(editMapUsers.keySet().toArray(new String[] {})) ;     
            
            String[] editPerGroups = contact.getEditPermissionGroups() ;
            Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ;
            if (editPerUsers != null)
              for (String edit : editPerGroups) editMapGroups.put(edit, edit) ; 
            for (String user : receiveGroups.keySet()) editMapGroups.put(user, user) ;
            contact.setEditPermissionGroups(editMapGroups.keySet().toArray(new String[] {})) ;
          } else {
            List<String> newEditPerUsers = new ArrayList<String>() ;
            for (String edit : contact.getEditPermissionUsers()) 
              if (!receiveUsers.keySet().contains(edit)) newEditPerUsers.add(edit) ; 
            contact.setEditPermissionUsers(newEditPerUsers.toArray(new String[] {})) ;
            
            List<String> newEditPerGroups = new ArrayList<String>() ;
            if (contact.getEditPermissionGroups() != null)
              for (String edit : contact.getEditPermissionGroups()) 
                if (!receiveGroups.keySet().contains(edit)) newEditPerGroups.add(edit) ; 
            contact.setEditPermissionGroups(newEditPerGroups.toArray(new String[] {})) ;
          }
          contactService.saveContact(username, contact, false) ;
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          uiAddEdit.updateContactGrid(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;
          uiForm.getUIStringInput(ContactUtils.FIELD_USER).setEditable(true) ;
          
          UIContacts uiContacts = uiForm.getAncestorOfType(
              UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
          
//        add to fix bug cs-1300
          if (uiContacts.getContactMap().get(contact.getId()) != null) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
          }
          /*
          if (uiContacts.isDisplaySearchResult()) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
          }*/
        } 
      }
      uiForm.getUIStringInput(ContactUtils.FIELD_USER).setValue(null) ;
      uiForm.getUIStringInput(ContactUtils.FIELD_GROUP).setValue(null) ;
      uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).setChecked(false) ;
      //uiForm.permissionGroup_.clear() ;        
  
      uiForm.isNew_ = true ;
    }
    private static Contact addPerUsers(Contact contact, Map<String, String> viewMap, Map<String, String> editMap) {
      String[] viewPer = contact.getViewPermissionUsers() ;
      if (viewPer != null)
        for (String view : viewPer) viewMap.put(view, view) ;
      contact.setViewPermissionUsers(viewMap.keySet().toArray(new String[] {})) ;
      
      String[] editPer = contact.getEditPermissionUsers() ;
      if (editPer != null)
        for (String edit : editPer) editMap.put(edit, edit) ; 
      contact.setEditPermissionUsers(editMap.keySet().toArray(new String[] {})) ;
      return contact ;
    }
    private static Contact addPerGroups(Contact contact, Map<String, String> viewMap, Map<String, String> editMap) {
      String[] viewPer = contact.getViewPermissionGroups() ;
      if (viewPer != null)
        for (String view : viewPer) viewMap.put(view, view) ;
      contact.setViewPermissionGroups(viewMap.keySet().toArray(new String[] {})) ;
      
      String[] editPer = contact.getEditPermissionGroups() ;
      if (editPer != null)
        for (String edit : editPer) editMap.put(edit, edit) ; 
      contact.setEditPermissionGroups(editMap.keySet().toArray(new String[] {})) ;
      return contact ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UISharedForm uiForm = uiContainer.findFirstComponentOfType(UISharedForm.class);
      String values = uiUserSelector.getSelectedUsers();
      if (values == null) return ;
      for (String value : values.split(","))
        uiForm.updateSelect(ContactUtils.FIELD_USER, value.trim()) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUseSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUseSelector.getParent() ;
      UIPopupContainer uiContainer = uiPoupPopupWindow.getAncestorOfType(UIPopupContainer.class) ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }

  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiSharedForm = event.getSource() ;
      if (!uiSharedForm.isNew_) {
        UIApplication uiApp = uiSharedForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.cannot-change-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      UIForm uiForm = (UIForm)uiSharedForm ;
      ContactUtils.selectPermissions(uiForm, event);
    }
  }
  
  
  static  public class SelectGroupActionListener extends EventListener<UIGroupSelector> {   
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIGroupSelector uiForm = event.getSource() ;
      String user = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getAncestorOfType(UISharedForm.class).updateSelect(ContactUtils.FIELD_GROUP, user) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ; 
    }
  }
  
  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }

}
