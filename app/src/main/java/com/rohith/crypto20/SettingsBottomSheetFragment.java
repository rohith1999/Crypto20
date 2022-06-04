package com.rohith.crypto20;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rohith.crypto20.permenant.Constants;
import com.rohith.crypto20.permenant.PreferenceManager;

import org.jetbrains.annotations.NotNull;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {

    private View main_view;
    private SwitchMaterial fingerPrintButton;
    private PreferenceManager preferenceManager;

    public SettingsBottomSheetFragment() {
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

       main_view=inflater.inflate(R.layout.bottom_sheet_settings,container,false);


       fingerPrintButton=main_view.findViewById(R.id.fingerprint_switch);


       preferenceManager=new PreferenceManager(getActivity());

        if (preferenceManager.getBoolean(Constants.USER_FINGERPRINT)){
            fingerPrintButton.setChecked(true);
        }else{
            fingerPrintButton.setChecked(false);
        }

        fingerPrintButton.setOnClickListener(view -> {
            if (fingerPrintButton.isChecked()){
                preferenceManager.putBoolean(Constants.USER_FINGERPRINT,true);
            }else{
                preferenceManager.putBoolean(Constants.USER_FINGERPRINT,false);
            }

        });

        return main_view;
    }


}
