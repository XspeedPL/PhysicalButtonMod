package xeed.xposed.cbppmod;

final class Key
{
    int code;
    int dl;
    boolean dn;
    
    Key(final int kcode, final boolean down, final int delay)
    {
        code = kcode;
        dn = down; dl = delay;
    }
}
