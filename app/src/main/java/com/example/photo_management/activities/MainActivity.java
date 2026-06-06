package com.example.photo_management.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.photo_management.R;

public class MainActivity extends AppCompatActivity {

    private View btnCustomer, btnService, btnOrder, btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCustomer = findViewById(R.id.btnCustomer);
        btnService = findViewById(R.id.btnService);
        btnOrder = findViewById(R.id.btnOrder);
        btnSearch = findViewById(R.id.btnSearch);

        // khách hàng
        btnCustomer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CustomerActivity.class)));

        btnService.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ServiceActivity.class)));

        btnOrder.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, OrderActivity.class)));

        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SearchOrderActivity.class)));
    }
}