package xeed.xposed.cbppmod.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import xeed.xposed.cbppmod.R;
import xeed.xposed.cbppmod.Chain;
import xeed.xposed.cbppmod.PBMain;

public final class PagerAdapter extends FragmentStatePagerAdapter
{
    private final ArrayList<Chain> chs = new ArrayList<Chain>();
    private final ViewPager vp;
    private ChainListFragment clf = null;
    private ChainEditFragment cef = null;
    private KeyTestFragment ktf = null;
    public Chain edit;
    public boolean saved;
    
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public PagerAdapter(final ViewPager pager, final FragmentManager fm)
    {
        super(fm);
        vp = pager;
        final SharedPreferences sp = pager.getContext().getSharedPreferences("pbmcsettings", Context.MODE_WORLD_READABLE);
        final int n = sp.getInt("chainlist.count", 0);
        for (int i = 0; i < n; ++i)
        {
            final String name = sp.getString("chainlist." + i, "");
            if (name.length() > 0) addChain(new Chain(sp, name));
        }
        saved = true;
    }
    
    public final List<Chain> getChains() { return chs; }
    
    public final ListIterator<Chain> getIterator() { return chs.listIterator(); }
    
    public final void addChain(final Chain ch) { chs.add(ch); }
    
    @Override
    public final Fragment getItem(final int i)
    {
        if (i == 0) return getList();
        else if (i == 1) return getTest();
        else return getEdit();
    }
    
    public final ChainListFragment getList()
    {
        return clf == null ? (clf = new ChainListFragment()) : clf;
    }
    
    public final ChainEditFragment getEdit()
    {
        return cef == null ? (cef = new ChainEditFragment()) : cef;
    }
    
    public final KeyTestFragment getTest()
    {
        return ktf == null ? (ktf = new KeyTestFragment()) : ktf;
    }
    
    @Override
    public final int getCount()
    {
        return edit == null ? 2 : 3;
    }
    
    @Override
    public final String getPageTitle(final int i)
    {
        if (i == 0) return PBMain.r.getString(R.string.tab_chns);
        else if (i == 1) return PBMain.r.getString(R.string.tab_test);
        else return PBMain.r.getString(R.string.tab_edit) + " " + edit.nm;
    }
    
    public final void setEdit(final Chain ch)
    {
        edit = ch;
        notifyDataSetChanged();
        if (ch != null)
        {
            getEdit().updateEdit();
            vp.setCurrentItem(2);
        }
    }
    
    public final void chainsSaved()
    {
        final Intent i = new Intent("xeed.xposed.cbppmod.Update");
        i.putExtra("xeed.xposed.cbppmod.Chains", true);
        vp.getContext().sendBroadcast(i);
        getList().changed(true);
    }
    
    public final void chainChanged(final boolean big)
    {
        if (big) notifyDataSetChanged();
        getList().changed(false);
    }
}
