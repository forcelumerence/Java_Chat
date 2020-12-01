package client;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientWindow extends JFrame
{

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 4004;
  private Socket clientSocket;

  private Scanner inMessage;
  private PrintWriter outmessage;
  private DataOutputStream outNick = null;

  private JTextArea jTextAreaMessage;
  private JTextField jtfMessage;
  private JTextField jtfName;

  private String clientName = "";
  private long startTime;

  public ClientWindow() throws HeadlessException
  {
    try
    {
      clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
      inMessage = new Scanner(clientSocket.getInputStream());
      outmessage = new PrintWriter(clientSocket.getOutputStream());
      outNick = new DataOutputStream(clientSocket.getOutputStream());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }


    setBounds(600, 300, 500, 400);
    setTitle("Client of chat");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    getContentPane().setBackground(Color.BLACK);

    jTextAreaMessage = new JTextArea();
    jTextAreaMessage.setBackground(Color.DARK_GRAY);
    jTextAreaMessage.setForeground(Color.WHITE);
    jTextAreaMessage.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
    jTextAreaMessage.setEditable(false); // позволяет выбирать и копировать его содержимое, но не позволяет изменять его значение.
    jTextAreaMessage.setLineWrap(true); // с новой строки

    JScrollPane jScrollPane = new JScrollPane(jTextAreaMessage); //обеспечивает представление с возможностью прокрутки
    add(jScrollPane, BorderLayout.CENTER);

    JLabel jNumberOfClients = new JLabel("Количество клиентов в чате: ");
    jNumberOfClients.setForeground(Color.WHITE);
    add(jNumberOfClients, BorderLayout.NORTH);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    add(bottomPanel, BorderLayout.SOUTH);

    JButton jbSendMessage = new JButton("Отправить");
    jbSendMessage.setBackground(Color.BLACK);
    jbSendMessage.setForeground(Color.WHITE);
    jbSendMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    bottomPanel.add(jbSendMessage, BorderLayout.EAST);

    jtfMessage = new JTextField(" Введите ваше сообщение ");
    jtfMessage.setBackground(Color.DARK_GRAY);
    jtfMessage.setForeground(Color.LIGHT_GRAY);
    jtfMessage.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
    bottomPanel.add(jtfMessage, BorderLayout.CENTER);

    jtfName = new JTextField(" Введите ваше имя ");
    jtfName.setBackground(Color.DARK_GRAY);
    jtfName.setForeground(Color.LIGHT_GRAY);
    jtfName.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
    bottomPanel.add(jtfName, BorderLayout.WEST);

    jbSendMessage.addActionListener(e -> {
      if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty())
      {
        clientName = jtfName.getText();
        sendMsg();
        jtfMessage.grabFocus();
      }
    });

    jbSendMessage.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        jbSendMessage.setBackground(Color.WHITE);
        jbSendMessage.setForeground(Color.BLACK);
      }
      public void mouseReleased(MouseEvent e) {
        jbSendMessage.setBackground(Color.BLACK);
        jbSendMessage.setForeground(Color.WHITE);
      }

    });

    // это некий указатель, который говорит о том, какой сейчас компонент активен и может реагировать на клавиатуру
    jtfName.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        jtfName.setText("");
        jtfName.setForeground(Color.WHITE);
      }
    });

    jtfMessage.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        jtfMessage.setText("");
        jtfMessage.setForeground(Color.WHITE);
      }
    });

    // листенер для проверки изменения значения TextField
    jtfName.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        warn();
      }
      public void removeUpdate(DocumentEvent e) {
        warn();
      }
      public void insertUpdate(DocumentEvent e) {
        warn();
      }

      public void warn() {
        try {
          // если произошло заполнение поля
          if (!jtfName.getText().trim().isEmpty() && !jtfName.getText().trim().equals("Введите ваше имя")) {
            outNick.writeBoolean(true); // отправка на сервер, что было заполнение
            // outNick.flush();
            // outNick.close();
            // jtfName.getDocument().removeDocumentListener(this);
          }
        } catch (Exception e2) {
          e2.printStackTrace();
        }
      }
    });

    new Thread(() -> {
      try
      {
        while (true)
        {
          if (inMessage.hasNext())
          {
            String inMes = inMessage.nextLine();
            String clientsInChat = "Клиентов в чате";

            if (inMes.indexOf(clientsInChat) == 0)
            {
              jNumberOfClients.setText(inMes);
            }
            else
            {
              jTextAreaMessage.append(inMes);
              jTextAreaMessage.append("\n");
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }).start();

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e)
      {
        super.windowClosing(e);

        try
        {
          if (!clientName.isEmpty() && !clientName.trim().equals("Введите ваше имя"))
          {
            outmessage.println(clientName + " вышел из чата");
          }
          else
          {
            outmessage.println("Участник вышел из чата так и не назвав свое имя");
          }

          outmessage.println("session end");
          outmessage.flush();
          outmessage.close();
          inMessage.close();
          clientSocket.close();
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      }
    });
    setVisible(true);
  }

  private void sendMsg()
  {
    String messageStr = jtfName.getText() + ": " + jtfMessage.getText();
    outmessage.println(messageStr);
    outmessage.flush();
    jtfMessage.setText("");
  }
}
