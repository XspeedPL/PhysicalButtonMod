package xeed.xposed.cbppmod;

import java.util.LinkedList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

final class Chain
{
	final LinkedList<Key> ks = new LinkedList<Key>();
	int vib, rep, au, md;
	Action act;
	boolean ccl;
    String nm;
	
	Chain(final SharedPreferences sp, final String name)
	{
		nm = name;
		if (sp != null) load(sp);
		else act = new Action(Action.ACTION_NONE, -1);
	}
	
	@Override
	public final int hashCode() { return nm.hashCode(); }
	
	@Override
	public final boolean equals(final Object o) { return o instanceof Chain && nm.equals(((Chain)o).nm); }
	
    final void load(final SharedPreferences sp)
    {
    	final String pref = "chains." + nm + ".";
        act = new Action(sp, pref + "act.");
        try
        {
            vib = sp.getInt(pref + "vib", 20);
            rep = sp.getInt(pref + "rep", 0);
            au = sp.getInt(pref + "au", 7);
            md = sp.getInt(pref + "md", 1);
            ccl = sp.getBoolean(pref + "ccl", true);
        }
        catch (final Exception ex) { vib = 20; rep = 0; au = 7; md = 1; ccl = true; }
        int kct = sp.getInt(pref + "kct", 0);
        for (int i = 0; i < kct; ++i)
        {
        	int c = sp.getInt(pref + "k" + i + ".c", 0);
        	boolean dn = sp.getBoolean(pref + "k" + i + ".dn", false);
        	int dl = sp.getInt(pref + "k" + i + ".dl", -1);
        	ks.add(new Key(c, dn, dl));
        }
    }
    
    final void save(final Editor e)
    {
    	final String pref = "chains." + nm + ".";
        act.save(e, pref + "act.");
        e.putInt(pref + "vib", vib);
        e.putInt(pref + "rep", rep);
        e.putInt(pref + "au", au);
        e.putInt(pref + "md", md);
        e.putBoolean(pref + "ccl", ccl);
        e.putInt(pref + "kct", ks.size());
        int i = -1;
        for (final Key k : ks)
        {
        	e.putInt(pref + "k" + (++i) + ".c", k.code);
            e.putBoolean(pref + "k" + i + ".dn", k.dn);
            e.putInt(pref + "k" + i + ".dl", k.dl);
        }
    }
}
