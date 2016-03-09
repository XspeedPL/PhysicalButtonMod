package xeed.xposed.cbppmod;

import java.util.LinkedList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

final class Chain
{
	final LinkedList<Key> ks = new LinkedList<Key>();
	int vib = 200, rep = 0, au = 7, md = 1;
	private int ez = -1;
	Action act;
	boolean ccl = true, en = true;
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
        	en = sp.getBoolean(pref + "en", true);
        	ez = sp.getInt(pref + "ez", -1);
            vib = sp.getInt(pref + "vib", 200);
            rep = sp.getInt(pref + "rep", 0);
            au = sp.getInt(pref + "au", 7);
            md = sp.getInt(pref + "md", 1);
            ccl = sp.getBoolean(pref + "ccl", true);
        }
        catch (final Exception ex) { }
        int kct = sp.getInt(pref + "kct", 0);
        for (int i = 0; i < kct; ++i)
        {
        	int c = sp.getInt(pref + "k" + i + ".c", 0);
        	boolean dn = sp.getBoolean(pref + "k" + i + ".dn", false);
        	int dl = sp.getInt(pref + "k" + i + ".dl", 0);
        	ks.add(new Key(c, dn, dl));
        }
    }
    
    final void save(final Editor e)
    {
    	final String pref = "chains." + nm + ".";
        act.save(e, pref + "act.");
        e.putBoolean(pref + "en", en);
        e.putInt(pref + "ez", ez);
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
    
    final boolean isEz() { return ez != -1; }
    
    final int getEz() { return ez; }
    
    final void setEz(final int t, final int key, final int dl)
    {
    	ks.clear();
    	if (t != -1)
    		for (int i = t % 3; i >= 0; --i)
    		{
    			ks.add(new Key(key, true, 0));
    			ks.add(new Key(key, false, 0));
    		}
    	if (t > 2) ks.removeLast();
    	ks.getLast().dl = dl;
    	ez = t;
    }
}
