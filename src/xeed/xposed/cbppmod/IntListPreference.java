package xeed.xposed.cbppmod;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

public final class IntListPreference extends ListPreference
{
    public IntListPreference(final Context c, final AttributeSet as) { super(c, as); }

    public IntListPreference(final Context c) { super(c); }
    
	@Override
	protected final void onPrepareDialogBuilder(final Builder b)
	{
		super.onPrepareDialogBuilder(b);
		int val;
		try { val = getPersistedInt(0); }
		catch (final Exception ex) { val = 0; }
		b.setSingleChoiceItems(getEntries(), val, new OnClickListener()
		{
			@Override
			public final void onClick(final DialogInterface di, final int pos)
			{
				if (shouldPersist()) persistInt(pos);
				di.dismiss();
			}
		});
	}
	
	@Override
	protected final void onSetInitialValue(final boolean restore, final Object def) { }
	
	@Override
	protected final Object onGetDefaultValue(final TypedArray ta, final int i)
	{
		return ta.getInt(i, 0);
	}
}
