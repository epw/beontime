package org.willisson.eric.BeOnTime;

import android.app.Activity;
import android.os.Bundle;
import android.content.res.Resources;
import android.content.Intent;
import android.util.Log;
import android.content.pm.ActivityInfo;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Color;

import java.util.Observer;
import java.util.Observable;
import java.util.ArrayList;
import android.os.Handler;

import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.Serializable;

import android.os.AsyncTask;

import android.os.CountDownTimer;
import java.util.concurrent.TimeUnit;
import android.text.format.Time;

import org.willisson.eric.BeOnTime.MemoryFlip.*;

public class MemoryFlipActivity extends Activity implements Observer
{
	private final int size = 6;
	private int rows;
	private int cols;

	/** The root of the GUI layout */
	private LinearLayout root;

	/** An array of the buttons representing Tiles */
	private Button[][] buttons;

	/** The countdown timer and view */
	private CountDownTimer timer;
	private EditText timerView;

	Resources res;

	private boolean stopped = false;

	private static final String APP_STATE = "org.willisson.eric.BeOnTime.state";

	/** Serializable object to save the state */
	private static class AppState implements Serializable {
		/** The MemoryFlip board */
		private Board board;

		/** The time left */
		long timeLeft;

		/** The moment when the activity is paused. */
		long moment;

		public AppState (Resources res) {
			board = null;
			timeLeft = res.getInteger
				(R.integer.max_starting_seconds) * 1000;
		}

		public Board getBoard () {
			return board;
		}
		public void setBoard (Board board) {
			this.board = board;
		}

		public long getTimeLeft () {
			return timeLeft;
		}
		public void setTimeLeft (long timeLeft) {
			this.timeLeft = timeLeft;
		}

		public long getMoment () {
			return moment;
		}
		public void setMoment (long moment) {
			this.moment = moment;
		}
	}

	private AppState applicationState;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources res = getResources ();

		if (savedInstanceState == null) {
			applicationState = new AppState (res);
		} else {
			applicationState = (AppState)
				savedInstanceState.getSerializable
				(APP_STATE);
		}

		long timeRemaining = getIntent().getExtras().getLong
			("org.willisson.eric.BeOnTime.TimeRemaining");
		if (timeRemaining != 0
		    && timeRemaining < applicationState.getTimeLeft()) {
			applicationState.setTimeLeft (timeRemaining);
		}

		setRequestedOrientation
			(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		init ();
	}

	/**
	 * @see android.app.Activity#onSaveInstanceState (android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState (outState);

		Time now = new Time();

		now.setToNow ();
		applicationState.setMoment (now.toMillis(false));
		timer.cancel ();

		outState.putSerializable (APP_STATE, applicationState);
	}

	/**
	 * @see android.app.Activity#onRestoreInstanceState (android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState (Bundle inState) {
		super.onRestoreInstanceState (inState);

		res = getResources ();

		Time now = new Time ();

		now.setToNow ();
		applicationState = (AppState) inState.getSerializable
			(APP_STATE);

		init ();

		timer = new GameTimer (applicationState.getTimeLeft()
				       - (now.toMillis(false)
					  - applicationState.getMoment()));

	}

	@Override
	protected void onResume () {
		super.onResume ();
		Time now = new Time ();

		now.setToNow ();
		timer = new GameTimer (applicationState.getTimeLeft()
				       - (now.toMillis(false)
					  - applicationState.getMoment()));
	}

	
	private class GameTimer extends CountDownTimer {
		public GameTimer (long ms) {
			super (ms, 1000);
		}

		@Override public void onTick (long ms) {
			if (res == null) {
				res = getResources ();
			}
			applicationState.setTimeLeft (ms);
			timerView.setText
				(String.format
				 (res.getString(R.string.time_remaining_fmt),
				  TimeUnit.MILLISECONDS.toMinutes(ms),
				  TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))));
		}
		@Override public void onFinish () {
			onTick (0);
			lose ();
		}
	}


	/**
	 * Actually initialize activity, allowing for in-app restarts
	 */
	private void init () {
		rows = size;
		cols = size;

		if (applicationState.getBoard () == null) {
			applicationState.setBoard (new Board (cols, rows));
		}
		applicationState.getBoard().deleteObservers ();
		applicationState.getBoard().addObserver (this);

		LinearLayout.LayoutParams containerParams
			= new LinearLayout.LayoutParams (
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				0.0F);
		LinearLayout.LayoutParams rowParams
			= new LinearLayout.LayoutParams (
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				0.0F);
		LinearLayout.LayoutParams tileParams
			= new LinearLayout.LayoutParams (
				ViewGroup.LayoutParams.FILL_PARENT,
				60,
				1.0F);
		LinearLayout.LayoutParams widgetParams
			= new LinearLayout.LayoutParams (
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				1.0F);

		root = new LinearLayout (this);
		root.setOrientation (LinearLayout.VERTICAL);
		root.setBackgroundColor (Color.LTGRAY);
		root.setLayoutParams (containerParams);

		buttons = new Button[rows][cols];

		LinearLayout ll;

		for (int row = 0; row < rows; row++) {
			ll = new LinearLayout (this);
			ll.setOrientation (LinearLayout.HORIZONTAL);
			ll.setBackgroundColor (Color.GRAY);
			ll.setLayoutParams (rowParams);
			root.addView (ll);

			for (int col = 0; col < cols; col++) {
				final Button b = new Button (this);
				final int x = col;
				final int y = row;
				int face = applicationState.getBoard().getFaceAt
					(x, y);
				if (face == Tile.FACEDOWN) {
					clearButton (b);
				} else if (face == Tile.NONEFACE) {
					hideButton (b);
				} else {
					b.setText (Integer.toString(face));
				}
				b.setLayoutParams (tileParams);
				b.setOnClickListener (
					new Button.OnClickListener () {
						@Override
						public void onClick (View arg0){
							applicationState
								.getBoard()
								.flipTileAt
								(x, y);
						}
					}
					);
				ll.addView (b);
				buttons[row][col] = b;
			}
		}

		ll = new LinearLayout (this);
		ll.setOrientation (LinearLayout.HORIZONTAL);
		ll.setBackgroundColor (Color.GRAY);
		ll.setLayoutParams (rowParams);
		root.addView (ll);

		timerView = new EditText (this);
		timerView.setFocusable (false);
		timerView.setLayoutParams (widgetParams);
		ll.addView (timerView);

		setContentView (root);

		if (timer != null) {
			timer.cancel ();
		}
		timer = new GameTimer (applicationState.getTimeLeft()).start ();
	}

