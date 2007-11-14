/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.popup.UIEditTagForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UITagContainer.gtmpl",
    events = {
        @EventConfig(listeners = UITagContainer.ChangeTagActionListener.class),
        @EventConfig(listeners = UITagContainer.EditTagActionListener.class),
        @EventConfig(listeners = UITagContainer.RemoveTagActionListener.class,confirm="UITagContainer.msg.confirm-remove-tag"),
        @EventConfig(listeners = UITagContainer.EmptyTagActionListener.class)
    }
)

public class UITagContainer extends UIComponent {
  public UITagContainer() throws Exception {}
  
  public List<Tag> getTags() throws Exception {
    List<Tag> tagList = new ArrayList<Tag>();
    MailService mailService = (MailService)PortalContainer.getComponent(MailService.class) ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
    if (accountId != null && accountId != "")  
      tagList = mailService.getTags(username, accountId);
    return tagList;
  }
  
  static public class ChangeTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTags = event.getSource();
      UIMailPortlet uiPortlet = uiTags.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setMessagePageList(mailSrv.getMessagePagelistByTag(username, accountId, tagId));
      MessageFilter filter = new MessageFilter("Tag"); 
      filter.setTag(new String[] { tagId });
      uiMessageList.setMessageFilter(filter);
      uiMessageList.setSelectedTagId(tagId);
      uiMessageList.setSelectedFolderId(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class EditTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;      
      UITagContainer uiTag = event.getSource();
      UIPopupAction uiPopup = uiTag.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIEditTagForm uiRenameTagForm = uiPopup.activate(UIEditTagForm.class, 450) ;
      uiRenameTagForm.setTag(tagId);
    }
  }
  
  static public class RemoveTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      System.out.println("============>>>> Remove Tag Action Listener");
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTag = event.getSource();     
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      mailSrv.removeTag(username, accountId, tagId);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
    }
  }
  
  static public class EmptyTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      System.out.println("============>>>> Empty Tag Action Listener");
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTag = event.getSource();     
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      
      List<Message> listMessage = mailSrv.getMessageByTag(username, accountId, tagId);
      List<String> listTag = new ArrayList<String>();
      List<String> listMessageId = new ArrayList<String>();
      for (Message mess : listMessage) {
        listMessageId.add(mess.getId());
      }
      listTag.add(tagId);
      mailSrv.removeMessageTag(username, accountId, listMessageId, listTag);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
}