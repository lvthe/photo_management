package com.example.photo_management.utils;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.photo_management.R;
import com.example.photo_management.activities.CustomerActivity;
import com.example.photo_management.activities.OrderActivity;
import com.example.photo_management.activities.SearchOrderActivity;
import com.example.photo_management.activities.ServiceActivity;

/**
 * Thanh điều hướng dưới tự dựng (layout view_bottom_nav.xml) thay cho
 * BottomNavigationView của Material — để canh icon chính xác, không lệ thuộc
 * hành vi nội bộ của widget.
 *
 * Mỗi màn là 1 Activity riêng. Đổi tab = mở Activity đích rồi finish() màn hiện
 * tại (không hiệu ứng) → back-stack luôn gọn: Dashboard → 1 màn chức năng.
 */
public final class NavBar {

    private NavBar() {
    }

    /** @param activeId id của ô tab đang đứng (vd R.id.navCustomer). */
    public static void setup(AppCompatActivity activity, int activeId) {
        bind(activity, R.id.navCustomer, R.id.icCustomer, CustomerActivity.class, activeId);
        bind(activity, R.id.navService, R.id.icService, ServiceActivity.class, activeId);
        bind(activity, R.id.navOrder, R.id.icOrder, OrderActivity.class, activeId);
        bind(activity, R.id.navSearch, R.id.icSearch, SearchOrderActivity.class, activeId);
    }

    private static void bind(AppCompatActivity activity, int frameId, int iconId,
                             Class<?> target, int activeId) {
        View frame = activity.findViewById(frameId);
        ImageView icon = activity.findViewById(iconId);
        if (frame == null || icon == null) {
            return;
        }

        boolean active = (frameId == activeId);
        if (active) {
            icon.setBackgroundResource(R.drawable.bg_nav_pill);
            icon.setColorFilter(ContextCompat.getColor(activity, R.color.nav_item_active));
        } else {
            icon.setBackground(null);
            icon.setColorFilter(ContextCompat.getColor(activity, R.color.nav_item_inactive));
        }

        frame.setOnClickListener(v -> {
            if (active) {
                return; // đang ở tab này
            }
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
            activity.overridePendingTransition(0, 0);
        });
    }
}
