package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Edit_user extends AppCompatActivity {

    EditText user_name_update,pass_update,re_pass_update;

    CircleImageView avatar;

    Button luu_chinh_sua;

    private ActionBar actionBar;


    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();
    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference();

    ///Chọn hình ảnh
    public Uri image_uri;

    ////Lấy về giá trị truyền của tên tài khoản
    String tentaikhoan ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);


        /// Ánh xạ các giá trị sử dụng
        user_name_update = findViewById(R.id.user_name_display);
        pass_update = findViewById(R.id.user_name_password);
        re_pass_update = findViewById(R.id.user_name_re_password);
        avatar = findViewById(R.id.user_avatar);

        luu_chinh_sua = findViewById(R.id.btn_profile_save);

        ///// set tên tài khoản

        tentaikhoan = getIntent().getStringExtra("tentaikhoan");
        /// Sửa thanh action bar;
        actionBar = getSupportActionBar();
        actionBar.setTitle("Sửa thông tin tài khoản");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);



        /// Set tên người sử dụng lên trên màn hình
        set_user_Name_txt(tentaikhoan);
        /// Set mật khẩu lên trên màn hình
        ///// Set ảnh đại diện
        set_avatar(tentaikhoan);



        //// Xử lí nút ảnh đại diện
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_pic();
            }
        });

        //// Xử lí nút lưu chỉnh sửa
        luu_chinh_sua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_in4();
            }
        });

    }

    private void save_in4() {

        //// kiểm tra tải lên hình ảnh
        DocumentFile file = DocumentFile.fromSingleUri(this, image_uri);
        if(image_uri != null && !image_uri.equals(Uri.EMPTY)){
            String fileName = file.getName();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Đang tải lên hình ảnh...");
            progressDialog.show();
            IMG_CSDL.child("avatar").child(tentaikhoan).child(fileName).putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.hide();
                }
            });
            CSDL.child("user").child(tentaikhoan).child("avatar_file").setValue(fileName);
        }





        //// Cập nhật các thông tin lên cơ sở dữ liệu

        /////Kiểm tra tên có trống không
        if(  user_name_update.getText().toString().isEmpty() ){
            Toast.makeText(this, "Vui lòng điền tên vào chỗ trống", Toast.LENGTH_SHORT).show();
            return;
        }else {
            CSDL.child("user").child(tentaikhoan).child("full_name").setValue(user_name_update.getText().toString());
        }

        ////Kiểm tra mật khẩu có trống không, nếu cả 2 ô ko trống sẽ so sánh và cập nhật
        if (!pass_update.getText().toString().isEmpty() && !re_pass_update.getText().toString().isEmpty() ){

            if (pass_update.getText().toString().equals(re_pass_update.getText().toString())){
                CSDL.child("user").child(tentaikhoan).child("password").setValue(pass_update.getText().toString());
            }else {
                Toast.makeText(this, "Bạn vui lòng nhập mật khẩu mới và nhập lại mật khẩu mới trùng khớp", Toast.LENGTH_SHORT).show();
                return;
            }

        }else if (!pass_update.getText().toString().isEmpty() && re_pass_update.getText().toString().isEmpty()
        || pass_update.getText().toString().isEmpty() && !re_pass_update.getText().toString().isEmpty()
        ){
            Toast.makeText(this, "Bạn vui lòng nhập mật đầy đủ thông tin để đổi mật khẩu nhé", Toast.LENGTH_SHORT).show();
            return;
        }




        Toast.makeText(Edit_user.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Edit_user.this, activity_user_profile.class).putExtra("tentaikhoan",tentaikhoan));
        overridePendingTransition(0,0);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 && resultCode == RESULT_OK && data!=null && data.getData()!= null ){
            image_uri = data.getData();
            avatar.setImageURI(image_uri);
        }
    }

    private void select_pic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }


    private void set_avatar(String tentaikhoan) {

        ////Lấy tên avatar

        CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               String avt_name = snapshot.child(tentaikhoan).child("avatar_file").getValue(String.class);
                start_set_avt(tentaikhoan,avt_name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void start_set_avt(String tentaikhoan, String avt_name) {

        IMG_CSDL.child("avatar").child(tentaikhoan).child(avt_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(avatar);
            }
        });

    }


    private void set_user_Name_txt(String tentaikhoan) {
        CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user_name_update.setText(snapshot.child(tentaikhoan).child("full_name").getValue(String.class) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Edit_user.this, "Có lỗi khi tải tên tài khoản",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;}

}