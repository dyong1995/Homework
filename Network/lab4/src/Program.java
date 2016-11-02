import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;


public class Program implements Serializable
{
    class MessageList  implements Serializable
    {
        private LinkedList<Pair<Message, Long>> messagesList = new LinkedList<>();
        private Map<UUID, Integer> association = new HashMap<>();

        public void removeEldestOne()
        {
            Pair<Message, Long> eldestEntry = messagesList.poll();
            association.remove(eldestEntry.first.getUUID());
        }
        public void removeMessageWithUUID(UUID uuid)
        {
            int idx = association.get(uuid);
            association.remove(uuid);
            messagesList.remove(idx);
        }

        public void addMessage(Message message, Long time)
        {
            Pair data = new Pair(message, time);
            messagesList.add(data);
            int idx = messagesList.indexOf(data);
            association.put(message.getUUID(), idx);
        }

        public int size() {return messagesList.size();}
        public LinkedList getMessageList(){return messagesList;}
        public void clear(){messagesLists.clear(); association.clear();}
    }

    public final int MAX_MESSAGES_NUMBER = 1000;
    public final int TIMEOUT = 1000;
    private String name;
    private int lossPersentage;
    private int port;
    private DatagramSocket socket;
    private SocketAddress parentAddress;
    private Vector<SocketAddress> childAdresses;
    private Scanner scanner = new Scanner(System.in);
    private Random randomGenerator = new Random();
    private Map<SocketAddress, MessageList> messagesLists = new HashMap<>();

    // i'm your son content number of packet +
    // i'm not your child anymore content number of packet +
    // content message content string and number of packet +
    // submit msg number of packet +
    // this is your new father socketAddress +
    //

