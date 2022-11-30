package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_user_profile extends AppCompatActivity {

    private ActionBar actionBar;
    private CircleImageView avatar;
    private TextView user_name_display;
    private LinearLayout click_user_detail;
    private Button btn_exit;

    /// Thư viện cho firebase

    private StorageReference storage_user_pic_Reference;

    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();
    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //// Khai báo các biến sử dụng
        avatar = findViewById(R.id.user_avatar);
        user_name_display = findViewById(R.id.user_name_display);
        click_user_detail = findViewById(R.id.layout0);
        btn_exit = findViewById(R.id.button_exit);


        ////Lấy về giá trị truyền của tên tài khoản
        String tentaikhoan = getIntent().getStringExtra("tentaikhoan");
        /// Set tên người sử dụng lên trên màn hình
        set_user_Name_txt(tentaikhoan);
        /// Set avatar của tài khoản
        ///// Set ảnh đại diện
        set_avatar(tentaikhoan);


        /// Sửa thanh action bar;
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chi tiết cá nhân");


        //// Xử lý các nút click

        /// Click thoát
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
                System.exit(0);

            }
        });

        /// Click sửa thông tin cá nhân
        click_user_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity_user_profile.this, Edit_user.class).putExtra("tentaikhoan",tentaikhoan));
            }
        });

        ///Bottom nav
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setSelectedItemId(R.id.nav_account);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_news:
                        startActivity(new Intent(activity_user_profile.this, Blog_show.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.nav_forum:
                        startActivity(new Intent(activity_user_profile.this, Forum_Main.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                    case R.id.nav_follow:
                        startActivity(new Intent(activity_user_profile.this, activity_my_post.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.nav_account:
                        break;
                }
                return true;
            }
        });

    }

    private void set_user_Name_txt(String tentaikhoan) {

        CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user_name_display.setText(snapshot.child(tentaikhoan).child("full_name").getValue(String.class) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_user_profile.this, "Có lỗi khi tải tên tài khoản",Toast.LENGTH_SHORT).show();
            }
        });

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

}