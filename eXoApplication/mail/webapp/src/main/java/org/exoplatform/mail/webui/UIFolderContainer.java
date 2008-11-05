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
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIFolderForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIRenameFolderForm;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    template = "app:/templates/mail/webui/UIFolderContainer.gtmpl",
    events = {
        @EventConfig(listeners = UIFolderContainer.ChangeFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.AddFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.AddSubFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RenameFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RemoveFolderActionListener.class, confirm="UIFolderContainer.msg.confirm-remove-folder"),
        @EventConfig(listeners = UIFolderContainer.MarkReadActionListener.class),
        @EventConfig(listeners = UIFolderContainer.EmptyFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.MoveToTrashActionListener.class)
    }
)
public class UIFolderContainer extends UIContainer {
  private String currentFolder_ = null ;
  public int i = 1;
  
  public UIFolderContainer() throws Exception { }

  public void init(String accountId) throws Exception {
    currentFolder_ = Utils.createFolderId(accountId, Utils.FD_INBOX, false);
  }
  
  public String getSelectedFolder(){ return currentFolder_ ; }
  public void setSelectedFolder(String folderId) { currentFolder_ = folderId ;}
  
  public List<Folder> getDefaultFolders() throws Exception{
    return getFolders(false);
  } 
  
  public List<Folder> getCustomizeFolders() throws Exception{
    return getFolders(true);
  }
  
  public List<Folder> getSubFolders(String parentPath) throws Exception {
    MailService mailSvr = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser() ;
    String accountId = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    List<Folder> subFolders = new ArrayList<Folder>();
    for (Folder f : mailSvr.getSubFolders(SessionProviderFactory.createSystemProvider(), username, accountId, parentPath)) {
      subFolders.add(f);
    }
    return subFolders ;
  }
  
  public Folder getCurrentFolder() throws Exception{
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    return mailSvr.getFolder(SessionProviderFactory.createSystemProvider(), username, accountId, getSelectedFolder()) ;
  }
  
