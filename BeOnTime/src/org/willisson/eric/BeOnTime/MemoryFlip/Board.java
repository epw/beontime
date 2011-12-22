package org.willisson.eric.BeOnTime.MemoryFlip;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import java.util.Observer;
import java.util.Observable;

import java.io.Serializable;

/**
 * This class implements the board of Tiles, which are normally
 * obscured, but can be examined. It also tracks when all tiles are
 * uncovered, and the game is won.
 */
public class Board extends Observable implements Serializable {
	public final int width, height;
	private final int numFaces;

	private Tile[][] tiles;

	private int remainingTiles;

	private int flippedX, flippedY;

	private boolean won;

	/**
	 * Default constructor. Initializes random tiles. Assumes
	 * default size of 8x8. Uses 32 different tile faces.
	 */
	public Board () {
		width = 8;
		height = 8;
		numFaces = 32;
		flippedX = -1;
		flippedY = -1;

		won = false;

		tiles = new Tile[8][8];
		fillBoard ();
	}

	/**
	 * Constructor which initializes random tiles. Uses 32
	 * different tile faces.
	 *
	 * @param width the number of tiles wide to make the board
	 * @param height the number of tiles tall to make the board
	 */
	public Board (int width, int height) {
		this.width = width;
		this.height = height;
		numFaces = width * height / 2;
		flippedX = -1;
		flippedY = -1;

		won = false;

		tiles = new Tile[this.width][this.height];
		fillBoard ();
	}

	/**
	 * Constructor which initializes random tiles.
	 *
	 * @param width the number of tiles wide to make the board
	 * @param height the number of tiles tall to make the board
	 * @param numFaces the number of different faces to generate
	 */
	public Board (int width, int height, int numFaces) {
		this.width = width;
		this.height = height;
		this.numFaces = numFaces;
		flippedX = -1;
		flippedY = -1;

		won = false;

		tiles = new Tile[this.width][this.height];
		fillBoard ();
	}

	/**
	 * Test constructor which accepts tiles
	 *
	 * @param width the number of tiles wide to make the board
	 * @param height the number of tiles tall to make the board
	 * @param numFaces the number of different faces to generate
	 * @param faces the list of faces to be used.
	 */
	public Board (int width, int height, int numFaces,
		      List<Integer> faces) {
		this.width = width;
		this.height = height;
		this.numFaces = numFaces;
		flippedX = -1;
		flippedY = -1;

		won = false;

		tiles = new Tile[this.width][this.height];

		Iterator<Integer> it = faces.iterator ();

		remainingTiles = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = new Tile (it.next());
				remainingTiles++;
			}
		}
	}

	private void clearFlipped () {
		if (flippedX >= 0) {
			tiles[flippedX][flippedY].flipDown ();
		}
		flippedX = -1;
		flippedY = -1;
	}

	/**
	 * Fill board array with tiles
	 */
	private void fillBoard () {
		ArrayList<Integer> faces = new ArrayList<Integer>(numFaces * 2);

		for (int i = 0; i < numFaces*2; i++) {
			if (i >= numFaces) {
				faces.add (i - numFaces);
			} else {
				faces.add (i);
			}
		}
		
		Collections.shuffle (faces);

		Iterator<Integer> it = faces.iterator ();

		remainingTiles = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = new Tile (it.next());
				remainingTiles++;
			}
		}
	}

	/**
	 * Get face of tile, or FACEDOWN, at position.
	 *
	 * @param x the x-coordinate of the tile
	 * @param y the y-coordinate of the tile
	 * @return the face of the tile.
	 */
	public int getFaceAt (int x, int y) {
		return tiles[x][y].getFace();
	}

	/**
	 * Using notifyObservers to send messages, so need to override it
	 * to automatically call setChanged, then send out the notifications.
	 *
	 * @param arg any object
	 */
	@Override
	public void notifyObservers (Object arg) {
		setChanged ();
		super.notifyObservers (arg);
	}

	/**
	 * Flip up the tile at the specified position. If another
	 * tile is flipped, compare their faces, and remove them if
	 * they are equal, or flip them both back face down if they
	 * are not.
	 *
	 * @param x the x-coordinate of the tile
	 * @param y the y-coordinate of the tile
	 * @return the value of the tile flipped
	 */
	public int flipTileAt (int x, int y) {
		int face;

		tiles[x][y].flipUp ();
		face = tiles[x][y].getFace();

		if (face == Tile.NONEFACE) {
			return face;
		}

		notifyObservers (new UncoveredTileMsg (x, y, face));

		if (flippedX >= 0) {
			if (tiles[x][y].equals (tiles[flippedX][flippedY])
			    && (x != flippedX || y != flippedY)) {
				tiles[x][y] = Tile.NONE;
				tiles[flippedX][flippedY] = Tile.NONE;
				notifyObservers (new RemovedTilesMsg(x, y,
								     flippedX,
								     flippedY));
				remainingTiles -= 2;
				flippedX = -1;
				flippedY = -1;
				if (remainingTiles == 0) {
					win ();
				}
			} else {
				tiles[x][y].flipDown ();
				notifyObservers (new CoveredTileMsg
						 (x, y));
				notifyObservers (new CoveredTileMsg
						 (flippedX, flippedY));
				clearFlipped ();
			}
		} else {
			flippedX = x;
			flippedY = y;
		}
		return face;
	}

	/**
	 * Win the game.
	 */
	private void win () {
		won = true;
		notifyObservers (new WonMsg ());
	}

	/**
	 * Check whether game has been won.
	 *
	 * @return true if game has been won, false otherwise
	 */
	public boolean hasWon () {
		return won;
	}

	/**
	 * Get the number of tiles remaining on the board.
	 *
	 * @return the number of tiles remaining on the board
	 */

	public int getRemaining () {
		return remainingTiles;
	}

	/**
	 * The X-coordinate of the currently flipped tile, if any.
	 *
	 * @return X-coordinate or -1 if no tile is flipped.
	 */
	public int getFlippedX () {
		return flippedX;
	}

	/**
	 * The Y-coordinate of the currently flipped tile, if any.
	 *
	 * @return Y-coordinate or -1 if no tile is flipped.
	 */
	public int getFlippedY () {
		return flippedY;
	}
}
