package xeed.xposed.cbppmod.prf;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public final class InfoDialogPreference extends DialogPreference
{
	public InfoDialogPreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setNegativeButtonText(null);
	}
}
