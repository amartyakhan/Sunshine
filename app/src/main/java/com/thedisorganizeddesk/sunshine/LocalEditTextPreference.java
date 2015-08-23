package com.thedisorganizeddesk.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by amkhan on 23-Aug-15.
 */
public class LocalEditTextPreference extends EditTextPreference {
    static int mMinLength;
    static View mEditTextDialog;
    static int DEFAULT_MINIMUM_LOCATION_LENGTH=2;
    public LocalEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.LocalEditTextPreference,0,0);
        try{
            mMinLength=a.getInteger(R.styleable.LocalEditTextPreference_minLength,DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText=getEditText();
        editText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Dialog d= getDialog();
                        if(d instanceof AlertDialog){
                            AlertDialog dialog=(AlertDialog) d;
                            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if(s.length()<mMinLength){
                                positiveButton.setEnabled(false);
                            }
                            else{
                                positiveButton.setEnabled(true);
                            }
                        }
                    }
                }
        );

    }
}