	/**
	 * Clear button text without making it take up less vertical space.
	 *
	 * @param b The button to clear
	 */
	private void clearButton (Button b) {
		b.setTextColor (Color.TRANSPARENT);
	}


	/**
	 * Hide button without making it take up less vertical space.
	 *
	 * @param b The button to hide
	 */
	private void hideButton (Button b) {
		b.setBackgroundColor (Color.GRAY);
	}

	/**
	 * Open a dialog box congratulating the player
	 */
	private void win () {
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setMessage (res.getString (R.string.win))
			.setCancelable (false)
			.setPositiveButton ("New Game",
					    new DialogInterface.OnClickListener () {
						    public void onClick
						    (DialogInterface dialog,
						     int id) {
							    init ();
							    dialog.cancel();
						    }
					    })
			.setNegativeButton ("Quit",
					    new DialogInterface.OnClickListener () {
						    public void onClick
						    (DialogInterface dialog,
						     int id) {
							    MemoryFlipActivity.this.finish();
						    }
					    });
		AlertDialog alert = builder.create ();
		alert.show ();
	}

	/**
	 * Open a dialog box consoling the player
	 */
	private void lose () {
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setMessage (res.getString (R.string.lose_time))
			.setCancelable (false)
			.setPositiveButton ("Quit",
					    new DialogInterface.OnClickListener () {
						    public void onClick
						    (DialogInterface dialog,
						     int id) {
							    MemoryFlipActivity.this.finish ();
						    }
					    });
		AlertDialog alert = builder.create ();
		if (!stopped) {
			alert.show ();
		}
	}

	/**
	 * Called when the board notifies its observers of a change.
	 * Clicking on a button sends a message to the board, which informs
	 * the app of the new appearance.
	 */
	@Override
	public void update (Observable observable, Object data) {
		if (data instanceof UncoveredTileMsg) {
			UncoveredTileMsg msg = (UncoveredTileMsg) data;
			buttons[msg.y][msg.x].setTextColor (Color.BLACK);
			buttons[msg.y][msg.x].setText (Integer.toString
						       (msg.face));
		} else if (data instanceof CoveredTileMsg) {
			CoveredTileMsg msg = (CoveredTileMsg) data;
			new DelayedClear (buttons[msg.y][msg.x]).execute ();
		} else if (data instanceof RemovedTilesMsg) {
			RemovedTilesMsg msg = (RemovedTilesMsg) data;
			clearButton (buttons[msg.y1][msg.x1]);
			hideButton (buttons[msg.y1][msg.x1]);
			clearButton (buttons[msg.y2][msg.x2]);
			hideButton (buttons[msg.y2][msg.x2]);
		} else if (data instanceof WonMsg) {
			win ();
		}
	}

	/**
	 * This class lets an uncovered tile be shown for a few moments
	 * before it is covered again. The class is a thread started from
	 * update ().
	 */
	private class DelayedClear extends AsyncTask<Void, Void, Void> {
		/** The button to be cleared */
		private final Button b;
		
		/**
		 * Constructor to allow button to be passed in.
		 */
		public DelayedClear (Button b) {
			this.b = b;
		}
		
		@Override protected void onPreExecute () {
		}
		@Override protected void onPostExecute (Void result) {
			clearButton (b);
		}
		@Override protected Void doInBackground (Void... params) {
			try {
				Thread.sleep (res.getInteger(R.integer.flip_tile_ms));
			} catch (java.lang.InterruptedException e) {
			}
			return null;
		}
	}
}
