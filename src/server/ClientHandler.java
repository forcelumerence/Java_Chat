package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientHandler implements Runnable
{
  private PrintWriter outMessage;
  private Scanner inMessage;
  private DataInputStream inNick;

  private static final int PORT = 4004;
  private static final String HOST = "localhost";
  private Socket clientSocket;
  private Server server;
  private static int clientsCount = 0;
  private static String KEY_OF_SESSION_END = "session end";

  private static String NEW_CLIENTS_MSG = "Новый участник! Теперь нас = ";
  private static String EXIT_CLIENT_MSG = "Участник вышел! Теперь нас = ";
  private static String TIMEOUT_EXIT_CLIENT_MSG = "Истекло время ожидания участника! Теперь нас = ";

  private long startTime;

  private boolean nickStatus = false; // статус заполнения ника, по умолчанию false

  public ClientHandler(Socket clientSocket, Server server)
  {

    clientsCount++;
    this.clientSocket = clientSocket;
    this.server = server;
    try
    {
      this.outMessage = new PrintWriter(clientSocket.getOutputStream());
      this.inMessage = new Scanner(clientSocket.getInputStream());
      this.inNick = new DataInputStream(clientSocket.getInputStream());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void run()
  {
    try
    {
      startTime = System.currentTimeMillis(); // засекаем время начала работы
      server.sendMsgToAllClients(NEW_CLIENTS_MSG + clientsCount);

      while (true) {

        //слушаем значение статуса от клиента
        try
        {
          nickStatus = inNick.readBoolean();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        // на 120-й секунде проверяем, было ли изменение
        if ((System.currentTimeMillis() - startTime) >= 120000) {
          // если было заполнение поля имени и пришло true, то ничего не произойдет
          // если заполнения не было, то значение nickStatus останется false и произойдет выход из цикла и вызов exitClientSession()
          if (nickStatus == false) {
            server.sendMsgToAllClients(TIMEOUT_EXIT_CLIENT_MSG + clientsCount);
            break;
          }
        }

        if (inMessage.hasNext()) {
          String clientsMsg = inMessage.nextLine();
          System.out.println(clientsMsg);

          if (clientsMsg.equalsIgnoreCase(KEY_OF_SESSION_END)) {
            break;
          }
          server.sendMsgToAllClients(clientsMsg);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      exitClientSession();
    }
  }

  public void sendMessage(String msgText)
  {
    outMessage.println(msgText);
    outMessage.flush();
  }

  public void exitClientSession()
  {
    server.removeClient(this);
    clientsCount--;
    server.sendMsgToAllClients(EXIT_CLIENT_MSG + clientsCount);
  }

}
