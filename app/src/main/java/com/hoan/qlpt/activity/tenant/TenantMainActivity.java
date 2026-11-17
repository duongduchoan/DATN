package com.hoan.qlpt.activity.tenant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hoan.qlpt.R;
import com.hoan.qlpt.fragment.NotificationsFragment;
import com.hoan.qlpt.fragment.ProfileFragment;
import com.hoan.qlpt.fragment.SearchRoomsFragment;
import com.hoan.qlpt.fragment.TenantHomeFragment;
import com.hoan.qlpt.fragment.TenantInvoicesFragment;
import com.hoan.qlpt.util.AccountGuard;
import com.hoan.qlpt.util.Role;

public class TenantMainActivity extends AppCompatActivity {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_main);

        nav = findViewById(R.id.bottomNav);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                show(new ProfileFragment());
            } else if (id == R.id.nav_home) {
                show(new TenantHomeFragment());
            } else if (id == R.id.nav_invoices) {
                show(new TenantInvoicesFragment());
            } else if (id == R.id.nav_notifications) {
                show(new NotificationsFragment());
            } else if (id == R.id.nav_search) {
                show(new SearchRoomsFragment());
            }

            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }

        refreshBadge();
    }

    private void show(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadge();
    }

    private void refreshBadge() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null || nav == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(result -> {
                    long count = result.getCount();

                    if (count > 0) {
                        BadgeDrawable badge =
                                nav.getOrCreateBadge(R.id.nav_notifications);
                        badge.setNumber((int) count);
                        badge.setVisible(true);
                    } else {
                        nav.removeBadge(R.id.nav_notifications);
                    }
                });
    }

    private final AccountGuard guard = new AccountGuard();

    @Override
    protected void onStart() {
        super.onStart();
        guard.start(this, Role.TENANT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        guard.stop();
    }
}
