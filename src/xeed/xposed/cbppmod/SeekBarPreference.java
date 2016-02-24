package xeed.xposed.cbppmod;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;


public final class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private final Context mContext;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax;
	private int mValue;

	private final String getStr(final String key, final AttributeSet a)
	{
		final int resId = a.getAttributeResourceValue(androidns, "dialogMessage", 0);
		if (resId != 0) return mContext.getResources().getString(resId);
		else return a.getAttributeValue(androidns, "dialogMessage");
	}

	public SeekBarPreference(final Context c, final AttributeSet a)
	{ 
		super(c, a); 
		mContext = c;
		mDialogMessage = getStr("dialogMessage", a);
		mSuffix = a.getAttributeValue(androidns, "text");
		mDefault = a.getAttributeIntValue(androidns, "defaultValue", 300);
		mMax = a.getAttributeIntValue(androidns, "max", 1500) / 10;
		mValue = shouldPersist() ? getPersistedInt(mDefault) : mDefault;
	}

	public SeekBarPreference(final Context c, final AttributeSet attrs, final String dlgMsg, final String suffix)
	{
		super(c, attrs);
		mContext = c;
		mDialogMessage = dlgMsg;
		mSuffix = suffix;
		mDefault = 300;
		mMax = 150;
		mValue = 300;
	}

	@Override 
	protected final View onCreateDialogView()
	{
		mValue = shouldPersist() ? getPersistedInt(mDefault) : mDefault;
		final LinearLayout.LayoutParams params;
		final LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);
		mSplashText = new TextView(mContext);
		mSplashText.setTextColor(0xffaaaaaa);
		mSplashText.setPadding(28, 2, 0, 2);
		if (mDialogMessage != null) mSplashText.setText(mDialogMessage);
		layout.addView(mSplashText);
		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		mValueText.setTextColor(0xffffffff);
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);
		mSeekBar = new SeekBar(mContext);
		mSeekBar.setMax(mMax);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setProgress(mValue / 10);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		return layout;
	}
	
	@Override
	protected final void onSetInitialValue(final boolean restore, final Object def)  
	{
		super.onSetInitialValue(restore, def);
		if (restore) mValue = shouldPersist() ? getPersistedInt(mDefault) : mDefault;
		else if (shouldPersist()) persistInt(mValue = mDefault);
	}

	@Override
	public final void onProgressChanged(final SeekBar sb, final int value, final boolean user)
	{
		if (user) mValue = value * 10;
		mValueText.setText(value * 10 + mSuffix);
	}

	@Override
	protected final void onDialogClosed(final boolean result)
	{
		if (result && shouldPersist()) persistInt(mValue);
		super.onDialogClosed(result);
	}

	@Override
	public final void onStartTrackingTouch(final SeekBar seek) { }

	@Override
	public final void onStopTrackingTouch(final SeekBar seek) { }
}