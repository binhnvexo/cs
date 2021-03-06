/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.mail.Multipart;
import javax.mail.Part;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 19, 2010  
 */
public interface DataStorage {

  public static final String MIX_REFERENCEABLE = "mix:referenceable".intern();

  public static final String NT_FILE           = "nt:file".intern();

  public static final String NT_UNSTRUCTURED   = "nt:unstructured".intern();

  public static final String EXO_SHARED_MIXIN  = "exo:accountShared".intern();

  public static final String EXO_SHARED_ID     = "exo:sharedId".intern();

  public static final String JCR_UUID          = "jcr:uuid".intern();

  public String getMailHierarchyNode() throws Exception;

  public Node getMailHomeNode(SessionProvider sProvider, String username) throws Exception;

  public Account getAccountById(String username, String id) throws Exception;

  public List<Account> getAccounts(String username) throws Exception;

  public Account getAccount(SessionProvider sProvider, Node accountNode) throws Exception;

  public Message getMessageById(String username, String accountId, String msgId) throws Exception;

  public QueryImpl createXPathQuery(SessionProvider sProvider, String username, String accountId, String xpath) throws Exception;

  public MailSetting getMailSetting(String username) throws Exception;

  public MessagePageList getMessagePageList(String username, MessageFilter filter) throws Exception;

  public List<Message> getMessages(String username, MessageFilter filter) throws Exception;

  public Message getMessage(Node messageNode) throws Exception;

  public void removeAccount(String username, String accountId) throws Exception;

  public void removeMessage(String username, String accountId, Message message) throws Exception;

  public void removeMessages(String username, String accountId, List<Message> messages, boolean moveReference) throws Exception;

  public void moveMessages(String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId) throws Exception;

  public void moveMessage(String username, String accountId, Message msg, String currentFolderId, String destFolderId, boolean updateReference) throws Exception;

  public void moveMessages(String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId, boolean updateReference) throws Exception;

  public void saveAccount(String username, Account account, boolean isNew) throws Exception;

  public void saveMailSetting(String username, MailSetting newSetting) throws Exception;

  public void saveMessage(String username, String accountId, String targetMsgPath, Message message, boolean isNew) throws Exception;

  /**
   * [POP3]Saving all of message on Local. Or saving the message that sent successfully***/
  public Node saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception;

  public boolean saveMessage(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, boolean saveTotal, String currentUserName) throws Exception;

