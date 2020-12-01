package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server
{
  static final int PORT = 4004;
  private List<ClientHandler> clients = new ArrayList<>();

  public Server()
  {
    ServerSocket serverSocket = null;
    Socket clientSocket = null;

    try
    {
      serverSocket = new ServerSocket(PORT);
      System.out.println("Сервер запущен");

      while (true)
      {
        clientSocket = serverSocket.accept();
        ClientHandler newClient = new ClientHandler(clientSocket, this);
        clients.add(newClient);

        new Thread(newClient).start();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        clientSocket.close();
        serverSocket.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

  }

  public void sendMsgToAllClients(String msgText)
  {
    clients.forEach(clientHandler -> clientHandler.sendMessage(msgText));
  }

  public void removeClient(ClientHandler clientHandler)
  {
    clients.remove(clientHandler);
  }


}
