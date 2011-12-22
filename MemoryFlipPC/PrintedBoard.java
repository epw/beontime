package MemoryFlipPC;

import java.io.*;
import java.util.Observer;
import java.util.Observable;
import java.util.List;
import java.util.LinkedList;

/**
 * This class implements a command-line view of Memory Flip.
 * It displays the current state of the board.
 */
public class PrintedBoard implements Observer {
	public Board board;

	private LinkedList<String> messages;

	/**
	 * Default constructor. Makes an 8x8 board.
	 */
	public PrintedBoard () {
		board = new Board ();

		init ();
	}

	/**
	 * Constructor which sets size of board
	 *
	 * @param width the number of tiles wide to make the board
	 * @param height the number of tiles tall to make the board
	 */
	public PrintedBoard (int width, int height) {
		board = new Board (width, height, width * height / 2);

		init ();
	}

	/**
	 * Test constructor which accepts tiles
	 *
	 * @param width the number of tiles wide to make the board
	 * @param height the number of tiles tall to make the board
	 * @param faces the list of faces to be used.
	 */
	public PrintedBoard (int width, int height, List<Integer> faces) {
		board = new Board (width, height, faces.size(), faces);

		init ();
	}

	/**
	 * Initialization of the class
	 */
	private void init () {
		board.addObserver (this);
		messages = new LinkedList<String> ();
	}

	/**
	 * Receive an update from board being observed.
	 *
	 * @param observable the object being observed
	 * @param data the updated data
	 */
	@Override
	public void update (Observable observable, Object data) {
		if (data instanceof UncoveredTileMsg) {
			UncoveredTileMsg msg = (UncoveredTileMsg) data;

			messages.push ("Uncovered tile ("
				       + Integer.toString (msg.x) + ", " +
				       Integer.toString (msg.y) + "): " +
				       Integer.toString (msg.face));
		}
	}

	/**
	 * Output and clear message queue.
	 */
	public String getMessages () {
		StringBuilder all_messages = new StringBuilder ();
		while (messages.size() > 0) {
			all_messages.append (messages.pop () + "\n");
		}
		return all_messages.toString ();
	}
			

	/**
	 * Get board
	 *
	 * @return the board
	 */
	public Board getBoard () {
		return board;
	}

	/**
	 * String representation of board
	 */
	public String boardString () {
		StringBuilder bar = new StringBuilder (" -");
		StringBuilder s = new StringBuilder ();

		for (int col = 0; col < board.width; col++) {
			bar.append ("----");
		}
		bar.append ("\n");

		s.append (bar);
		for (int row = 0; row < board.height; row++) {
			s.append (" ");
			for (int col = 0; col < board.width; col++) {
				int face;

				face = board.getFaceAt (col, row);

				s.append ("|");

				if (face == Tile.NONEFACE) {
					s.append ("###");
				} else if (face == Tile.FACEDOWN) {
					s.append ("   ");
				} else {
					if (face < 10) {
						s.append (" "
							  + Integer.toString
							  (face)
							  + " ");
					} else {
						s.append (" " + Integer.toString
							  (face));
					}
				}
			}
			s.append ("|\n");
			s.append (bar);
		}

		return s.toString ();
	}
}
