package xeed.xposed.cbppmod.viw;

import com.mobeta.android.dslv.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.util.AttributeSet;
import android.view.MotionEvent;

@SuppressLint("ClickableViewAccessibility")
public final class CustomListView extends DragSortListView implements GestureDetector.OnGestureListener
{
	private final GestureDetectorCompat gd_i, gd_it;
	
	public CustomListView(final Context c, final AttributeSet as)
	{
		super(c, as);
        gd_i = new GestureDetectorCompat(c, this);
        gd_i.setIsLongpressEnabled(false);
        gd_it = new GestureDetectorCompat(c, this);
        gd_it.setIsLongpressEnabled(false);
	}

	@Override
	public final boolean onDown(final MotionEvent e) { return false; }

	@Override
	public final void onShowPress(final MotionEvent e) { }

	@Override
	public final boolean onSingleTapUp(final MotionEvent e)
	{
		final int pos = pointToPosition((int)e.getX(), (int)e.getY());
		if (pos != INVALID_POSITION && pos >= getHeaderViewsCount() && pos < (getCount() - getFooterViewsCount()))
		{
			performItemClick(getChildAt(pos), pos, getItemIdAtPosition(pos));
			return true;
		}
		return false;
	}

	@Override
	public final boolean onTouchEvent(final MotionEvent e)
	{
        return gd_i.onTouchEvent(e) || super.onTouchEvent(e);
	}
	
	@Override
	public final boolean onInterceptTouchEvent(final MotionEvent e)
	{
	    return gd_it.onTouchEvent(e) || super.onInterceptTouchEvent(e);
	}
	
	@Override
	public final boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) { return false; }

	@Override
	public final void onLongPress(final MotionEvent e) { }

	@Override
	public final boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) { return false; }
}
