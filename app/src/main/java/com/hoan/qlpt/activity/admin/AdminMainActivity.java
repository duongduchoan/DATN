package com.hoan.qlpt.activity.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hoan.qlpt.R;
import com.hoan.qlpt.fragment.AdminApproveFragment;
import com.hoan.qlpt.fragment.AdminOverviewFragment;
import com.hoan.qlpt.fragment.AdminUsersFragment;
import com.hoan.qlpt.fragment.ProfileFragment;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                // Tab Cá nhân
                show(new ProfileFragment());

            } else if (id == R.id.nav_admin_overview) {
                // Tab Tổng quan hệ thống
                show(new AdminOverviewFragment());

            } else if (id == R.id.nav_approve) {
                // Tab duyệt yêu cầu đăng ký làm chủ trọ
                show(new AdminApproveFragment());

            } else if (id == R.id.nav_users) {
                // Tab Quản lý người dùng
                show(new AdminUsersFragment());
            }

            return true;
        });

        // Tab mặc định
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_admin_overview);
        }
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
