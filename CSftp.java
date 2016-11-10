
import java.lang.System;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*; 
import java.util.*;



//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
	private static final int MAX_LEN = 255;
	private static Socket conn;
	private static boolean open = false;
	private static BufferedReader input;
	private static BufferedWriter output;


	public static void connect(String host, int port) {
		try {

			conn = new Socket();
			conn.connect(new InetSocketAddress(InetAddress.getByName(host), port), 30000);   // 30 second timeout
			
			input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			
			open = true;
			System.out.println("The connection to " + host + " on port " + port + " was successful.");

		} catch (UnknownHostException e) {
			System.out.println("802 Invalid argument.");
		} catch (IOException e) {
			System.out.println("820 Control connection  to " + host + " on port " + port +  " failed to open.");
		}
	}


	//TODO: make it read and print all lines in the InputStream
	private static String read() throws IOException {

		String line = input.readLine();
		System.out.println("<-- " + line);
		String response = line.substring(0,3);
		return response;
	}

	private static void write(String line) throws IOException  {
		try {
			output.write(line + "\n");
			output.flush();

		} catch (IOException e) {
			System.out.println("825 Control connection I/O error, closing control connection.");
			conn.close();
		}
	}

	public static void sendUser(String user) throws IOException{

		try {

			String line = read();
			write("USER " + user);
			System.out.println("--> " + "USER " + user);

			line = read();
			if (line.equals("331")) {

				System.out.print("Enter password: ");
				Scanner getPass = new Scanner (System.in);
				String pass = getPass.nextLine();
				write("PASS " + pass);
				System.out.println("--> " + "PASS " + pass);
				line = read();
			}


		} catch (IOException e) {
			System.out.println("825 Control connection I/O error, closing control connection.");
			conn.close();
		}
	}

	public static void close() {
		try {
			write("QUIT");
			System.out.println("--> " + "QUIT");
			read();
			open = false;
		} catch (IOException e) {
			System.out.println("825 Control connection I/O error, closing control connection.");
		}
	}

	public static void quit() {
		if (open) { close(); }
		System.exit(0);
	}

	public static void get(String remote) {
		System.out.println(remote);
	}

	public static void put(String local) {
		System.out.println(local);
	}

	public static void cd(String directory) {
		try {

			write("CWD " + directory);
			System.out.println("--> " + "CWD " + directory);
			read();

		} catch (IOException e) {
			System.out.println("825 Control connection I/O error, closing control connection.");
		}

	}

	public static void dir() {
		System.out.println("I'm dirring");
	}



	public static ArrayList<String> parseString(String str) {

		ArrayList<String> toReturn = new ArrayList<String>();

		for (int l = 0; l < 255; l++) {
			toReturn.add("");
		}

		int index = 0;

		for (int i = 0; i < str.length(); i++) {
			String ch = str.substring(i,i+1);
			if (!ch.equals(" ") && !ch.equals("\t")) {
				toReturn.set(index, toReturn.get(index).concat(ch));
			} else {
				index++;
			}
		}

		return toReturn;
	}


	public static void main(String [] args)
	{
		byte cmdString[] = new byte[MAX_LEN];

		try {

			for (int len = 1; len > 0;) {

				System.out.print("csftp> ");
				len = System.in.read(cmdString);

				if (len <= 0) 
					break;

				String cmd = "";

				for(int i = 0; i < len-1; i++)
				{
					cmd += (char)cmdString[i];
				}

				ArrayList<String >parsed = parseString(cmd);

				String first = parsed.get(0);

				if (first.isEmpty()) {
					continue;
				} else if (first.substring(0,1).equals("#")) {
					continue;
				}

				if (open == false) {
					if (first.equals("quit")) {
						quit();
						continue;
					}
					if (!first.equals("open")) {
						System.out.println("803 Supply command not expected at this time.");
						continue;
					}
				} else {
					if (first.equals("open")) {
						System.out.println("803 Supply command not expected at this time.");
						continue;
					}
				}

				boolean isValid = false;

				if (first.equals("open")) {
					isValid = true;

					if (parsed.get(1).isEmpty() || !parsed.get(3).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					} 
					String host = parsed.get(1);
					if (parsed.get(2).isEmpty()) {
						connect(host,21);
						continue;
					}
					try { 
						int port = Integer.parseInt(parsed.get(2)); 
						connect(host, port);
						continue;

					} catch(NumberFormatException e) { 
						System.out.println("802 Invalid argument.");
					}

				}

				if (first.equals("user")) {
					isValid = true;
					if (parsed.get(1).isEmpty() || !parsed.get(2).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					String user = parsed.get(1);
					sendUser(user);
					continue;
				}

				if (first.equals("close")) {
					isValid = true;
					if (!parsed.get(1).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					close();
					open = false;
					continue;
				}

				if (first.equals("quit")) {
					isValid = true;
					if (!parsed.get(1).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					quit();
				}

				if (first.equals("get")) {
					isValid = true;
					if (parsed.get(1).isEmpty() || !parsed.get(2).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					String remote = parsed.get(1);
					get(remote);
					continue;
				}

				if (first.equals("put")) {
					isValid = true;
					if (parsed.get(1).isEmpty() || !parsed.get(2).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					String local = parsed.get(1);
					put(local);
					continue;
				}

				if (first.equals("cd")) {
					isValid = true;
					if (parsed.get(1).isEmpty() || !parsed.get(2).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					String directory = parsed.get(1);
					cd(directory);
					continue;
				}

				if (first.equals("dir")) {
					isValid = true;
					if (!parsed.get(1).isEmpty()) {
						System.out.println("801 Incorrect number of arguments.");
						continue;
					}
					dir();
					continue;
				}

				if (isValid == false) {
					System.out.println("800 Invalid command.");
					continue;
				} 
			}

		} catch (IOException exception) {
			System.err.println("898 Input error while reading commands, terminating.");
		} 

	}
}
