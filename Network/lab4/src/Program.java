import org.omg.CORBA.portable.UnknownException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by Татьяна on 27.10.2016.
 */
public class Program
{
    public final int MAX_MESSAGES_NUMBER = 1000;
    public final int TIMEOUT = 1000;
    private DatagramPacket packet = new DatagramPacket(buf, buf.length);
    private String name;
    private int lossPersentage;
    private int port;
    private DatagramSocket socket;
    private boolean isRoot;
    private SocketAddress parentAddress;
    private SocketChannel parentSocketChannel;
    private static final String MESSAGE = "hello";
    private Scanner scanner = new Scanner(System.in);
    private Map<SocketAddress, Vector<Pair<Message, Long>>> messagesLists = new HashMap<>();
    private Random randomGenerator = new Random();


    // i'm your son
    // my end
    // content message
    // submit msg
    // this is your new father
    //

    public void run() throws UnknownHostException, IOException
    {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(TIMEOUT);

        if (parentAddress != null)
        {
            sendMessageImYourSon(parentAddress);
        }


        while (true)
        {
            if (scanner.hasNext())
            {
                String userMessage = scanner.next();
                Message message = new ContentMessage(userMessage);
                for(Vector<Pair<Message, Long>> messagesList  : messagesLists.values())
                {
                    if(messagesList.size() == MAX_MESSAGES_NUMBER)
                    {
                        messagesList.removeElementAt(0);
                    }
                    messagesList.add(new Pair<>(message, 0L));
                }
            }

            for(Vector<Pair<Message, Long>> messagesList  : messagesLists.values())
            {
                for (Pair<Message, Long> data : messagesList)
                {
                    Message message = data.first;
                    Long lastSendTime = data.second;
                    if(lastSendTime - System.currentTimeMillis() > 5)
                    {
                        message.beSentBy(socket);
                    }
                }
            }

            try
            {
                socket.receive(packet);

                if(randomGenerator.nextInt(100) >= lossPersentage)
                {
                    ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                    Long halfGuid1 = buffer.getLong();
                    Long halfGuid2 = buffer.getLong();

                    // сериализируем 2 long to type guid

                    Message message = new ConfirmMessage(guid);
                    message.beSentBy(socket);

                    int messageID = buffer.getInt();
                    // востанавливаем класс сообщения десериализатором
                    // делаем что-то в соответствии с видом сообщения
                    // например, если это письмо-подтверждение, то мы смотрим номер какого пакета оно подтверждает и удаляем его из очереди писем конкретной программы
                }
            }
            catch (SocketTimeoutException e){}
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException
    {
        Program program = new Program();
        program.name = args[0];
        program.lossPersentage = Integer.parseInt(args[1]);
        program.port = Integer.parseInt(args[2]);
        if (args.length == 5)
        {
            String parentIP = args[3];
            int parentPort = Integer.parseInt(args[4]);
            program.parentAddress = new InetSocketAddress(parentIP, parentPort);
        }
        else
        {
            program.isRoot = true;
        }
        program.run();
    }

    private class Pair<F, S>
    {
        public F first;
        public S second;

        public Pair(F first, S second)
        {
            this.first = first;
            this.second = second;
        }
    }
}

