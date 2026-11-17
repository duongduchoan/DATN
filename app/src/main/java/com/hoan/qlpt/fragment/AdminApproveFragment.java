package com.hoan.qlpt.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.hoan.qlpt.R;

public class AdminApproveFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_approve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment f;
                switch (tab.getPosition()) {
                    case 0:
                        f = new ApproveHousesFragment();
                        break;
                    case 1:
                        f = new ApproveRoomsFragment();
                        break;
                    default:
                        f = new RoleRequestsFragment();
                }
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.childContainer, f).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        tabs.addTab(tabs.newTab().setText("Điểm trọ"));
        tabs.addTab(tabs.newTab().setText("Phòng"));
        tabs.addTab(tabs.newTab().setText("Lên chủ trọ"));
    }
}