  public List<Folder> getFolders(boolean isPersonal) throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    try {
      folders.addAll(mailSvr.getFolders(SessionProviderFactory.createSystemProvider(), username, accountId, isPersonal)) ;
    } catch (Exception e){
      //e.printStackTrace() ;
    }
    return folders ;
  }
  
  public String[] getActions() {
    return new String[] {"AddFolder"} ;
  }
  
  static public class AddFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      UIFolderContainer uiFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolder.getAncestorOfType(UIMailPortlet.class);
      UIApplication uiApp = uiFolder.getAncestorOfType(UIApplication.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIFolderContainer.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      uiPopup.activate(UIFolderForm.class, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static public class AddSubFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIFolderForm uiFolderForm = uiPopup.createUIComponent(UIFolderForm.class, null, null);
      uiFolderForm.setParentPath(folderId);
      uiPopup.activate(uiFolderForm, 450, 0, false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static public class ChangeFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIFolderContainer uiFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolder.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class) ;
      UIMessageList uiMessageList = uiMsgArea.getChild(UIMessageList.class) ;
      UIMessagePreview uiMsgPreview = uiMsgArea.getChild(UIMessagePreview.class) ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      boolean isRefesh = true ;
      if (uiFolder.getSelectedFolder() != null  && uiFolder.getSelectedFolder().equals(folderId)) {
        try {
          uiMessageList.updateList();
          isRefesh = false ;
        } catch(Exception e) { } 
      } 
      if (isRefesh) {
        uiFolder.setSelectedFolder(folderId) ;
        MessageFilter filter = new MessageFilter("Folder"); 
        filter.setAccountId(accountId);
        filter.setFolder(new String[] {folderId});
        uiMessageList.setMessageFilter(filter);
        uiMessageList.setSelectedFolderId(folderId);
        uiMessageList.setSelectedTagId(null);
        uiMessageList.init(accountId);
        uiMessageList.viewing_ =  uiMessageList.VIEW_ALL;
        uiMsgPreview.setMessage(null);
      }
      
      UISearchForm uiSearchForm = uiPortlet.findFirstComponentOfType(UISearchForm.class);
      System.out.println("==========aaa============>>>>>> " + uiSearchForm.getTextSearch());
      if (!MailUtils.isFieldEmpty(uiSearchForm.getTextSearch())) {
        uiSearchForm.setTextSearch("");       
        event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder.getParent()) ;
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea) ;
    }
  }
  
  static public class RenameFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;      
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIRenameFolderForm uiRenameFolderForm = uiPopup.activate(UIRenameFolderForm.class, 450) ;
      uiRenameFolderForm.setFolderId(folderId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }

  static public class RemoveFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;          
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      UINavigationContainer uiNavigationContainer = uiFolderContainer.getAncestorOfType(UINavigationContainer.class);
      String accountId = uiNavigationContainer.getChild(UISelectAccount.class).getSelectedValue();
      
      mailService.removeUserFolder(SessionProviderFactory.createSystemProvider(), username, accountId, folderId);     
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      if (folderId.equals(uiFolderContainer.getSelectedFolder())) {
        uiMessageList.setMessageFilter(null);
        uiMessageList.init(accountId);
        uiFolder.setSelectedFolder(Utils.createFolderId(accountId, Utils.FD_INBOX, false));
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null) ;
      } else if (uiFolderContainer.getSelectedFolder() == null && uiMessageList.getMessageFilter().getName().equals("Search")) {
        uiMessageList.updateList() ;
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent()) ;
    }
  }
  
  static public class EmptyFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      UIFolderContainer uiFolderContainer = event.getSource() ;
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIMessageList uiMsgList = uiMsgArea.getChild(UIMessageList.class) ;
      UIMessagePreview uiMsgPreview = uiMsgArea.getChild(UIMessagePreview.class);
      UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      
      List<Message> msgList = mailSrv.getMessagesByFolder(SessionProviderFactory.createSystemProvider(), username, accountId, folderId) ;
      mailSrv.removeMessages(SessionProviderFactory.createSystemProvider(), username, accountId, msgList, false);
      
      uiMsgList.updateList();
      Message msgPreview = uiMsgPreview.getMessage() ;
      if (msgPreview != null) {
      	if (!msgList.contains(msgPreview.getId()))
      		uiMsgPreview.setMessage(null);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea) ;
    }
  }
  
  static public class MarkReadActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      MessageFilter filter = new MessageFilter("");
      filter.setFolder(new String[] {folderId});
      filter.setAccountId(accountId);
      filter.setViewQuery("@" + Utils.EXO_ISUNREAD + "='true'");
      List<Message> messages = mailSrv.getMessages(SessionProviderFactory.createSystemProvider(), username, filter);
      mailSrv.toggleMessageProperty(SessionProviderFactory.createSystemProvider(), username, accountId, messages, Utils.EXO_ISUNREAD);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      List<Message> msgList = new  ArrayList<Message>(uiMessageList.messageList_.values());
      for (Message msg : msgList) {
        msg.setUnread(false);
        uiMessageList.messageList_.put(msg.getId(), msg);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent()) ;
    }
  }
  
  static public class MoveToTrashActionListener extends EventListener<UIFolderContainer> {
	public void execute(Event<UIFolderContainer> event) throws Exception {
	  UIFolderContainer uiFolderContainer = event.getSource() ;
	  String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;		  
    UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class) ;
    UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class) ;
	  UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class) ;	
    UIMessageList uiMsgList = uiMsgArea.getChild(UIMessageList.class) ;
	  UIMessagePreview uiMsgPreview = uiMsgArea.getChild(UIMessagePreview.class) ;
	  String username = uiPortlet.getCurrentUser() ;
	  String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
	  MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class) ;
	  List<Message> msgList = mailSrv.getMessagesByFolder(SessionProviderFactory.createSystemProvider(), username, accountId, folderId) ;
	  boolean containPreview = false ;
	  Message msgPre = uiMsgPreview.getMessage() ;
	  for (Message msg : msgList) {
	    if (msgPre != null && msg.getId().equals(msgPre.getId())) containPreview = true ;
	    String trashFolderId = Utils.createFolderId(accountId, Utils.FD_TRASH, false) ;
	    mailSrv.moveMessage(SessionProviderFactory.createSystemProvider(), username, accountId, msg, folderId, trashFolderId) ;
	  }	      		 
	  uiMsgList.updateList() ;
	  if (containPreview) uiMsgPreview.setMessage(null);
	  event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
	  event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea) ;
	  }
  }
}