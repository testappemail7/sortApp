package models;

import java.util.*;
import play.data.validation.Constraints.*;
import play.db.ebean.*;
import javax.persistence.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import javax.mail.PasswordAuthentication;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


@Entity
public class Task extends Model{

  @Id
  public Long id;


  @Required
  public int number;


  public String email_address;





   public static Finder<Long,Task> find = new Finder(
      Long.class, Task.class
   );



	public static List<Task> all() {
	  return find.all();
	}

	public static void create(Task task) {
	  task.save();
	}


	public static void delete(Long id) {
	  find.ref(id).delete();
    }



    public static void sort(String email) {

      try {
		  List<Task> allEntities = all();
		  int size = allEntities.size();
		  if(size > 0)
			{
			 String beforeSorting = "";
			 String afterSorting = "";
			 String orderNo = "TA_"+System.currentTimeMillis();
			 ArrayList<Integer> values = new ArrayList<Integer>(5);
			 for(int a = 0; a < size; a++)
				{
				 Task thisTask = (Task)allEntities.get(0);
				 Long nodeId = thisTask.id;
				 if(a == 0)
				   {
					beforeSorting = ""+thisTask.number;
				   }
				 else
				   {
					beforeSorting = beforeSorting+", "+thisTask.number;
				   }
				 values.add(new Integer(thisTask.number));
				 allEntities.remove(0);
				 find.ref(nodeId).delete();
				}

			 Object[] sortedData = Task.BubbleSort(values.toArray());
			 for(int k = 0; k < sortedData.length; k++)
				{
				 Integer val = (Integer)sortedData[k];

				 if(k == 0)
				   {
					afterSorting = ""+val.intValue();
				   }
				 else
				   {
					afterSorting = afterSorting+", "+val.intValue();
				   }
				}

			  String resultOfSort = "Order Number = "+orderNo+"\r\nBefore Sorting = "+beforeSorting+"\r\nAfter Sorting = "+afterSorting;
			  String[] attachments = new String[0];
			  new Email().send(email, "Sorting Test App (results)", resultOfSort, attachments);
			 }

      } catch (Exception e){
	           e.printStackTrace();
	  }
	}

   public static Object[] BubbleSort(Object[] num)
	{
	 int j;
	 boolean flag = true;
	 Integer temp;
	 while(flag)
	 {
	  flag = false;
	  for(j=0;  j < num.length -1;  j++)
	   {
		if((((Integer)num[j]).intValue()) > (((Integer)num[j+1]).intValue()))
		  {
		   temp = ((Integer)num[j]).intValue();
		   num[j] = num[j+1];
		   num[j+1] = temp;
		   flag = true;
		  }
		}
	  }
	return num;
	}



//==================================
// Emailing functionalities...
//==================================

static class Email {


public void send(String recipeintEmail,
                 String subject,
                 String messageText,
                 String []attachments) throws  MessagingException, AddressException {


try {
	String senderEmail = "testappemail7@gmail.com";
	String senderMailPassword = "testappemail";
	String gmail = "smtp.gmail.com";
	Properties props = System.getProperties();
	props.put("mail.smtp.user", senderEmail);
	props.put("mail.smtp.host","smtp.gmail.com");
	props.put("mail.smtp.port","465");
	props.put("mail.smtp.starttls.enable","true");
	props.put("mail.smtp.debug","true");
	props.put("mail.smtp.auth","true");
	props.put("mail.smtp.socketFactory.port","465");
	props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
	props.put("mail.smtp.socketFactory.fallback","false");



	// Required to avoid security exception.
	MyAuthenticator authentication = new MyAuthenticator(senderEmail,senderMailPassword);
	Session session = Session.getInstance(props,authentication);
	session.setDebug(true);
	MimeMessage message = new MimeMessage(session);
	BodyPart messageBodyPart = new MimeBodyPart();
	messageBodyPart.setText(messageText);


	Multipart multipart = new MimeMultipart();
	multipart.addBodyPart(messageBodyPart);


	for(int i= 0; i < attachments.length; i++)
	   {
		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(attachments[i]);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(attachments [i]);
		multipart.addBodyPart(messageBodyPart) ;
	   }


	message.setContent(multipart);
	message.setSubject(subject);
	message.setFrom(new InternetAddress(senderEmail));
	message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipeintEmail));
	Transport transport = session.getTransport("smtps");
	transport.connect(gmail,465, senderEmail, senderMailPassword);
	transport.sendMessage(message, message.getAllRecipients());
	transport.close();

} catch (Exception e){
         e.printStackTrace();
}
}


//______________________________________________________________
//
//               Inner class..
//______________________________________________________________

private class MyAuthenticator extends javax.mail.Authenticator {

String User;
String Password;

public MyAuthenticator(String user, String password){

User = user;
Password = password;
}


//override
public PasswordAuthentication getPasswordAuthentication() {

return new javax.mail.PasswordAuthentication(User, Password);
}

}
}

}