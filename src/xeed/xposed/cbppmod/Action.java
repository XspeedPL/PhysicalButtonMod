package xeed.xposed.cbppmod;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;

final class Action
{
	public static final int ACTION_NONE = -1, ACTION_MEDIA = 0, ACTION_KEY = 1, ACTION_CODED = 2, ACTION_OTHER = 3;
	
	public static final int MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
	public static final int MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
	public static final int MEDIA_PREV = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
	public static final int MEDIA_VOUP = KeyEvent.KEYCODE_VOLUME_UP;
	public static final int MEDIA_VODN = KeyEvent.KEYCODE_VOLUME_DOWN;
	public static final int MEDIA_FORV = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
	public static final int MEDIA_REWD = KeyEvent.KEYCODE_MEDIA_REWIND;
	public static final int[] MEDIA = new int[] { MEDIA_PLAY, MEDIA_NEXT, MEDIA_PREV, MEDIA_VOUP, MEDIA_VODN, MEDIA_FORV, MEDIA_REWD };
	
	public static final int CODED_SRBT = 0, CODED_FLHT = 1;
	
	public int type;
	public int ex_i;
	public String ex_s;
	
	public Action(final int atype, final String extra) { type = atype; ex_s = extra; }
	public Action(final int atype, final int extra) { type = atype; ex_i = extra; }
	
	public Action(final SharedPreferences sp, final String pref)
	{
		type = sp.getInt(pref + "type", -1);
		if (type < 3) ex_i = sp.getInt(pref + "ex_i", -1);
		if (type > 0) ex_s = sp.getString(pref + "ex_s", "  <->  ");
	}
	
	public final void save(final Editor e, final String pref)
	{
		e.putInt(pref + "type", type);
		if (type < 3) e.putInt(pref + "ex_i", ex_i);
		if (type > 0) e.putString(pref + "ex_s", ex_s);
	}
}