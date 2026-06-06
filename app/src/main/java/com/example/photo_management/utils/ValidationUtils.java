package com.example.photo_management.utils;

import android.text.TextUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class ValidationUtils {

    public static boolean isEmpty(String value) {
        return TextUtils.isEmpty(value) || TextUtils.isEmpty(value.trim());
    }

    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return phone.matches("^[0-9]{9,11}$");
    }

    public static boolean isPositiveDouble(String value) {
        try {
            double d = Double.parseDouble(value.trim());
            return d > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPositiveInt(String value) {
        try {
            int i = Integer.parseInt(value.trim());
            return i > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount) + " đ";
    }

    /**
     * Hiển thị số gọn để nạp vào ô nhập: bỏ phần ".0" thừa.
     * Ví dụ: 300000.0 -> "300000", 2.0 -> "2", 1.5 -> "1.5".
     */
    public static String formatPlain(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return new java.math.BigDecimal(String.valueOf(value))
                .stripTrailingZeros()
                .toPlainString();
    }
}