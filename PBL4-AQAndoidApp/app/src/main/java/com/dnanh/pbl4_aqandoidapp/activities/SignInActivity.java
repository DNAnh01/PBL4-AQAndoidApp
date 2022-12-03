package com.dnanh.pbl4_aqandoidapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.dnanh.pbl4_aqandoidapp.R;
import com.dnanh.pbl4_aqandoidapp.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {


    /*
    bật chế độ xemBinding cho dự, lớp liên kết cho từng bố cục XML sẽ được tạo tự động
    */
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    /*
    một thể hiện của lớp ràng buộc chứa các tham chiếu trực tiếp đến tất cả
    các layout có ID trong đó tương ứng
     */
    private void setListeners() {
        binding.textCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
    }
}