    Message deserializeMessage(ByteBuffer buffer)
    {
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (Message) objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println("Can't deserialize a message");
            System.exit(1);
            return null;
        }
    }

    byte[] serializeMessage(Message message)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            objectOutputStream.close();
            return outputStream.toByteArray();
        }
        catch (IOException e)
        {
            System.err.println("Can't serialize a message");
            System.exit(1);
            return null;
        }
    }

    public void run() throws UnknownHostException, IOException
    {
        socket = new DatagramSocket(port);
        socket.setSoTimeout(TIMEOUT);

        if (parentAddress != null)
        {
            Message message = new ImYourChildMessage();
            messagesLists.put(parentAddress, new MessageList());
            messagesLists.get(parentAddress).addMessage(message, 0L);
        }

        while (true)
        {
            if (System.in.available() > 0)
            {
                String userMessage = scanner.next();
                if(userMessage == "exit")
                {
                    if(parentAddress != null)
                    {

                        Message message = new ImNotYourChildAnymoreMessage();
                        messagesLists.get(parentAddress).clear();
                        messagesLists.get(parentAddress).addMessage(message, 0L);

                        for(SocketAddress childAddress : childAdresses)
                        {
                            Message msg = new YouHaveNewFatherMessage(parentAddress);
                            messagesLists.get(childAddress).clear();
                            messagesLists.get(childAddress).addMessage(msg, 0L);
                        }
                    }
                    else
                    {
                        boolean isFirst = true;
                        SocketAddress newParentAddress = null;
                        Message msg;
                        for(SocketAddress childAddress : childAdresses)
                        {
                            if(isFirst)
                            {
                                msg = new YouHaveNewFatherMessage(null);
                                newParentAddress = childAddress;
                            }
                            else
                            {
                                msg = new YouHaveNewFatherMessage(newParentAddress);
                            }
                            messagesLists.get(childAddress).clear();
                            messagesLists.get(childAddress).addMessage(msg, 0L);
                        }
                    }
                    break;
                }
                Message message = new ContentMessage(name + ": " + userMessage);
                for (MessageList messagesList : messagesLists.values())
                {
                    if (messagesList.size() == MAX_MESSAGES_NUMBER)
                    {
                        messagesList.removeEldestOne();
                    }
                    messagesList.addMessage(message, 0L);
                }
            }

            for (Map.Entry<SocketAddress, MessageList>  entry : messagesLists.entrySet())
            {
                SocketAddress socketAddress = entry.getKey();
                LinkedList<Pair<Message, Long>> messagesList = entry.getValue().getMessageList();
                for (Pair<Message, Long> data : messagesList)
                {
                    Message message = data.first;
                    Long lastSendTime = data.second;
                    if (System.currentTimeMillis() - lastSendTime > 50)
                    {
                        byte[] buffer = serializeMessage(message);
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socketAddress);
                        socket.send(packet);
                    }
                }
            }
            try
            {
                DatagramPacket recievedPacket = new DatagramPacket(new byte[1024], 1024);
                socket.receive(recievedPacket);
                if (randomGenerator.nextInt(100) >= lossPersentage)
                {
                    ByteBuffer buffer = ByteBuffer.wrap(recievedPacket.getData());
                    Message message = deserializeMessage(buffer);
                    message.setFrom(recievedPacket.getSocketAddress());
                    message.beProcessed();
                    message = new ConfirmMessage(message.getUUID());
                    messagesLists.get(recievedPacket.getSocketAddress()).addMessage(message, 0L);
                }
            }
            catch (SocketTimeoutException e) {}
        }

        boolean isReadyToExit = false;
        while(!isReadyToExit)
        {
            if(parentAddress == null || messagesLists.get(parentAddress).size() == 0)
            {
                isReadyToExit = true;
                for (SocketAddress childAddress : childAdresses)
                {
                    if(messagesLists.get(childAddress).size() != 0)
                    {
                        isReadyToExit = false;
                    }
                }
            }
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
        program.run();
    }

    private class Pair<F, S>  implements Serializable
    {
        public F first;
        public S second;

        public Pair(F first, S second)
        {
            this.first = first;
            this.second = second;
        }
    }


    public static abstract class Message  implements Serializable
    {
        protected UUID uuid = UUID.randomUUID();
        protected SocketAddress from;

        public abstract void beProcessed();
        public UUID getUUID() {return uuid;}
        public void setFrom(SocketAddress from) {this.from = from;}
    }


    public  static class  ImYourChildMessage extends Message implements Serializable
    {
        private Map<SocketAddress, MessageList> messagesLists;
        private SocketAddress childAddress;
        ImYourChildMessage(Map<SocketAddress, MessageList> messagesLists, SocketAddress childAddress)
        {
            this.messagesLists = messagesLists;
            this.childAddress = childAddress;
        }
        public void beProcessed()
        {
            System.out.println("i have a child!");
            childAdresses.add(from);
            messagesLists.put(from, new MessageList());
        }
    }

    public  static class  ImNotYourChildAnymoreMessage extends Message implements Serializable
    {
        public void beProcessed()
        {
            childAdresses.remove(from);
            messagesLists.remove(parentAddress);
        }
    }

    public  static class  ContentMessage extends Message implements Serializable
    {
        private String message;
        ContentMessage(String message){this.message = message;}
        public void beProcessed() {System.out.println(message);}
    }

    public  static class  YouHaveNewFatherMessage extends Message implements Serializable
    {
        private SocketAddress newParentAddress;
        YouHaveNewFatherMessage(SocketAddress newParentAddress) {this.newParentAddress = newParentAddress;}
        public void beProcessed()
        {
            messagesLists.remove(parentAddress);
            parentAddress = newParentAddress;

            Message message = new ImYourChildMessage();
            messagesLists.put(parentAddress, new MessageList());
            messagesLists.get(parentAddress).addMessage(message, 0L);
        }
    }

    public  static class  ConfirmMessage extends Message implements Serializable
    {
        private UUID uuidToConfirm;

        ConfirmMessage(UUID uuidToConfirm) {this.uuidToConfirm = uuidToConfirm;}
        public void beProcessed()
        {
            MessageList messagesList = messagesLists.get(from);
            messagesList.removeMessageWithUUID(uuidToConfirm);
        }
    }
}




