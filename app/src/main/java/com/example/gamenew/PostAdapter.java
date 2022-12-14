package com.example.gamenew;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference();
    String avt_name;

    Context mContext;
    List<Post> mData ;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item,parent,false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.tvTitle.setText(mData.get(position).getTitle());


        ///Glide.with(mContext).load(mData.get(position).getPicture()).into(holder.imgPost);


       /// Glide.with(mContext).load(mData.get(position).getUserPhoto()).into(holder.imgPostProfile);

        //// Set img post
        IMG_CSDL.child("Posts").child(mData.get(position).getUserId()).child(mData.get(position).getPostKey()).child(mData.get(position).getPicture()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(holder.imgPost);
            }
        });


        //// Set img profile

        FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avt_name = snapshot.child(mData.get(position).getUserId()).child("avatar_file").getValue(String.class);
                start_set_avt(holder,mData.get(position).getUserId(),avt_name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        System.gc();


    }

    private void start_set_avt(MyViewHolder holder, String userId, String avt_name) {
        IMG_CSDL.child("avatar").child(userId).child(avt_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(holder.imgPostProfile);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imgPost;
        ImageView imgPostProfile;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.row_post_title);
            imgPost = itemView.findViewById(R.id.row_post_img);
            imgPostProfile = itemView.findViewById(R.id.row_post_profile_img);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent postDetailActivity = new Intent(mContext,PostDetailActivity.class);
                    int position = getAdapterPosition();



                   postDetailActivity.putExtra("title",mData.get(position).getTitle());
                    postDetailActivity.putExtra("postImage",mData.get(position).getPicture());
                    postDetailActivity.putExtra("description",mData.get(position).getDescription());
                    postDetailActivity.putExtra("postKey",mData.get(position).getPostKey());
                    postDetailActivity.putExtra("userPhoto",mData.get(position).getUserPhoto());

                    ///// Truy???n ???nh Postimage



                    ///postDetailActivity.putExtra("BitmapImage", viewBitmap);






                    ///// Truy???n ???nh ch??? b??i vi???t
                    imgPostProfile.buildDrawingCache();
                    Bitmap bmap = imgPostProfile.getDrawingCache();
                    postDetailActivity.putExtra("BitmapImage", bmap);




                    // will fix this later i forgot to add user name to post object
                    //postDetailActivity.putExtra("userName",mData.get(position).getUsername);
                    long timestamp  = (long) mData.get(position).getTimeStamp();
                    postDetailActivity.putExtra("postDate",timestamp);
                    System.gc();
                    mContext.startActivity(postDetailActivity);





                }
            });

        }


    }
}
