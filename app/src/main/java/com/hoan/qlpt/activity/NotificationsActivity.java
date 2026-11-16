package com.hoan.qlpt.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hoan.qlpt.R;
import com.hoan.qlpt.fragment.NotificationsFragment;

public class NotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new NotificationsFragment()).commit();
        }
    }
}