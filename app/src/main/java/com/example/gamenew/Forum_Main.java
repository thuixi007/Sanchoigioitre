package com.example.gamenew;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Forum_Main extends AppCompatActivity {

    Dialog popAddpost;
    String tentaikhoan;

    /////
    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference("Posts");
    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference("Posts");

    ImageView popupUserImage, popupPopimage, popupAddbtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;

    Post post;
    ///Chọn hình ảnh
    public Uri image_uri;

    RecyclerView postrecyclerView;
    PostAdapter postAdapter;

    List<Post> postList;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_main);
        Runtime.getRuntime().gc();
        // anh xa
        postrecyclerView = findViewById(R.id.postRV_user_post);
        postrecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        postrecyclerView.setHasFixedSize(true);

////Lấy về giá trị truyền của tên tài khoản
        tentaikhoan = getIntent().getStringExtra("tentaikhoan");

        //Sửa action bar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Diễn đàn bài viết");


        /// Thiết lập popup Thêm bài viết
        popAddpost = new Dialog(Forum_Main.this);
        popAddpost.setContentView(R.layout.popup_add_post);
        popAddpost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddpost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        popAddpost.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = popAddpost.findViewById(R.id.popup_user_image);
        popupPopimage = popAddpost.findViewById(R.id.popup_img);
        popupTitle = popAddpost.findViewById(R.id.popup_title);
        popupDescription = popAddpost.findViewById(R.id.popup_description);
        popupAddbtn = popAddpost.findViewById(R.id.popup_add);
        popupClickProgress = popAddpost.findViewById(R.id.popup_progressBar);

        //// thêm nút click thêm bài viết trên popup

        popupAddbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!popupTitle.getText().toString().isEmpty()
                        && !popupDescription.getText().toString().isEmpty()
                        && image_uri != null ) {
                    popupAddbtn.setVisibility(View.INVISIBLE);
                    popupClickProgress.setVisibility(View.VISIBLE);
                    start_add_post_to_db();

                }else {
                    Toast.makeText(Forum_Main.this,"Bạn hãy điền đầy đủ tiêu đề, nội dung, và thêm hình ảnh nhé!",Toast.LENGTH_SHORT).show();

                }


            }
        });

        /////// Thêm nút click thay đổi hình ảnh của popup trên bài viết
        popupPopimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_pic();

            }
        });


        //// Hiển thị popup thêm bài viết
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Log.d("TAG", "onCreate: " + tentaikhoan);
        set_avatar(tentaikhoan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popAddpost.show();
            }
        });


        ///Bottom nav
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setSelectedItemId(R.id.nav_forum);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_news:
                        startActivity(new Intent(Forum_Main.this, Blog_show.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                        ActivityCompat.finishAffinity(Forum_Main.this);
                        //System.exit(0);


                        break;
                    case R.id.nav_forum:
                        break;
                    case R.id.nav_follow:
                        startActivity(new Intent(Forum_Main.this, activity_my_post.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                        ActivityCompat.finishAffinity(Forum_Main.this);
                        //System.exit(0);
                        break;
                    case R.id.nav_account:
                        finish();
                        startActivity(new Intent(Forum_Main.this, activity_user_profile.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);

                        break;
                }
                return true;
            }
        });



        ///////// Load bai viet - của tôi
        load_all_post();

    }

    private void load_all_post() {

        CSDL.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList = new ArrayList<>();

                    for(DataSnapshot postsnap : snapshot.getChildren() ) {
                        Post post = postsnap.getValue(Post.class);
                        postList.add(post);
                    }
                    Collections.reverse(postList);
                    postAdapter = new PostAdapter(Forum_Main.this, postList );
                    postrecyclerView.setAdapter(postAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void start_add_post_to_db() {

        //// lấy tên hình ảnh
        DocumentFile file = DocumentFile.fromSingleUri(this, image_uri);
        String fileName = file.getName();

        Post post = new Post(popupTitle.getText().toString(),
                popupDescription.getText().toString(),
                fileName,
                tentaikhoan,
                tentaikhoan);
        
        add_new_post(post, fileName);


    }

    private void add_new_post(Post post, String fileName) {

        String key = CSDL.push().getKey();
        post.setPostKey(key);

        ///// Tải bài viết lên CSDL
        CSDL.push().setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {


                ///// Tải hình ảnh lên CSDL
                IMG_CSDL.child(tentaikhoan).child(post.getPostKey()).child(fileName).putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });

                Toast.makeText(Forum_Main.this,"Thêm bài viết thành công",Toast.LENGTH_SHORT).show();
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddbtn.setVisibility(View.VISIBLE);
                popAddpost.dismiss();
                finish();
                startActivity(getIntent());
            }
        });


    }

    private void set_avatar(String tentaikhoan) {

        ////Lấy tên avatar

        FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
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

        FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference().child("avatar").child(tentaikhoan).child(avt_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(popupUserImage);
            }
        });

    }

    private void select_pic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 && resultCode == RESULT_OK && data!=null && data.getData()!= null ){
            image_uri = data.getData();
            popupPopimage.setImageURI(image_uri);
        }
    }

    @Override
    protected void onDestroy() {


        Runtime.getRuntime().gc();
        super.onDestroy();
    }
}