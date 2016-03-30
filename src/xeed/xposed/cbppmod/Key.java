package xeed.xposed.cbppmod;

import android.os.SystemClock;

public final class Key
{
    public long sw;
    public int code, dl;
    public boolean dn;
    
    public Key(final int kcode, final boolean down, final int delay)
    {
        code = kcode; dn = down;
        dl = delay < 0 ? 0 : delay;
        sw = SystemClock.elapsedRealtime();
    }
}
