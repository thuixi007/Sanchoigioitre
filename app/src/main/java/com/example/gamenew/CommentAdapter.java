package com.example.gamenew;

import android.content.Context;

import android.net.Uri;
import android.text.format.DateFormat;
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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mData;

    DatabaseReference CSDL = FirebaseDatabase.getInstance(Firebase_Setup.Link_conn).getReference();
    StorageReference IMG_CSDL = FirebaseStorage.getInstance(Firebase_Setup.Link_storage).getReference();


    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment,parent,false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        //Glide.with(mContext).load(mData.get(position).getUimg()).into(holder.img_user);
        //holder.tv_name.setText(mData.get(position).getUid());
        set_user_Name_txt(holder, mData.get(position).getUid() );

        holder.tv_content.setText(mData.get(position).getContent());
        holder.tv_date.setText(timestampToString((Long)mData.get(position).getTimestamp()));

        set_avatar(mData.get(position).getUid(), holder);

    }

    private void set_user_Name_txt(CommentViewHolder holder, String tentaikhoan) {

        CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.tv_name.setText(snapshot.child(tentaikhoan).child("full_name").getValue(String.class) );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText( mContext, "Có lỗi khi tải tên tài khoản",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void set_avatar(String tentaikhoan, CommentViewHolder holder) {
        ////Lấy tên avatar

        CSDL.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avt_name = snapshot.child(tentaikhoan).child("avatar_file").getValue(String.class);
                start_set_avt(tentaikhoan,avt_name, holder);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void start_set_avt(String tentaikhoan, String avt_name, CommentViewHolder holder) {

        IMG_CSDL.child("avatar").child(tentaikhoan).child(avt_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString() ).into(holder.img_user);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        ImageView img_user;
        TextView tv_name,tv_content,tv_date;

        public CommentViewHolder(View itemView) {
            super(itemView);
            img_user = itemView.findViewById(R.id.comment_user_img);
            tv_name = itemView.findViewById(R.id.comment_username);
            tv_content = itemView.findViewById(R.id.comment_content);
            tv_date = itemView.findViewById(R.id.comment_date);
        }
    }



    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm",calendar).toString();
        return date;


    }


}
