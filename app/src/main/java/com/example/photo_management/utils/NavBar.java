package com.example.photo_management.utils;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photo_management.R;
import com.example.photo_management.activities.CustomerActivity;
import com.example.photo_management.activities.OrderActivity;
import com.example.photo_management.activities.SearchOrderActivity;
import com.example.photo_management.activities.ServiceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Gắn thanh điều hướng dưới (BottomNavigationView) cho các Activity chức năng.
 *
 * Mỗi màn hình là 1 Activity riêng (không dùng Fragment). Khi đổi tab: mở Activity
 * đích rồi {@code finish()} Activity hiện tại (không hiệu ứng) nên back-stack luôn
 * chỉ gồm: Dashboard → 1 màn chức năng. Đổi tab = thay thế, bấm Back luôn về thẳng
 * Dashboard — tránh tình trạng chồng/đảo thứ tự back-stack gây "chạy loạn".
 */
public final class NavBar {

    private NavBar() {
    }

    public static void setup(AppCompatActivity activity, BottomNavigationView nav, int selectedItemId) {
        // Đặt tab đang chọn TRƯỚC khi gắn listener để không kích hoạt điều hướng tới chính nó.
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // đang ở tab này rồi, bỏ qua
            }

            Class<?> target = null;
            if (id == R.id.nav_customer) {
                target = CustomerActivity.class;
            } else if (id == R.id.nav_service) {
                target = ServiceActivity.class;
            } else if (id == R.id.nav_order) {
                target = OrderActivity.class;
            } else if (id == R.id.nav_search) {
                target = SearchOrderActivity.class;
            }

            if (target == null) {
                return false;
            }

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish(); // thay thế màn hiện tại, không chồng back-stack
            activity.overridePendingTransition(0, 0);
            return true;
        });
    }
}
