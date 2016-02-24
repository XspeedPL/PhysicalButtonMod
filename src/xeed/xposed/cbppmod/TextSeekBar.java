package xeed.xposed.cbppmod;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

public final class TextSeekBar extends SeekBar
{
	private final String mSuffix;
	private final Paint mPaint;
	private final int mMult;
	
	@SuppressWarnings("deprecation")
	public TextSeekBar(Context c, AttributeSet as)
	{
		super(c, as);
		final TextView tv = new TextView(c);
		final TypedArray ta = c.getTheme().obtainStyledAttributes(as, R.styleable.TextSeekBar, 0, 0);
		try
		{
			mMult = ta.getInteger(R.styleable.TextSeekBar_textValueMult, 1);
			mSuffix = ta.getString(R.styleable.TextSeekBar_textSuffix);
		}
		finally { ta.recycle(); }
		tv.setTextAppearance(c, android.R.style.TextAppearance_Medium);
		mPaint = new Paint();
		mPaint.setColor(tv.getCurrentTextColor());
		mPaint.setTextSize(tv.getTextSize());
		mPaint.setTypeface(tv.getTypeface());
		mPaint.setTextAlign(Align.CENTER);
	}
	
	@Override
	public final void onDraw(final Canvas c)
	{
		super.onDraw(c);
		c.drawText(String.valueOf(mMult * getProgress()) + mSuffix, getWidth() / 2, (int)(c.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint);
	}
}
