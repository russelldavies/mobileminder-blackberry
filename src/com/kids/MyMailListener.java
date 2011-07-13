package com.kids;

import com.kids.prototypes.Debug;
import com.kids.prototypes.LocalDataWriter;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.mail.event.StoreEvent;
import net.rim.blackberry.api.mail.event.StoreListener;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;

public class MyMailListener implements FolderListener, StoreListener
{
	private LocalDataWriter actLog;
			MailMessage 	messageObject;
			Message			emailMessage;
			Debug   		logWriter				  = Logger.getInstance();
			boolean 		_hasSupportedAttachment	  = false; 
			boolean 		_hasUnsupportedAttachment = false;

    public MyMailListener(LocalDataWriter inputAccess)
    {
    	logWriter.log("Start MyCallListener");
        actLog = inputAccess;
        messageObject = new MailMessage();
        
        // The following is a recursive method to search all folders on the device
        // Since corporate users will have different locations than personal users, we need
        // to search for their INBOX/OUTBOX, instead of specifying a path.
        // BONUS: This code can be used on Mobile Minder, and the corporate equivalent later on!
        ServiceBook sb = ServiceBook.getSB();
        ServiceRecord[] srs = sb.getRecords();

        for(int cnt = srs.length - 1; cnt >= 0; --cnt)
        {
        	//identify the service record associated with a mail message service via a CID of 'CMIME'
			if( srs[cnt].getCid().equals( "CMIME" ))
			{
				ServiceConfiguration sc = new ServiceConfiguration(srs[cnt]);
                Store store = Session.getInstance(sc).getStore();
                //store.addFolderListener(this);
                
                //then search recursively for INBOX and OUTBOX folders
        		Folder[] folders = store.list();
        		for( int foldercnt = folders.length - 1; foldercnt >= 0; --foldercnt)
        		{
        			Folder f = folders[foldercnt];
        			recurse(f);
        		} // end for()
			} // end if()
        }  // end for()
    }  

    /**
     * Recursive method to search a folder, and its sub-folders on
     * the device to see if it matches "Folder.INBOX"
     * @param f The folder which we want to compare with "Folder.INBOX"
     */
    public void recurse(Folder f)
    {
       if ( f.getType() == Folder.INBOX || f.getType() == Folder.SENT)
       {	// If it matches, we add the listener
    	   logWriter.log("Folder matching INBOX found! "+f.getFullName());
           f.addFolderListener(this);
           
       }
       Folder[] farray = f.list();
       //Search all the folders sub-folders
       for (int fcnt = farray.length - 1; fcnt >= 0; --fcnt)
       {
           recurse(farray[fcnt]);
       }
    }
    
// Folders and listeners should all be taken care of at this stage
// Now we get onto handling the incoming message

