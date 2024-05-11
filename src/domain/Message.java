package domain;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Objeto que transmite os dados de comunicação entre os usuários de um servidor
public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String sender;
	private String recipient;
	private String text;
	private UserFile userFile;
	private String timeStamp;
	private Action action;
	private List<String> onlineUsers = new ArrayList<>();
	
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public UserFile getUserFile() {
		return this.userFile;
	}
	
	public void setUserFile(UserFile file) throws IOException {
		this.userFile = file;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm]");
		this.timeStamp = sdf.format(date);
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<String> getOnlineUsers() {
		return onlineUsers;
	}

	public void setOnlineUsers(List<String> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}

	public enum Action {
		CONNECT, DISCONNECT, SEND, SEND_ONE, USERS_ONLINE
	}

}
