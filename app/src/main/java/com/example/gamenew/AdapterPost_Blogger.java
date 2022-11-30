package com.example.gamenew;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterPost_Blogger extends RecyclerView.Adapter<AdapterPost_Blogger.HolderPost>{

    private Context context;
    private ArrayList<MoHinhBaiDang> postArrayList;

    /// Tạo hàm dựng

    public AdapterPost_Blogger(Context context, ArrayList<MoHinhBaiDang> postArrayList) {
        this.context = context;
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public HolderPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /// Dựng layout cho Row_post
        View view = LayoutInflater.from(context).inflate(R.layout.row_post_blogger, parent , false);
        return new HolderPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPost holder, int position) {
    /// Lấy về Data, Set Data, Xử lí các nút click

    MoHinhBaiDang mohinh = postArrayList.get(position); /// Lấy data tại vị trí được chọn

        /// Lấy data
    String Author = mohinh.getAuthor();
    String content = mohinh.getContent();
    String ID = mohinh.getId();
    String Published = mohinh.getPublished();
    String selfLink = mohinh.getSelfLink();
    String Title = mohinh.getTitle();
    String Updated = mohinh.getUpdated();
    String Url = mohinh.getUrl();

    /// Xử lý Json lấy trên API của trang Blogger

        /// Gọi Jsoup để xử lý HTML
        Document document = Jsoup.parse(content);
        try{
            Elements elements = document.select("img");
            String image = elements.get(0).attr("src");
            Picasso.get().load(image).placeholder(R.drawable.ic_image).into(holder.imageTV);
        }catch (Exception e){
            holder.imageTV.setImageResource(R.drawable.ic_image);
        }

        /// Định dạng ngày đăng
        String GTMdate = Published;
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/mm/yyyy K:mm a");
        String formatteddate ="";

        try{
            Date date = dateFormat1.parse(GTMdate);
            formatteddate = dateFormat2.format(date);

        }catch (Exception e){
            formatteddate = Published;
            e.printStackTrace();
        }

        ///Set Tiêu đề, nội dung
        holder.titleTV.setText("");
        holder.descriptionTV.setText(Title );
        holder.publishInfoTV.setText(Author + " " + formatteddate);

        /// Xem chi tiết bài viết :3
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /// Trả về intent cho blog_show

                Intent intent = new Intent( context, Post_Details_Blogger.class);
                intent.putExtra("postID" , ID ) ; // Tạo key - val
                context.startActivity(intent);

            }
        });






    }

    @Override
    public int getItemCount() {
        return postArrayList.size(); // Trả về số lượng bài đăng, kích thước của danh sách :(
    }


    /// hiển thị lên Row_post.xml

    class HolderPost extends RecyclerView.ViewHolder{

        /// Ánh xạ các thể loại từ row_post.xml

        ImageButton xemthem_btn;
        TextView titleTV, publishInfoTV, descriptionTV, xem_chi_tiet;
        ImageView imageTV;

        public HolderPost(@NonNull View itemView) {
            super(itemView);

            /// Tiến hành ánh xạ các thể loại
            xemthem_btn = itemView.findViewById(R.id.btn_xemthem);
            titleTV = itemView.findViewById(R.id.titleTV);
            publishInfoTV = itemView.findViewById(R.id.publishInfoTV);
            imageTV = itemView.findViewById(R.id.imageTV);
            descriptionTV = itemView.findViewById(R.id.descriptionTV);


        }
    }

}
