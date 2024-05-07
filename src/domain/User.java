package domain;

import java.net.Socket;

public record User(String username, Socket socket) {

}
