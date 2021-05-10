import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;

public class Server 
{
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private ServerSocket socket;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private FileInputStream fileIn;
    private String filename;
    private int bytes;
    private byte[] buffer = new byte[BUFFER_SIZE];
    
    private static boolean debug = false;   
    private static boolean b_range = false;
    private static boolean write = false;
    private long start;
    private long end;
    public Server(int port) 
    {
        try 
        {
            socket = new ServerSocket(port);
            System.out.println("Port: " + port);
            if(debug)
            	System.out.println("Debug = 1");
            else
            	System.out.println("Debug = 0");
            // Wait for connection and process it
            while (true) 
            {
                try 
                {
                    connection = socket.accept(); // Block for connection request

                    socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
                    socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client

                    filename = socketIn.readUTF(); // Read filename from client
                                        
                    String bool_write = socketIn.readUTF();
                    if(bool_write.equals("true"))
                    {
                        //Write to server
                    	File file = new File(filename);
                    	if(!file.exists())
                    	{
	            	        String exists = socketIn.readUTF();
	            	        if(exists.equals("false"))
	            	        {
	                            b_range = false;
	                            write = false;
	                            if(debug)
	                            	System.out.println("Error: Could not retrieve file");
	                            continue;
	            	        }
                    		
	            	        FileOutputStream fileOutputStream = new FileOutputStream(filename);
	            	        Path path = Paths.get(filename);
	            	        long size = socketIn.readLong();
	            	        // Read file contents from server
	            	        while (size > 0 && (bytes = socketIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) 
	            	        {
	            	        	fileOutputStream.write(buffer, 0, bytes);
	            	            size -= bytes;
	            	        }
	            	        connection.close();
	            	        fileOutputStream.close();
	            	        
	            	        if(debug)
	            	        	System.out.println("Successfully Recieved " + filename);
                    	}
                    	else
                    		System.out.println("Error: " + filename + " already exists!");
                    }
                    else
                    {
	                    String bool_range = socketIn.readUTF();
	                    if(bool_range.equals("true"))
	                    {
	                    	b_range = true;
	                    	start = socketIn.readLong();
	                    	end = socketIn.readLong();
	                    }
	                                      
	                    File file = new File(filename);
	                    if(file.exists())
	                    {
		                    long flen = file.length();
		                    long len = end - (start-1);
		                    if(b_range)
		                    {
		                    	if(end > flen)
		                    	{
		                    		end = flen;
		                    		len = end - (start-1);
		                    		if(debug)
		                    			System.out.println("End byte specified exceeds number of bytes in file, end is now " + flen + " bytes.");
		                    	}
		                    	if(start > flen)
		                    	{
		                    		System.out.println("Error: Start byte specified exceeds number of bytes in file.\nAborting file transfer...");
		                            b_range = false;
		                            write = false;
			                    	socketOut.writeUTF("false");
		                            continue;
		                    	}
		                    	if(len <= 0 || start <= 0 || end <= 0)
		                    	{
		                    		System.out.println("Error: Start and End bytes must be valid!");
		                    		if(start > end)
		                    			System.out.println("Start byte is larger than end byte.");
		                    		if(start <= 0 || end <= 0)
		                    			System.out.println("Either start byte or end byte is less than one.");
		                    		
		                    		System.out.println("Aborting file transfer...");
		                    		b_range = false;
		                    		write = false;
			                    	socketOut.writeUTF("false");
		                    		continue;
		                    	}
		                    }
		                    
	                    	socketOut.writeUTF("true");
		                    fileIn = new FileInputStream(filename);
		                    
		                    socketOut.writeLong(flen);
		                    // Write file contents to client
		                    
		                    if(debug)
		                    	System.out.println("Sending " + filename + " to " + connection.getInetAddress().getHostAddress());
		                    
		                    long read = 0;
		                    int val = 10;
		                    
		                    //If there's a start and end byte range
		                    if(b_range)
		                    {	                    	
		                    	fileIn.skip((int)(start - 1));
			                    while ((bytes = fileIn.read(buffer, 0, (int)Math.min(buffer.length, len))) != -1) 
			                    {
			                    	boolean end = false;
			                        read += bytes;
			                        
			                        if(read >= len)
			                        {
			                        	while(read > len)
			                        	{
			                        		read--;
			                        		bytes--;
			                        	}
			                        	end = true;
			                        }
			                        
			                    	socketOut.write(buffer, 0, bytes); // Write bytes to socket
			                        if(debug)
			                        {
				                        double ratio = ((double)read/(double)len) * 100;
				                        int rounded = ((int) Math.floor(ratio/10))*10;
				                        if(val < rounded)
				                        {
				                        	for(int i = val; i < rounded; i += 10)
				                        		System.out.println("Sent " + i + "%" + " " + filename);
				                        	
				                        	val = rounded;
				                        }
			                        	if(val == 100)
			                        	{
			                        		System.out.println("Sent 100" + "%" + " " + filename);
			                        	}
				                    }         
			                        socketOut.flush();
			                    	if(end)
			                    		break;
			                    }
		                    }
		                    //Read the whole file from start to end
		                    else
		                    {
			                    while ((bytes = fileIn.read(buffer)) != -1) 
			                    {        	
			                        socketOut.write(buffer, 0, bytes); // Write bytes to socket
			                        
			                        if(debug)
			                        {
				                        read += bytes;
				                        double ratio = ((double)read/(double)flen) * 100;
				                        int rounded = ((int) Math.floor(ratio/10))*10;
				                        if(val < rounded)
				                        {
				                        	for(int i = val; i < rounded; i += 10)
				                        		System.out.println("Sent " + i + "%" + " " + filename);
				                        	
				                        	val = rounded;
				                        }
			                        	if(val == 100)
			                        	{
			                        		System.out.println("Sent 100" + "%" + " " + filename);
			                        	}
				                    }	                        
			                        socketOut.flush();
			                    }
		                    }
		                    
		                    fileIn.close();
		                    
		                    if(debug)
		                    	System.out.println("Finished sending " + filename + " to " + connection.getInetAddress().getHostAddress());
	                    }
	                    else
	                    {
	                    	System.out.println("Error: File does not exist!");
	                    	socketOut.writeUTF("false");
	                    }
                    }
                    
                    b_range = false;
                    write = false;
                } 
                catch (Exception ex) 
                {
                    System.out.println("Error: " + ex);
                } 
                finally 
                {
                    // Clean up socket and file streams
                    if (connection != null) {
                        connection.close();
                    }

                    if (fileIn != null) {
                        fileIn.close();
                    }
                }
            }
        } 
        catch (IOException i) 
        {
            System.out.println("Error: " + i);
        }
    }
    
    public static boolean isInteger(String s) 
    {
        boolean isValidInteger = false;
        try
        {
           Integer.parseInt(s);
      
           isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
        }
   
        return isValidInteger;
     } 

    public static void main(String[] args) 
    {
    	int port = 5000;
    	boolean p = false;
    	boolean in = false; 
    	
    	if(args.length <= 3)
    	{
    		for(int i = 0; i < args.length; i++)
    		{
    			if(args[i].equals("DEBUG=1") && debug != true)
    			{
    				debug = true;
    				continue;
    			}
    			else if(args[i].equals("Port") && p != true)
    			{
    				p = true;
    				continue;
    			}
    			else if(isInteger(args[i]) && p == true && in == false)
    			{
    				port = Integer.parseInt(args[i]);
    				in = true;
    			}
    			else
    			{
        			System.out.println("Invalid arguments");
    	    		System.out.println("Usage: Server [DEBUG=1] [Port <port_number>]");
    	    		
    	    		if(p == true && in == false)
    	    			System.out.println("Must specify valid Integer!");
    	    		
        			return;
    			}
    		}
    		if((debug == false || p == false) && args.length == 3)
    		{
    			System.out.println("Invalid arguments");
	    		System.out.println("Usage: Server [DEBUG=1] [Port <port_number>]");
	    		
    			return;
    		}
    		
    	}
    	else
    	{
	    		System.out.println("Invalid Arguments");
	    		System.out.println("Usage: Server [DEBUG=1] [Port <port_number>]");
	    		
	    		return;
	    }
    	
        Server server = new Server(port);
    }
}
