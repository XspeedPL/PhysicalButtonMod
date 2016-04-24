package xeed.xposed.cbppmod.prf;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import xeed.xposed.cbppmod.R;

public class ColorPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
    private final ColorDrawable mImage;
    
    Button okColorButton;
    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    private int red, green, blue;

    public ColorPreference(final Context c, final AttributeSet as)
    {
        super(c, as);
        mImage = new ColorDrawable(0xffffff);
    }

    @Override
    protected final void onSetInitialValue(final boolean restore, final Object def) { }
    
    @Override
    protected final Object onGetDefaultValue(final TypedArray ta, final int i)
    {
        return ta.getInt(i, 0);
    }
    
    @SuppressLint("InflateParams")
    @Override
    protected void onPrepareDialogBuilder(final Builder b)
    {
        super.onPrepareDialogBuilder(b);
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.color_picker, null, false);
        b.setView(v);
        redSeekBar = (SeekBar)v.findViewById(R.id.redSeekBar);
        greenSeekBar = (SeekBar)v.findViewById(R.id.greenSeekBar);
        blueSeekBar = (SeekBar)v.findViewById(R.id.blueSeekBar);
        redSeekBar.setOnSeekBarChangeListener(this);
        greenSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);
        
        int c = 0xffffff;
        try { c = getPersistedInt(c); }
        catch (final Exception ex) { ex.printStackTrace(); }
        updateColor11(c);
        red = Color.red(c);
        green = Color.green(c);
        blue = Color.blue(c);
        
        redSeekBar.setProgress(red);
        greenSeekBar.setProgress(green);
        blueSeekBar.setProgress(blue);
        final int size = getPx(25);
        mImage.setBounds(0, 0, size, size);
    }

    @TargetApi(11)
    private final void updateColor11(final int color)
    {
        if (Build.VERSION.SDK_INT > 10) mImage.setColor(color);
    }
    
    @Override
    public final void showDialog(final Bundle b)
    {
        super.showDialog(b);
        final AlertDialog ad = (AlertDialog)getDialog();
        okColorButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        okColorButton.setCompoundDrawablePadding(getPx(7));
        okColorButton.setCompoundDrawables(mImage, null, null, null);
    }
    
    public final int getPx(final float dp)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (seekBar.getId() == R.id.redSeekBar) red = progress;
        else if (seekBar.getId() == R.id.greenSeekBar) green = progress;
        else if (seekBar.getId() == R.id.blueSeekBar) blue = progress;
        updateColor11(getColor());
    }
    
    @Override
    public final void onClick(final DialogInterface di, final int pos)
    {
        if (pos == DialogInterface.BUTTON_POSITIVE) persistInt(getColor());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    public int getRed() { return red; }

    public int getGreen() { return green; }

    public int getBlue() { return blue; }

    public int getColor() { return Color.rgb(red, green, blue); }
}
