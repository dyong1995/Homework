import org.omg.CORBA.portable.UnknownException;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by Татьяна on 27.10.2016.
 */
public class Program
{
    public int MAX_MESSAGES_NUMBER = 1000;
    private String name;
    private int losses;
    private int port;
    private boolean isRoot;
    private SocketAddress parentAddress;
    private SocketChannel parentSocketChannel;
    private static final String MESSAGE = "hello";
    private Scanner scanner = new Scanner(System.in);
    private Map<SocketAddress, Vector<Pair<Message, Long>>> messagesLists = new HashMap<>();


    // i'm your son
    // my end
    // content message
    // submit msg
    // this is your new father
    //

    public void run() throws UnknownHostException
    {
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
                    if(data.second - System.currentTimeMillis() > 5)
                    {
                        //переотправить
                    }
                }
            }


            // проверка ввода пользователя
            // если написал, проверяем размеры очередей детей/родитей, если необходимо удаляем там самое древнее письмо
            // если написал, то кладем это в очередь писем для детей и родителей со временем последней отправкой = 0
            // проверяем очередь писем для родителей и детей, если время с момента прошлой отправкой больше чем 10 секунду, повторяем
            // проверка входящих
            // решаем потеряли ли мы это сообщение или нет
            // если не потеряли
            // читаем номер пакета
            // шлём подтверждение доставки пакета
            // смотрим что за вид сообщения
            // востанавливаем класс сообщения десериализатором
            // делаем что-то в соответствии с видом сообщения
            // например, если это письмо-подтверждение, то мы смотрим номер какого пакета оно подтверждает и удаляем его из очереди писем конкретной программы
            // если потеряли, то начинаем сначала
        }
        DatagramPacket helloPacket = new DatagramPacket(MESSAGE.getBytes(), MESSAGE.length(), broadcastIp, PORT);
        DatagramPacket recievedPacket = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket(PORT);
        socket.setSoTimeout(TIMEOUT);

        long lastTime = System.currentTimeMillis();
        while (true)
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= TIMEOUT)
            {
                socket.send(helloPacket);
                lastTime = currentTime;
            }
            try
            {
                socket.receive(recievedPacket);
                currentTime = System.currentTimeMillis();
                if (programsOnNet.put(recievedPacket.getAddress(), currentTime) == null)
                {
                    ++programsCount;
                    programSetChanged = true;
                }
            }
            catch (SocketTimeoutException e)
            {
                currentTime = System.currentTimeMillis();
            }

            Iterator<Map.Entry<InetAddress, Long>> it = programsOnNet.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<InetAddress, Long> entry = it.next();
                if (currentTime - entry.getValue() > TIMEOUT * 2)
                {
                    it.remove();
                    --programsCount;
                    programSetChanged = true;
                }
            }

            if (programSetChanged)
            {
                System.out.println("We found " + programsCount + " program(s) on the net with following ip address(es):");
                for (InetAddress keyVal : programsOnNet.keySet())
                {
                    System.out.println(keyVal.toString());
                }
                programSetChanged = false;
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException
    {
        Program program = new Program();
        program.name = args[0];
        program.losses = Integer.parseInt(args[1]);
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

