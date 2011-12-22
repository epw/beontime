package MemoryFlipPC;

/**
 * Message indicating a tile has been uncovered and what its face is.
 */
public class UncoveredTileMsg extends BoardMessage {
	public int face;
	public int x, y;

	/**
	 * Constructor including the position and face of the uncovered
	 * tile.
	 *
	 * @param x the X-coordinate
	 * @param y the Y-coordinate
	 * @param face the face
	 */
	public UncoveredTileMsg (int x, int y, int face) {
		this.x = x;
		this.y = y;
		this.face = face;
	}
}
