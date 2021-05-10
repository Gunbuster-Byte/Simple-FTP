import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.lang.*;

public class Client 
{
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private int bytes;
    private byte[] buffer = new byte[BUFFER_SIZE];

    private static boolean b_range = false;
    private static boolean write = false;
    
    public Client(String host, int port, String filename, long start, long end) 
    {
    	boolean server_file = true;
    
        try 
        {
            connection = new Socket(host, port);
            
            socketIn = new DataInputStream(connection.getInputStream()); // Read data from server
            socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to server
            
            socketOut.writeUTF(filename); // Write filename to server
            if(write)
            {
            	socketOut.writeUTF("true");
                
            	File file = new File(filename);
                
            	if(file.exists())
                	socketOut.writeUTF("true");
                else
                {
                	socketOut.writeUTF("false");
                	throw new FileNotFoundException();
                }
                
                long flen = file.length();
                
                socketOut.writeLong(flen);
            }
            else
            {
            	socketOut.writeUTF("false");
            
	            if(b_range)
	            	socketOut.writeUTF("true");
	            else
	            	socketOut.writeUTF("false");
            }
            if(b_range)
            {
            	socketOut.writeLong(start);
            	socketOut.writeLong(end);
            }
            
            if(!write)
            {
	            String exists = socketIn.readUTF();
	            if(exists.equals("false"))
	            {
	            	server_file = false;
                	throw new FileNotFoundException();
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
            }
            else
            {
            	FileInputStream fileIn = new FileInputStream(filename);
            	
            	while ((bytes = fileIn.read(buffer)) != -1) 
                {   
                    socketOut.write(buffer, 0, bytes); // Write bytes to socket
                    
                    socketOut.flush();
                }
                fileIn.close();
            }
            
        } 
        catch (UnknownHostException ex) 
        {
            System.out.println("Error: Host " + host + " could not be found.");
        }
        catch(FileNotFoundException ex)
        {
        	if(server_file)
        		System.out.println("Error: file " + filename + " could not be found.");
        	else
        		System.out.println("Error: file " + filename + " could not be found at the server.");
        }
        catch (IOException ex) 
        {
            System.out.println("Error: " + ex);
        }
    }
    public static boolean isInteger(String s) 
    {
        boolean isValidInteger = false;
        try
        {
           Long.parseLong(s);
      
           isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
        	System.out.println(s + " is not an integer!");
        }
   
        return isValidInteger;
     } 
    public static void main(String[] args) 
    {
    	long start = 0;
    	long end = 0;
    	int port = 5000; //default port
    	String filename = "";
    	String host = "";
    	
    	boolean w_flag = false;
    	boolean p_option = false;
    	boolean s_flag = false;
    	boolean e_flag = false;
    	boolean file_flag = false;
    	boolean serv_flag = false;
    	
    	//all flags can be put anywhere
    	if(args.length >= 2 && args.length <= 9)
    	{
    		for(int i = 0; i < args.length; i++)
    		{
    			if(args[i].equals("Port"))
    			{
    				if(i+1 < args.length)
    				{
	    				if(isInteger(args[i+1]))
	    				{
	    					port = Integer.parseInt(args[i+1]);
	    					p_option = true;
	    					i++;
	    					continue;
	    				}
	    				else
	    				{
	    					System.out.println("Please enter an integer for the port number.");
	    					System.out.println("Example: Client Port 50...");
	    					return;
	    				}
    				}
    				else
    				{
    					System.out.println("Invalid argument.");
    					System.out.println("Example: Client Port 50...");
    					return;
    				}
    			}
    			if(args[i].equals("-w"))
    			{
    				w_flag = true;
    				write = true;
    				continue;
    			}
    			if(args[i].equals("-s"))
    			{
    				
    				if(i+1 < args.length)
    				{
	    				if(isInteger(args[i+1]))
	    				{
	    					start = Long.parseLong(args[i+1]);
	        				s_flag = true;
	        				i++;
	    					continue;
	    				}
	    				else
	    				{
	    					System.out.println("Please enter an integer greater than 0 for the starting byte.");
	    					System.out.println("Example: Client -s 50...");
	    					return;
	    				}
    				}
    				else
    				{
    					System.out.println("Invalid argument.");
    					System.out.println("Example: Client -s 50...");
    					return;
    				}
    			}
    			if(args[i].equals("-e"))
    			{
    				
    				if(i+1 < args.length)
    				{
	    				if(isInteger(args[i+1]))
	    				{
	    					end = Long.parseLong(args[i+1]);
	        				e_flag = true;
	        				i++;
	    					continue;
	    				}
	    				else
	    				{
	    					System.out.println("Please enter an integer greater than 0 for the ending byte.");
	    					System.out.println("Example: Client -e 50...");
	    					return;
	    				}
    				}
    				else
    				{
    					System.out.println("Invalid argument.");
    					System.out.println("Example: Client -e 50...");
    					return;
    				}
    			}
    			if(serv_flag == false)
    			{
    				host = args[i];
    				serv_flag = true;
    			}
    			else if(file_flag == false)
    			{
    				filename = args[i];
    				file_flag = true;
    			}
    			else
    			{
    				System.out.println("Invalid arguments.");
    	    		System.out.println("Usage: Client [Port #] <server-name> [-w] <file-name> [-s StartBlock] [-e LastBlock]");
    	    		return;
    			}
    		}
    		if(file_flag == false || serv_flag == false)
    		{
    			System.out.println("Please provide a host name and file name");
				System.out.println("Example: Client 127.0.0.1 hello.txt...");
				return;
    		}
    		if(s_flag == true && e_flag == true && write == false)
    			b_range = true;
    		if((s_flag == true && e_flag == false) || (s_flag == false && e_flag == true))
    		{
    			System.out.println("Please provide both start byte and end byte values");
    			System.out.println("Example: Client 127.0.0.1 hello.txt -s 1 -e 5...");
    			return;
    		}
    	}
    	else
    	{
    		if(args.length > 9)
    			System.out.println("Too many arguments.");
    		else if(args.length < 2)
    			System.out.println("Too few arguments.");   
    		
    		System.out.println("Usage: Client [Port #] <server-name> [-w] <file-name> [-s StartBlock] [-e LastBlock]");
    		return;
    	}
    	Client client = new Client(host, port, filename, start, end);
    }
}
