package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kotlin.contracts.Returns;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost,imgUserPost,imgCurrentUser;
    TextView txtPostDesc,txtPostDateName,txtPostTitle;
    EditText editTextComment;
    Button btnAddComment;
    ///Kết nối tới cơ sở dữ liệu
    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();
    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference();
    private ActionBar actionBar;


    //////// comment setup
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;



    ///// Khai bao bien truyen du lieu
    String postImage,postTitle,userpostImage,postkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //Sửa action bar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chi tiết bài đăng");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        ///// Ánh xạ các thể loại trên màn hình
        imgPost =findViewById(R.id.post_detail_img);
        imgUserPost = findViewById(R.id.post_detail_user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        txtPostTitle = findViewById(R.id.post_detail_title);
        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDateName = findViewById(R.id.post_detail_date_name);

        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);

        RvComment = findViewById(R.id.rv_comment);



        // Lấy post data
        postTitle = getIntent().getExtras().getString("title", "");
        txtPostTitle.setText(postTitle);


        postImage = getIntent().getExtras().getString("postImage") ;
        //Glide.with(this).load(postImage).into(imgPost);




        userpostImage = getIntent().getExtras().getString("userPhoto");

        postkey = getIntent().getExtras().getString("postKey");
        //Glide.with(this).load(userpostImage).into(imgUserPost);

        //// Set img post
        IMG_CSDL.child("Posts").child(userpostImage).child(postkey).child(postImage).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(imgPost);
            }
        });

        // set img post user
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        imgUserPost.setImageBitmap(bitmap);

        /// Set img current_user comment
        SharedPreferences sharedPreferences = getSharedPreferences("set_ten_tk", Context.MODE_PRIVATE);
        String tendangnhap = sharedPreferences.getString("tentaikhoan", null);
        set_avatar(tendangnhap);

        String postDescription = getIntent().getExtras().getString("description", "");
        txtPostDesc.setText(postDescription);

        String date = timestampToString(getIntent().getExtras().getLong("postDate"));
        txtPostDateName.setText(date);

        //////// An ban phim
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);



        /// Xu ly nut them comment
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference comentReference = CSDL.child("Comment").child(postkey).push();

                String comment_content = editTextComment.getText().toString();
                String uid = sharedPreferences.getString("tentaikhoan", null);

                    if (comment_content.equals("")){
                        Toast.makeText(PostDetailActivity.this, "Bạn vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
                    }else {
                        Comment comment = new Comment(comment_content,uid);

                        comentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(PostDetailActivity.this, "Thêm bình luận thành công", Toast.LENGTH_SHORT).show();
                                editTextComment.getText().clear();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostDetailActivity.this, "Có lỗi khi thêm bình luận", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

            }
        });
        System.gc();
        loadcomment();
    }

    private void loadcomment() {

        RvComment.setLayoutManager(new LinearLayoutManager(this));

        CSDL.child("Comment").child(postkey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment = new ArrayList<>();
                listComment.clear();
                for (DataSnapshot snap: snapshot.getChildren()){
                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(), listComment);
                RvComment.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;

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
                Picasso.get().load(uri.toString() ).into(imgCurrentUser);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;}

    @Override
    protected void onDestroy() {
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

}