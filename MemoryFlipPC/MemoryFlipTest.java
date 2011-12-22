package MemoryFlipPC;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class MemoryFlipTest {

	/**
	 * Ensure a new tile is face down
	 */
	@Test
	public void testNewTileFaceDown () {
		assertTrue (Tile.FACEDOWN == new Tile(8).getFace());
	}

	/**
	 * Flip a tile face up
	 */
	@Test
	public void testFaceUpTile () {
		Tile t = new Tile (8);
		t.flipUp ();
		assertEquals (8, t.getFace());
	}

	/**
	 * Test that two tiles can be equal when flipped up
	 */
	@Test
	public void testEqualTiles () {
		Tile t1 = new Tile (1);
		Tile t2 = new Tile (1);
		t1.flipUp ();
		t2.flipUp ();

		assertEquals (t1, t2);
	}

	/**
	 * Test that a tile at a position in the board starts face down
	 */
	@Test
	public void testBoardFaceDown () {
		Board b = new Board ();

		assertEquals (Tile.FACEDOWN, b.getFaceAt (0, 0));
	}

	/**
	 * Test that flipping a tile results in one flipped-up tile.
	 */
	@Test
	public void testFlipOneTile () {
		Board b = new Board (2, 2, 2);

		b.flipTileAt (0, 0);

		assertFalse (Tile.FACEDOWN == b.getFaceAt (0, 0));
	}

	/**
	 * Test that flipping a tile with a specific layout works
	 */
	@Test
	public void testFlipOneSpecifiedTile () {
		ArrayList<Integer> faces = new ArrayList<Integer>(4);

		faces.add (0);
		faces.add (1);
		faces.add (1);
		faces.add (0);

		Board b = new Board (2, 2, 2, faces);

		assertEquals (0, b.flipTileAt (0, 0));
	}

	/**
	 * Test that flipping two different tiles with a specific layout
	 * flips them both back down
	 */
	@Test
	public void testFlipTwoDifferentTiles () {
		ArrayList<Integer> faces = new ArrayList<Integer>(4);

		faces.add (0);
		faces.add (1);
		faces.add (1);
		faces.add (0);

		Board b = new Board (2, 2, 2, faces);

		assertEquals (0, b.flipTileAt (0, 0));
		assertEquals (1, b.flipTileAt (0, 1));
		assertEquals (Tile.FACEDOWN, b.getFaceAt (0, 0));
		assertEquals (Tile.FACEDOWN, b.getFaceAt (0, 1));
	}


	/**
	 * Test that flipping two of the same tile with a specific layout
	 * removes them.
	 */
	@Test
	public void testFlipTwoSameTile () {
		ArrayList<Integer> faces = new ArrayList<Integer>(4);

		faces.add (0);
		faces.add (1);
		faces.add (1);
		faces.add (0);

		Board b = new Board (2, 2, 2, faces);

		assertEquals (0, b.flipTileAt (0, 0));
		assertEquals (0, b.flipTileAt (1, 1));
		assertEquals (Tile.NONEFACE, b.getFaceAt (0, 0));
		assertEquals (Tile.NONEFACE, b.getFaceAt (1, 1));
	}

	/**
	 * Test winning.
	 */
	@Test
	public void testWin () {
		ArrayList<Integer> faces = new ArrayList<Integer>(4);

		faces.add (0);
		faces.add (1);
		faces.add (1);
		faces.add (0);

		Board b = new Board (2, 2, 2, faces);

		assertEquals (4, b.getRemaining ());

		b.flipTileAt (1, 0);
		b.flipTileAt (0, 1);

		assertEquals (2, b.getRemaining ());

		b.flipTileAt (0, 0);
		assertEquals (0, b.getFaceAt (0, 0));
		b.flipTileAt (1, 1);

		assertEquals (0, b.getRemaining ());

		assertTrue (b.hasWon());
	}

	/**
	 * Test creating printed board adding self as observer.
	 */
	@Test
	public void testPrintedBoardObserver () {
		PrintedBoard pb = new PrintedBoard ();

		assertEquals (1, pb.getBoard().countObservers());
	}
}
