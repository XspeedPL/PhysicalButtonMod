package xeed.xposed.cbppmod;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;


public final class FlagListPreference extends DialogPreference implements OnMultiChoiceClickListener
{
	private CharSequence[] mEntries;
	private int mNewValue;
	
	public FlagListPreference(final Context c, final AttributeSet as)
	{
		super(c, as);
		final TypedArray ta = c.obtainStyledAttributes(as, new int[] { android.R.attr.entries });
		mEntries = ta.getTextArray(0);
		ta.recycle();
	}

	public final CharSequence[] getEntries() { return mEntries; }
	public final void setEntries(final CharSequence[] cs) { mEntries = cs; }
	
	@Override
	public final void onClick(final DialogInterface di, final int pos, final boolean check)
	{
		final int flag = 1 << pos;
		mNewValue = check ? (mNewValue |= flag) : (mNewValue &= ~flag);
	}
	
	@Override
	public final void onClick(final DialogInterface di, final int pos)
	{
		if (pos == DialogInterface.BUTTON_POSITIVE && shouldPersist()) persistInt(mNewValue);
	}
	
	@Override
	protected final void onPrepareDialogBuilder(final Builder b)
	{
		super.onPrepareDialogBuilder(b);
		try { mNewValue = getPersistedInt(0); }
		catch (final Exception ex) { mNewValue = 0; }
		b.setMultiChoiceItems(mEntries, getSelectedItems(), this);
	}

	private final boolean[] getSelectedItems()
	{
		final boolean[] res = new boolean[mEntries.length];
		int v = mNewValue;
		for (int i = 0; i < res.length; ++i)
		{
			res[i] = (v & 1) == 1;
			v >>= 1;
		}
		return res;
	}
	
	@Override
	protected final void onSetInitialValue(final boolean restore, final Object def) { }
	
	@Override
	protected final Object onGetDefaultValue(final TypedArray ta, final int i)
	{
		return ta.getInt(i, 0);
	}
}