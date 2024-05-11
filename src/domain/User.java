package domain;

import java.net.Socket;

//Objeto que transfere os dados de um respectivo usuário e o seu socket
public record User(String username, Socket socket) {

}
