/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Oct 25, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.ImportActionListener.class), 
      @EventConfig(listeners = UIImportForm.CancelActionListener.class)
    }
)
public class UIImportForm extends UIForm implements UIPopupComponent {
  private static final String CHOOSE_MIME_MESSAGE = "choose-mime-message".intern();
  private static final String IMPORT_TO_FOLDER = "import-to-folder".intern();
  
  public UIImportForm() throws Exception {
    addUIFormInput(new UIFormUploadInput(CHOOSE_MIME_MESSAGE, CHOOSE_MIME_MESSAGE));
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();   
    String accountId = MailUtils.getAccountId();
    String username = MailUtils.getCurrentUser() ;
    MailService mailSrv = MailUtils.getMailService(); 

    for (Folder folder : mailSrv.getFolders(username, accountId)) {   
      if (!folder.getName().equals(Utils.FD_SENT)) {
        SelectItemOption<String> option = new SelectItemOption<String>(folder.getName(), folder.getId());
        options.add(option);
      }
    }    
   
    addUIFormInput(new UIFormSelectBox(IMPORT_TO_FOLDER, IMPORT_TO_FOLDER, options));
  }
  
  public void setSelectedFolder(String value) throws Exception {
    getUIFormSelectBox(IMPORT_TO_FOLDER).setSelectedValues(new String[] {value});
  } 
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static public class ImportActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      System.out.println(" === >>> Import Listener");
      UIImportForm uiImportForm = event.getSource();
      UIApplication  uiApp = uiImportForm.getAncestorOfType(UIApplication.class);
      UIMailPortlet uiPortlet = uiImportForm.getAncestorOfType(UIMailPortlet.class);
      UIFormUploadInput uiUploadInput = (UIFormUploadInput) uiImportForm.getUIInput(CHOOSE_MIME_MESSAGE);
      UploadResource uploadResource = uiUploadInput.getUploadResource();
      if(uploadResource == null) {
        uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileName-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      InputStream inputStream = uiUploadInput.getUploadDataAsStream();
      String type = uploadResource.getMimeType();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      String username = uiPortlet.getCurrentUser() ;
      String folderId = uiImportForm.getUIFormSelectBox(IMPORT_TO_FOLDER).getValue();
      MailService mailSrv = MailUtils.getMailService();
      mailSrv.importMessage(username, accountId, folderId, inputStream, type);
      Folder folder = mailSrv.getFolder(username, accountId, folderId);
      folder.setNumberOfUnreadMessage(folder.getNumberOfUnreadMessage() + 1);
      mailSrv.saveFolder(username, accountId, folder);
      uiPortlet.cancelAction() ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}