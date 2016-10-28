import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class Message
{
    protected int ID;
    protected int length;
    protected int packetID;

    public void parse(SocketChannel channel) throws IOException
    {}
    public void setLength(int length){this.length = length;}
    public int getLength(){return length;}
    public int getID()
    {
        return ID;
    }
}