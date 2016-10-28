import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Татьяна on 27.10.2016.
 */
public class Program
{
    private String name;
    private int losses;
    private int port;
    private String parentIP;
    private int parentPort;
    private boolean isRoot;
    private SocketChannel parentSocketChannel;


    public void run()
    {
        InetSocketAddress address = new InetSocketAddress(parentIP, parentPort);
        try
        {
            parentSocketChannel = SocketChannel.open(address);
            ServerSocketChannel server = n;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        Program program = new Program();
        program.name = args[0];
        program.losses = Integer.parseInt(args[1]);
        program.port = Integer.parseInt(args[2]);
        if(args.length == 5)
        {
            program.parentIP = args[3];
            program.parentPort = Integer.parseInt(args[4]);
        }
        else
        {
            program.isRoot = true;
        }
        program.run();
    }
}
