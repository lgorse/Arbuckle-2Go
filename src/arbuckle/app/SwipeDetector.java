package arbuckle.app;

import android.view.MotionEvent;
import android.view.View;

/*
 * Detects a user's finger swipe when he goes from order view to details view
 */
public class SwipeDetector implements View.OnTouchListener{

	public static enum Action{
		LR,
		RL,
		TB,
		BT,
		none
	}

	private static final int HORIZONTAL_MIN_DISTANCE = 10;
	private static final int VERTICAL_MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;
	private Action mSwipeDetected = Action.none;
	private Boolean rowState;

	public boolean swipeDetected(){
		return mSwipeDetected != Action.none;
	}

	public Action getAction(){
		return mSwipeDetected;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:{
			downX = event.getX();
			downY = event.getY();
			mSwipeDetected = Action.none;

		}
		case MotionEvent.ACTION_UP:{
			upX = event.getX();
			upY = event.getY();

			float deltaX = downX - upX;
			float deltaY = downY - upY;

			//Horizontal Swipe detection
			if (Math.abs(deltaX) > HORIZONTAL_MIN_DISTANCE){
				if (deltaX < 0){
					
					mSwipeDetected = Action.LR;

				}
				if (deltaX > 0){
					mSwipeDetected = Action.RL;

				}
				/*}else if (Math.abs(deltaY) >VERTICAL_MIN_DISTANCE){
				if (deltaY < 0){
					mSwipeDetected = Action.TB;
					return false;
				}
				if (deltaY > 0){
					mSwipeDetected = Action.BT;
					return false;
				}*/
			}

		}
		}
		return false;
	}

	


}