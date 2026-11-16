package com.hoan.qlpt.activity.landlord;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hoan.qlpt.R;
import com.hoan.qlpt.fragment.LandlordInvoicesFragment;
import com.hoan.qlpt.fragment.LandlordOverviewFragment;
import com.hoan.qlpt.fragment.LandlordRequestsFragment;
import com.hoan.qlpt.fragment.MyRoomsFragment;
import com.hoan.qlpt.fragment.ProfileFragment;
import com.hoan.qlpt.util.AccountGuard;
import com.hoan.qlpt.util.Role;

public class LandlordMainActivity extends AppCompatActivity {

    private final AccountGuard guard = new AccountGuard();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_overview) show(new LandlordOverviewFragment());
            else if (id == R.id.nav_rooms) show(new MyRoomsFragment());
            else if (id == R.id.nav_requests) show(new LandlordRequestsFragment());
            else if (id == R.id.nav_invoices) show(new LandlordInvoicesFragment());
            else if (id == R.id.nav_profile) show(new ProfileFragment());
            return true;
        });

        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_overview);
    }

    private void show(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        guard.start(this, Role.LANDLORD);
    }

    @Override
    protected void onStop() {
        super.onStop();
        guard.stop();
    }
}