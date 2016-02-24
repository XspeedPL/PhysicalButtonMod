package xeed.xposed.cbppmod;

import com.mobeta.android.dslv.DragSortListView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

@SuppressLint("ClickableViewAccessibility")
public final class CustomListView extends DragSortListView implements OnGestureListener
{
	private final GestureDetector gd_it, gd_i;
	
	public CustomListView(final Context c, final AttributeSet as)
	{
		super(c, as);
		gd_it = new GestureDetector(c, this);
		gd_i = new GestureDetector(c, this);
		gd_it.setIsLongpressEnabled(false);
		gd_i.setIsLongpressEnabled(false);
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
	public final boolean onDown(final MotionEvent e) { return false; }

	@Override
	public final void onShowPress(final MotionEvent e) { }

	@Override
	public final boolean onSingleTapUp(final MotionEvent e)
	{
		final int touchPos = pointToPosition((int)e.getX(), (int)e.getY());
		if (touchPos != INVALID_POSITION && touchPos >= getHeaderViewsCount() && touchPos < (getCount() - getFooterViewsCount()))
		{
			final int pos = touchPos;
			performItemClick(getChildAt(pos), pos, getItemIdAtPosition(pos));
			return true;
		}
		return false;
	}

	@Override
	public final boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) { return false; }

	@Override
	public final void onLongPress(final MotionEvent e) { }

	@Override
	public final boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) { return false; }
}
