package org.willisson.eric.BeOnTime.MemoryFlip;

/**
 * Message indicating that a tile has been covered.
 */
public class CoveredTileMsg extends BoardMessage {
	public int x, y;

	/**
	 * Constructor including the position of the removed tile.
	 *
	 * @param x the first X-coordinate
	 * @param y the first Y-coordinate
	 */
	public CoveredTileMsg (int x, int y) {
		this.x = x;
		this.y = y;
	}
}
