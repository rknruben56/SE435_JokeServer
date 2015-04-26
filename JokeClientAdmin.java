/*--------------------------------------------------------
1. Ruben Rodriguez / Date: 4/19/2015

2. Java build 1.8.0_40

3.To compile:

> javac JokeClientAdmin.java

4. instructions to run this program:

In shell window, navigate to where the compiled file is and enter:

> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at
140.192.1.22 then you would type:

> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the server and client.

 a. JokeServer.java
 b. JokeClientAdmin.java

6. Notes: This code was mostly taken from JokeClient and InetClient.

----------------------------------------------------------*/
import java.io.*; //Get IO libraries
import java.net.*; //Get Java connection libraries

/**
 * JokeServer admin that changes the state of the server from joke, proverb, or
 * maintenance mode
 * 
 * @author rodri_000
 *
 */
public class JokeClientAdmin {
	public static void main(String[] args) {
		String serverName;

		// establish server
		if (args.length < 1)
			serverName = "localhost";
		else
			serverName = args[0];

		System.out.println("Ruben Rodriguez's Joke Admin Client, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: 4570");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {
			String input = "";
			do {
				// Get user input about which mode to change to
				System.out
						.println("Enter the mode you wish to establish the joker server in: joke-mode, proverb-mode, or maintenance-mode or quit to exit");
				System.out.flush();
				input = in.readLine();
				if (input.indexOf("quit") < 0)
					talkToServer(input, serverName);

			} while (input.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
			;
		} catch (IOException x) {
			x.printStackTrace();
		}

	}

	/**
	 * Method that establishes the connection with the server and handles the IO
	 * 
	 * @param input
	 * @param serverName
	 */
	private static void talkToServer(String input, String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			// establish socket with the admin port
			sock = new Socket(serverName, 4570);
			// Create filter I/O streams for the socket
			fromServer = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());

			// print the mode input
			toServer.println(input);
			toServer.flush();

			// get text from server
			for (int i = 0; i <= 1; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}
			// close the connection
			sock.close();
		} catch (IOException e) {
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}
}
