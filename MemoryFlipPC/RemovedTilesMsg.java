package MemoryFlipPC;

/**
 * Message indicating two tiles matched and have been removed.
 */
public class RemovedTilesMsg extends BoardMessage {
	public int x1, y1, x2, y2;

	/**
	 * Constructor including the position of the two removed tiles.
	 *
	 * @param x1 the first X-coordinate
	 * @param y1 the first Y-coordinate
	 * @param x2 the second X-coordinate
	 * @param y2 the second Y-coordinate
	 */
	public RemovedTilesMsg (int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
}