	public void messagesAdded(FolderEvent e)
	{
		messageObject.clearData();
		logWriter.log("Email message "+(e.getMessage().isInbound()?"received":"sent"));
		emailMessage = e.getMessage();

		boolean isInbound = (e.getMessage().isInbound()?true:false);
		messageObject.setMailDirection(isInbound);
		
		try
		{
			logWriter.log("MailListener::messaesAdded::setting Message");
			if (isInbound)	// If its inbound it should also only have 1 "from", but maybe other TO or CCs
			{
			String name = emailMessage.getFrom().getName();
			name = name.substring(1,emailMessage.getFrom().getName().length()-1);
			
			messageObject.setMessage(emailMessage.getFrom().getAddr(),
									 name,
									 emailMessage.getSubject(),
									 emailMessage.getBodyText(),
									 emailMessage.isInbound()?(byte)1:(byte)0,	// if its true, send 1, else 0
									 emailMessage.getSentDate().toString(),
									 _hasSupportedAttachment||_hasUnsupportedAttachment);
			}
			else
			{
				// Retrieve all types of recipient
				Address[] sentTo  = emailMessage.getRecipients(Message.RecipientType.TO);
				Address[] sentCc  = emailMessage.getRecipients(Message.RecipientType.CC);
				Address[] sentBcc = emailMessage.getRecipients(Message.RecipientType.BCC);			
				
				// Loops through the arrays and pulls out Recipient names
				StringBuffer allRecipientsNames = new StringBuffer();
				StringBuffer allRecipientsEmails = new StringBuffer();

				for (int count=0 ; count < sentTo.length ; count++)
				{
					allRecipientsEmails.append(sentTo[count].getAddr());
					allRecipientsEmails.append(";");
					//allRecipientsNames.append(sentTo[count].getName().substring(1, sentTo[count].getName().length()-1));
					//allRecipientsNames.append(";");	// This "name" substring is seperated by a ";"
				}
				for (int count=0 ; count < sentCc.length ; count++)
				{
					allRecipientsEmails.append(sentCc[count].getAddr());
					allRecipientsEmails.append(";");
					//allRecipientsNames.append(sentCc[count].getName().substring(1, sentCc[count].getName().length()-1));
					//allRecipientsNames.append(";");	// This "name" substring is seperated by a ";"
				}	
				for (int count=0 ; count < sentBcc.length ; count++)
				{
					allRecipientsEmails.append(sentBcc[count].getAddr());
					allRecipientsEmails.append(";");
					//allRecipientsNames.append(sentBcc[count].getName().substring(1, sentBcc[count].getName().length()-1));
					//allRecipientsNames.append(";");	// This "name" substring is seperated by a ";"
				}
				
				
				messageObject.setMessage(allRecipientsEmails.toString(),
										 "",  //No names on outbound emails, just the email address itself
										 emailMessage.getSubject(),
										 emailMessage.getBodyText(),
										 emailMessage.isInbound()?(byte)1:(byte)0,	// if its true, send 1, else 0
										 emailMessage.getSentDate().toString(),
										 _hasSupportedAttachment||_hasUnsupportedAttachment
										 );
			}
			
			logWriter.log("Message set");
			logWriter.log("RESTstring: "+messageObject.getREST());
		}
		catch (MessagingException e1)
		{
			logWriter.log("x::MailListener::readEmailBody::MessagingException::"+e1.getMessage());
			e1.printStackTrace();
		}
		logWriter.log("MailListener::messagesAdded::Adding message to log");
		actLog.addMessage(messageObject);
	}

	public void messagesRemoved(FolderEvent e)
	{
		logWriter.log("Messages deleted");		
	}

	public void batchOperation(StoreEvent e)
	{
		logWriter.log("Batch operation");
	}
	/*
	 
	private void readEmailBody(MimeBodyPart mbp)
	{	}

	private void readEmailBody(TextBodyPart tbp)
	{	}
	
	private void findEmailBody(Object obj)
	{
	   //Reset the attachment flags.
	   _hasSupportedAttachment = false;
	   _hasUnsupportedAttachment = false;
	   MimeBodyPart mbp = (MimeBodyPart)obj;

	   if(obj instanceof Multipart)
	   {
		   Multipart mp = (Multipart)obj;
	    
	       for(int count=0; count < mp.getCount(); ++count)
	       {
	    	   findEmailBody(mp.getBodyPart(count));
	       }
	   }
	    
	   else if (obj instanceof TextBodyPart)
	   {
		   TextBodyPart tbp = (TextBodyPart) obj;
	       readEmailBody(tbp);
	   }
	   else if (obj instanceof MimeBodyPart)
	   {
	       if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	       {
	    	   readEmailBody(mbp);
	       }
	   }
	   else if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
	   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
	   {
		   //The message has attachments or we are at the top level of the message.
	       //Extract all of the parts within the MimeBodyPart message.
	       findEmailBody(mbp.getContent());
	   }
	   
	   else if (obj instanceof SupportedAttachmentPart)  
	   {
		   _hasSupportedAttachment = true;
	   }

	   else if (obj instanceof UnsupportedAttachmentPart) 
	   {
		   _hasUnsupportedAttachment = true;
	   }
	}*/
}