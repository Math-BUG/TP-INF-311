package com.grupo10.inf311.docscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "SettingsBottomSheetFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView settingItemAccount = view.findViewById(R.id.setting_item_account);
        TextView settingItemNotifications = view.findViewById(R.id.setting_item_notifications);
        TextView settingItemLogout = view.findViewById(R.id.setting_item_logout);

        settingItemAccount.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Minha Conta Clicado", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        settingItemNotifications.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notificações Clicado", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        settingItemLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Sair Clicado", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}

