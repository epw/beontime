package MemoryFlipPC;

import java.io.*;

public class MemoryFlipMain {
	public static void main (String[] args) throws IOException {
		PrintedBoard pb = new PrintedBoard (4, 4);

		BufferedReader stdin = new BufferedReader
			(new InputStreamReader (System.in));
		String command;

		while (true) {
			System.out.print (pb.boardString ());
			System.out.print ("\n> ");
			System.out.flush ();
			command = stdin.readLine ();
			if (command == null || command.equals ("quit")) {
				System.out.println ("");
				break;
			} else {
				String[] coords = command.split (" ");
				int x, y;
				try {
					x = Integer.parseInt (coords[0]);
					y = Integer.parseInt (coords[1]);
					pb.board.flipTileAt (x, y);
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				} catch (java.lang.NumberFormatException e) {
				}
			}
			String messages = pb.getMessages ();
			if (!messages.equals ("")) {
				System.out.println ("Messages:");
				System.out.println (messages);
			}
			if (pb.board.hasWon()) {
				System.out.println ("You win!");
				break;
			}
		}
	}
}
