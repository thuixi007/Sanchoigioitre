package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DangKi extends AppCompatActivity {

    /// Tạo biến
    EditText ten_dang_nhap;
    EditText ten_nguoi_dung;
    EditText mat_khau;
    EditText nhap_lai_mat_khau;
    Button dang_ki;

    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ki);

        //////// Ánh xạ các thứ trên màn hình
        ten_dang_nhap = findViewById(R.id.textbox_dk_ten_dang_nhap);
        ten_nguoi_dung = findViewById(R.id.textbox_dk_ten_nguoi_dung);
        mat_khau = findViewById(R.id.textbox_dk_mat_khau);
        nhap_lai_mat_khau = findViewById(R.id.textbox_dk_re_mat_khau);
        dang_ki = findViewById(R.id.btn_dang_ki_main);
        TextView dangnhap = (TextView) findViewById(R.id.dang_nhap_ngay);

        ////// Xử lí click vào chữ đăng nhập ngay
        dangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dang_nhap();
            }
        });

        ///// Xử lý nút đăng kí
        dang_ki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DangKi.this,"Click ok",Toast.LENGTH_SHORT).show();
                Start_check_dangki();
            }
        });


    }

    private void Start_check_dangki() {


        /// tạo biến String của các giá trị đầu vào
       String ten_dang_nhap_txt = ten_dang_nhap.getText().toString();
       String ten_nguoi_dung_txt = ten_nguoi_dung.getText().toString();
       String mat_khau_txt = mat_khau.getText().toString();
       String nhap_lai_mat_khau_txt = nhap_lai_mat_khau.getText().toString();

        ///// Kiểm tra đầu vào xem có bị trống không

        if(ten_dang_nhap_txt.isEmpty() || ten_nguoi_dung_txt.isEmpty() || mat_khau_txt.isEmpty() || nhap_lai_mat_khau_txt.isEmpty()){
            Toast.makeText(DangKi.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }else if(!mat_khau_txt.equals(nhap_lai_mat_khau_txt)){
            Toast.makeText(DangKi.this,"Mật khẩu và nhập lại mật khẩu không trùng khớp",Toast.LENGTH_SHORT).show();
        }else {

            CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(ten_dang_nhap_txt)){
                        Toast.makeText(DangKi.this,"Tài khoản này đã tồn tại!",Toast.LENGTH_SHORT).show();
                    }else {
                        /// Tạo tk trên CSDL firebase
                        CSDL.child("user").child(ten_dang_nhap_txt).child("password").setValue(mat_khau_txt);
                        CSDL.child("user").child(ten_dang_nhap_txt).child("full_name").setValue(ten_nguoi_dung_txt);
                        CSDL.child("user").child(ten_dang_nhap_txt).child("avatar_file").setValue("none");

                        Toast.makeText(DangKi.this,"Tạo tài khoản thành công!",Toast.LENGTH_SHORT).show();
                        Dang_nhap();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    }

    private void Dang_nhap(){
        Intent intent = new Intent(DangKi.this , DangNhap.class);
        startActivity(intent); }

}