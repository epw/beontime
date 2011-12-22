package MemoryFlipPC;

import java.util.Random;

/**
 * This class implements an individual tile. A tile can only be seen
 * when it has been clicked on, and knows how to compare itself to
 * another tile.
 */
public class Tile {
	private boolean flipped = false;

	private final int face;

	public static int FACEDOWN = -1;
	public static int NONEFACE = -2;
	public static Tile NONE = new Tile (NONEFACE);

	/**
	 * Default constructor. Leaves face set to 0
	 */
	public Tile () {
		face = 0;
	}

	/**
	 * Create a tile given face.
	 *
	 * @param face face value
	 */
	public Tile (int face) {
		this.face = face;
	}

	/**
	 * If the tile is face-down (flipped is false), indicate so.
	 * Otherwise, return the face of the tile
	 *
	 * @return the face of the tile or FACEDOWN
	 */
	public int getFace () {
		if (flipped || this == Tile.NONE) {
			return face;
		}
		return FACEDOWN;
	}

	/**
	 * Reveal tile, making face visible
	 *
	 * @return the tile's face
	 */
	public int flipUp () {
		flipped = true;
		return getFace();
	}

	/**
	 * Cover tile, obscuring face
	 */
	public void flipDown () {
		flipped = false;
	}

	/**
	 * Two tiles are equal if they have the same face.
	 *
	 * @param object the object to compare to this object.
	 * @return true if the specified object is equal to this
	 *   tile, false otherwise.
	 */
	public boolean equals (Object object) {
		if (object instanceof Tile) {
			Tile t = (Tile) object;
			return getFace() == t.getFace();
		}
		return false;
	}
}