  public boolean saveMessage(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception;

  /**
   * [IMAP] Saving message header on Local***/
  public boolean saveMessage(String username, String accId, long[] msgUID, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception;

  public boolean saveMessage(String username, String accId, long msgUID, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception;

  public boolean saveTotalMessage(String username, String accId, String msgId, javax.mail.Message msg, SessionProvider sProvider) throws Exception;

  public String getAddresses(javax.mail.Message msg, javax.mail.Message.RecipientType type) throws Exception;

  public void increaseFolderItem(SessionProvider sProvider, String username, String accId, String folderId, boolean isReadMessage) throws Exception;

  public StringBuffer setMultiPart(Multipart multipart, Node node, StringBuffer body);

  public StringBuffer setPart(Part part, Node node, StringBuffer body);

  public StringBuffer getNestedMessageBody(Part part, Node node, StringBuffer body) throws Exception;

  public StringBuffer appendMessageBody(Part part, Node node, StringBuffer body) throws Exception;

  public Folder getFolder(String username, String accountId, String folderId) throws Exception;

  public String getFolderParentId(String username, String accountId, String folderId) throws Exception;

  public Node getFolderNodeById(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception;

  public Folder getFolder(Node node) throws Exception;

  public List<Folder> getFolders(String username, String accountId) throws Exception;

  public void saveFolder(String username, String accountId, Folder folder) throws Exception;

  public boolean isExistFolder(String username, String accountId, String parentId, String folderId) throws Exception;

  public void saveFolder(String username, String accountId, String parentId, Folder folder) throws Exception;

  public void renameFolder(String username, String accountId, String newName, Folder folder) throws Exception;

  public void removeFolderInMessages(SessionProvider sProvider, String username, String accountId, List<Node> msgNodes, String folderId) throws Exception;

  public void removeUserFolder(String username, String accountId, String folderId) throws Exception;

  public Node getFilterHome(SessionProvider sProvider, String username, String accountId) throws Exception;

  public List<MessageFilter> getFilters(String username, String accountId) throws Exception;

  public MessageFilter getFilterById(String username, String accountId, String filterId) throws Exception;

  public void saveFilter(String username, String accountId, MessageFilter filter, boolean applyAll) throws Exception;

  public void runFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception;

  public void removeFilter(String username, String accountId, String filterId) throws Exception;

  public Node getMessageHome(SessionProvider sProvider, String username, String accountId) throws Exception;

  public Node getFolderHome(SessionProvider sProvider, String username, String accountId) throws Exception;

  public Node getTagHome(SessionProvider sProvider, String username, String accountId) throws Exception;

  public void addTag(String username, String accountId, Tag tag) throws Exception;

  public void addTag(String username, String accountId, List<Message> messages, List<Tag> tagList) throws Exception;

  public List<Tag> getTags(String username, String accountId) throws Exception;

  public Tag getTag(String username, String accountId, String tagId) throws Exception;

  public void removeTagsInMessages(String username, String accountId, List<Message> msgList, List<String> tagIds) throws Exception;

  public void removeTag(String username, String accountId, String tagId) throws Exception;

  public void updateTag(String username, String accountId, Tag tag) throws Exception;

  public List<Message> getMessageByTag(String username, String accountId, String tagId) throws Exception;

  public List<Node> getMessageNodeByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception;

  public Node getSpamFilterHome(String username, String accountId) throws Exception;

  public SpamFilter getSpamFilter(String username, String accountId) throws Exception;

  public void saveSpamFilter(String username, String accountId, SpamFilter spamFilter) throws Exception;

  public void toggleMessageProperty(String username, String accountId, List<Message> msgList, String property, boolean value) throws Exception;

  public String getFolderHomePath(String username, String accountId) throws Exception;

  public List<Folder> getSubFolders(String username, String accountId, String parentPath) throws Exception;

  public void execActionFilter(String username, String accountId, Calendar checkTime) throws Exception;

  public Node getDateStoreNode(SessionProvider sProvider, String username, String accountId, Date date) throws Exception;

  public List<Node> getMatchingThreadAfter(SessionProvider sProvider, String username, String accountId, Node msg) throws Exception;

  public Node getMatchingThreadBefore(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msg) throws Exception;

  public void addMessageToThread(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msgNode) throws Exception;

  public void updateLastTimeToParent(String username, String accountId, Node node, Node parentNode, Calendar cal) throws Exception;

  public Node getReferentParent(String username, String accountId, Node node) throws Exception;

  public Node setIsRoot(String accountId, Node msgNode, Node converNode) throws Exception;

  public Node setIsRoot(String accountId, Node msgNode) throws Exception;

  public void createReference(Node msgNode, Node converNode) throws Exception;

  /*
   * Move reference : to first parent if it is exist, if not move reference to first child message.
   */
  public Node moveReference(String accountId, Node node) throws Exception;

  public List<Message> getReferencedMessages(String username, String accountId, String msgPath) throws Exception;

  public Message loadTotalMessage(String username, String accountId, Message msg, javax.mail.Message message) throws Exception;

  /**
   * 
   * @param username
   * @param msgHomeNode
   * @param accId
   * @param folderId
   * @param msg
   * @param msgId
   * @return
   */
  public byte checkDuplicateStatus(SessionProvider sProvider, String username, Node msgHomeNode, String accId, Node msgNode, String folderId);

  public boolean savePOP3Message(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, String currentUsername) throws Exception;

  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  public SessionProvider createSessionProvider();

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider);

  public SessionProvider createSystemProvider();

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception;

  public Session getSession(SessionProvider sprovider) throws Exception;

  public BufferAttachment getAttachmentFromDMS(String userName, String relPath) throws Exception;

  /**
   * delegate account to receiver 
   * @param user
   * @param receiver
   * @param accountId
   * @throws Exception
   */
  public void delegateAccount(String user, String receiver, String accountId) throws Exception;

  /**
   * remove delegated account of receiver 
   * @param receiver
   * @throws Exception
   */
  public void removeDelegateAccount(String receiver, String accountId) throws Exception;

  /**
   * query all delegated accounts for given user id
   * @param userId
   * @return
   * @throws Exception
   */
  public List<Account> getDelegateAccounts(String userId) throws Exception;

}
