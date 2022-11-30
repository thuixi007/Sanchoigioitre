package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.List;

public class my_editpost_Activity extends AppCompatActivity {

    ImageView imgPost;
    TextView txtPostDesc,txtPostTitle;


    Button Save,delete;
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

    ///Chọn hình ảnh
    public Uri image_uri;

    ///Upload img flag
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_editpost);



        //Sửa action bar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Sửa bài đăng");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        ///// Ánh xạ các thể loại trên màn hình
        imgPost = findViewById(R.id.post_img_edit);
        txtPostTitle = findViewById(R.id.textbox_tieude);
        txtPostDesc = findViewById(R.id.textbox_noidungbaiviet);


        Save = findViewById(R.id.button_save);
        delete = findViewById(R.id.button_delete);

        // Lấy post data của bài viết
        postTitle = getIntent().getExtras().getString("title", "");
        txtPostTitle.setText(postTitle);
        String postDescription = getIntent().getExtras().getString("description", "");
        txtPostDesc.setText(postDescription);

        postImage = getIntent().getExtras().getString("postImage") ;
        fileName = postImage;
        //Glide.with(this).load(postImage).into(imgPost);
        userpostImage = getIntent().getExtras().getString("userName");
        postkey = getIntent().getExtras().getString("postKey");
        //Glide.with(this).load(userpostImage).into(imgUserPost);

        //// Set img post
        IMG_CSDL.child("Posts").child(userpostImage).child(postkey).child(postImage).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(imgPost);
            }
        });


        ///// Xử lý nút chọn hình ảnh khác
        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_pic();
            }
        });

        ///// Xử lý nút cập nhật
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_update_post_to_bd();
            }
        });

        /////Xử lí nút xóa bài viết
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_delete_my_post();
            }
        });

    }

    private void start_delete_my_post() {
        /// Hỏi trước khi xóa

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn chắc chắn xóa bài viết này chứ?");

        builder.setPositiveButton("Có!", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

                CSDL.child("Posts").orderByChild("postKey").equalTo(postkey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ingredientSnapshot: snapshot.getChildren()) {

                            ingredientSnapshot.getRef().removeValue();



                        }

                        Toast.makeText(my_editpost_Activity.this, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(my_editpost_Activity.this, "Đã có lỗi kĩ thuật khi xóa bài đăng!", Toast.LENGTH_SHORT).show();
                    }
                });

                onBackPressed();
                finish();
                dialog.dismiss();
                return;
            }
        });

        builder.setNegativeButton("Không!", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing

                dialog.dismiss();

            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void start_update_post_to_bd() {

        //// lấy tên hình ảnh
        DocumentFile file = DocumentFile.fromSingleUri(this, image_uri);


        //Kiểm tra xem tiêu đề và nội dung có bị bỏ trống khogno
        if(txtPostTitle.getText().toString().equals("") | txtPostDesc.getText().toString().equals("")){
            Toast.makeText(this, "Bạn vui lòng điền đầy đủ nội dung bài đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        /// Kiểm tra xem có cập nhật ảnh mới lên không
        if(image_uri != null && !image_uri.equals(Uri.EMPTY)){
            fileName = file.getName();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Đang tải lên hình ảnh...");
            progressDialog.show();

            IMG_CSDL.child("Posts").child(userpostImage).child(postkey).child(fileName).putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.hide();

                }
            });

        }

        ///// Thay đổi nội dung tiêu đề và nội dung trên cơ sở dữ liệu
        CSDL.child("Posts").orderByChild("postKey").equalTo(postkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ingredientSnapshot: snapshot.getChildren()) {

                    ingredientSnapshot.getRef().child("title").setValue(txtPostTitle.getText().toString());
                    ingredientSnapshot.getRef().child("description").setValue(txtPostDesc.getText().toString());
                    ingredientSnapshot.getRef().child("picture").setValue(fileName);


                }

                Toast.makeText(my_editpost_Activity.this, "Cập nhật bài đăng thành công", Toast.LENGTH_SHORT).show();
                onBackPressed();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(my_editpost_Activity.this, "Không tìm thấy post key", Toast.LENGTH_SHORT).show();
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
            imgPost.setImageURI(image_uri);
        }
    }


}