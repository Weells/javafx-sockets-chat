package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private Integer clients;
	
	public ConnectionResponse initializeServer(Integer serverPort) {
		try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
			clients = 0;
			while(true) {
				Socket socket = serverSocket.accept();
				clients++;
				
				ServerThread thread = new ServerThread(socket);
				thread.setName("Thread Servidor: " + String.valueOf(clients));
				thread.start();
			}
		} catch(BindException e) {
			return ConnectionResponse.ADDRESS_IN_USE;
		} catch (IOException e) {
			return ConnectionResponse.ERROR;
		}
	}
	
	public enum ConnectionResponse {
		CONNECTED, ADDRESS_IN_USE, USERNAME_IN_USE, ERROR
	}
}
