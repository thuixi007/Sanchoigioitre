package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DangNhap extends AppCompatActivity {

    /// Khai báo các biến sử dụng

    EditText ten_dang_nhap;
    EditText mat_khau;
    Button dang_nhap;
    TextView dang_ki_ngay;
    CheckBox save_tk_mk;
    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        SharedPreferences sharedPreferences = getSharedPreferences("set_ten_tk", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        ///// Ánh xạ các biến
        ten_dang_nhap = findViewById(R.id.textbox_login_ten_dang_nhap);
        mat_khau = findViewById(R.id.textbox_login_mat_khau);
        dang_nhap = findViewById(R.id.btn_dang_nhap_main);
        dang_ki_ngay = findViewById(R.id.dang_ki_ngay);
        save_tk_mk = findViewById(R.id.checkBox);


        /// Kiểm tra xem đã lưu trước đó chưa
            Boolean is_after_save = sharedPreferences.getBoolean("login_save", false);
            save_tk_mk.setChecked(is_after_save);
        /// Kiểm tra checkbox đã check lưu tài khoản chưa
        if( is_after_save == true ){
            String ten_dang_nhap_save = sharedPreferences.getString("login_tk", "");
            String mat_khau_save = sharedPreferences.getString("login_mk", "");
            ten_dang_nhap.setText(ten_dang_nhap_save  );
            mat_khau.setText( mat_khau_save );
        }

        //// Xử lí nút đăng nhập
        dang_nhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ten_dang_nhap.getText().toString().equals("") || mat_khau.getText().toString().equals("")){
                    Toast.makeText(DangNhap.this, "Bạn vui lòng điền đầy đủ tên đăng nhập và mật khẩu",Toast.LENGTH_SHORT).show();
                }else {

                    CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(ten_dang_nhap.getText().toString() )){
                                String get_pass = snapshot.child(ten_dang_nhap.getText().toString()).child("password").getValue(String.class);

                                if(get_pass.equals(mat_khau.getText().toString() )){
                                    Toast.makeText(DangNhap.this, "Đăng nhập thành công",Toast.LENGTH_SHORT).show();

                                    ///// Kiểm tra lưu tài khoản / mật khẩu, nếu lưu thì tự động điền lần sau
                                    if(save_tk_mk.isChecked()){
                                        editor.putString("login_tk", ten_dang_nhap.getText().toString());
                                        editor.putString("login_mk", mat_khau.getText().toString());
                                        editor.putBoolean("login_save", true);
                                        editor.apply();
                                    }else {
                                        editor.clear().apply();
                                    }


                                    editor.putString("tentaikhoan", ten_dang_nhap.getText().toString());
                                    editor.apply();

                                    startActivity(new Intent(DangNhap.this,Blog_show.class).putExtra("tentaikhoan", ten_dang_nhap.getText().toString() ));

                                    finish();

                                }else {
                                    Toast.makeText(DangNhap.this, "Sai mật khẩu rồi",Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(DangNhap.this, "Không tìm thấy tên đăng nhập này",Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }
        });

        dang_ki_ngay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DangNhap.this , DangKi.class);
                startActivity(intent);
            }
        });

    }
}