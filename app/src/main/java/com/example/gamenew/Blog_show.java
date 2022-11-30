package com.example.gamenew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Blog_show extends AppCompatActivity {

    /// Khai báo biến các nút và textbox,...
    private RecyclerView postRV;
    private Button Xem_Them_btn;
    private String Url = "";
    private String NextToken = "";
    private EditText search_edit;
    private ImageButton btn_search;
    private Boolean is_search = false ;
    private ImageButton btn_clear_search;

    private ArrayList<MoHinhBaiDang> postArrayList;
    private AdapterPost_Blogger adapterPostBlogger;

    private static final String TAG = "MAIN_TAG";

    private ProgressDialog progressDialog;

    private ActionBar actionBar;
    private long back_To_exit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_show);

        Runtime.getRuntime().gc();
        ///Lấy tên tài khoản để sử dụng các chức năng
        String tentaikhoan = getIntent().getStringExtra("tentaikhoan");


        //// Ánh xạ các thứ trên màn hình hiển thị
        postRV = findViewById(R.id.postRV);
        Xem_Them_btn = findViewById(R.id.btn_xemthem);
        search_edit = findViewById(R.id.searchEdit);
        btn_search = findViewById(R.id.btn_search);
        btn_clear_search = findViewById(R.id.btn_clearSearch);


        /// Tạo hiệu ứng đang tải bài viết
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Chờ xíu bạn nhé");
        progressDialog.setMessage("Đang tải bài viết");

        /// Clear arraylist Trước khi thêm data cho chắc ^^

        postArrayList = new ArrayList<>();
        postArrayList.clear();

        loadpost();

        /// Sửa thanh action bar;
        actionBar = getSupportActionBar();
        actionBar.setTitle("Bài viết mới nhất");
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        /// Clear focus của edittext tìm kiếm
        search_edit.clearFocus();


        //// Nút xem thêm
        Xem_Them_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// Tạo query truy vấn mới
                String query = search_edit.getText().toString().trim();

                if(TextUtils.isEmpty(query)){
                    loadpost();
                }else {
                    searchPost(query);
                }

            }
        });

        //// Nút tìm kiếm
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NextToken ="";
                Url = "";

                postArrayList= new ArrayList<>();
                postArrayList.clear();

                //// Tạo query truy vấn mới
                String query = search_edit.getText().toString().trim().replace(" ","+");

                if(TextUtils.isEmpty(query)){
                    loadpost();
                }else {
                    searchPost(query);
                }
                ClearKeyboard();


            }
        });

        /// nút clear tìm kiếm
        btn_clear_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NextToken ="";
                postArrayList= new ArrayList<>();
                postArrayList.clear();
                loadpost();
                search_edit.getText().clear();
                ClearKeyboard();


            }
        });

        ///Bottom nav
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setSelectedItemId(R.id.nav_news);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_news:
                        break;
                    case R.id.nav_forum:

                        startActivity(new Intent(Blog_show.this, Forum_Main.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        finish();
                        ActivityCompat.finishAffinity(Blog_show.this);
                        ///System.exit(0);

                        break;
                    case R.id.nav_follow:
                        finish();
                        startActivity(new Intent(Blog_show.this, activity_my_post.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_account:
                        finish();
                        startActivity(new Intent(Blog_show.this, activity_user_profile.class).putExtra("tentaikhoan",tentaikhoan));
                        overridePendingTransition(0,0);
                        break;
                }
                return true;
            }
        });


    }

    private void ClearKeyboard() {
        View view = this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    /////// Nhấn nhiều lần back để thoát
    @Override
    public void onBackPressed() {


        if (back_To_exit + 1000 >  System.currentTimeMillis() ){
            this.finish();
            System.exit(0);
            return;
        }else {
            Toast.makeText(this, "Bấm quay lại thêm lần nữa để thoát ứng dụng",Toast.LENGTH_SHORT).show();
        }
        back_To_exit = System.currentTimeMillis();

    }

    private void searchPost(String query) {
        is_search = true;
        Log.d(TAG, "searchPost: is search" +is_search);

        progressDialog.show();

        if (NextToken.equals("")){

            Url = "https://www.googleapis.com/blogger/v3/blogs/"
                    +Blogger_Setup.Blog_id
                    + "/posts/search?q="+ query
                    +"&key="+ Blogger_Setup.Api_key;
            Log.d(TAG, "searchpost: Bạn đã xem hết rồi"+Url);

        }else if (NextToken.equals("end")){
            Log.d(TAG, "searchpost: Hết bài rồi " + Url);
            Toast.makeText(this,"Bạn đã xem hết bài",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        else {
            Log.d(TAG, "loadpost: Next token: " + NextToken);
            Url = "https://www.googleapis.com/blogger/v3/blogs/"
                    + Blogger_Setup.Blog_id
                    + "/posts/search?q="+ query
                    + "&pageToken=" + NextToken
                    + "&key=" + Blogger_Setup.Api_key;
        }
        Log.d(TAG, "loadpost: URL" + Url);

        request_blogger(Url);
    }


    private void loadpost() {

        is_search = false;

        progressDialog.show();

        if (NextToken.equals("")){

            Url = "https://www.googleapis.com/blogger/v3/blogs/"
                    +Blogger_Setup.Blog_id+
                    "/posts?maxResults="+Blogger_Setup.maxResults+"&key="+ Blogger_Setup.Api_key;
            Log.d(TAG, "loadpost: Bạn đã xem hết rồi"+Url);

        }else if (NextToken.equals("end")){
            Log.d(TAG, "loadpost: Hết bài rồi " + Url);
            Toast.makeText(this,"Bạn đã xem hết bài",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        else {
            Log.d(TAG, "loadpost: Next token: " + NextToken);
            Url = "https://www.googleapis.com/blogger/v3/blogs/"
                    + Blogger_Setup.Blog_id
                    + "/posts?maxResults=" + Blogger_Setup.maxResults
                    + "&pageToken=" + NextToken
                    + "&key=" + Blogger_Setup.Api_key;
        }
            Log.d(TAG, "loadpost: URL" + Url);

        request_blogger(Url);

    }

    private void request_blogger(String url) {

        /// tạo string request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /// Yêu cầu dữ liệu///
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response);

                try{
                    /// Lấy về file json
                    JSONObject jsonObject = new JSONObject(response);
                    /// Phân tách giá trị file Json

                    try {
                        NextToken = jsonObject.getString("nextPageToken");
                        Log.d(TAG, "onResponse: nextpagetoken" + NextToken);

                    }catch (Exception e){
                        Toast.makeText(Blog_show.this, "Đã hết mất rồi :(",Toast.LENGTH_SHORT).show();
                        NextToken = "end";
                        Log.d(TAG, "onResponse: end of page" + e.getMessage());
                    }

                    // Lấy dữ liệu từ json bỏ vào array list để hiển thị

                    /// Lấy mảng json
                    JSONArray jsonArray = jsonObject.getJSONArray("items");

                    /// Dùng độ dài của mảng để tạo vòng lặp lấy các giá trị

                    for (int i = 0; i< jsonArray.length(); i++){

                        try {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.getString("id");
                            String title = jsonObject1.getString("title");
                            String content = jsonObject1.getString("content");
                            String published = jsonObject1.getString("published");
                            String updated = jsonObject1.getString("updated");
                            String url = jsonObject1.getString("url");
                            String selfLink = jsonObject1.getString("selfLink");
                            String author = jsonObject1.getJSONObject("author").getString("displayName");
                            //String image = jsonObject1.getJSONObject("author").getString("displayName");


                            ///Set data
                            MoHinhBaiDang moHinhBaiDang = new MoHinhBaiDang(
                                    ""+author,
                                    ""+content,
                                    ""+id,
                                    ""+published,
                                    ""+selfLink,
                                    ""+title,
                                    ""+updated,
                                    ""+url
                            );
                            postArrayList.add(moHinhBaiDang);

                        }catch (Exception e){
                            Toast.makeText(Blog_show.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onResponse: 1" + e.getMessage());
                        }


                    }
                    /// Set Adapter hiển thị
                    adapterPostBlogger = new AdapterPost_Blogger(Blog_show.this,postArrayList);
                    postRV.setAdapter(adapterPostBlogger);
                    progressDialog.dismiss();


                }
                catch (Exception e){
                    Toast.makeText(Blog_show.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Blog_show.this, ""+error.getMessage(),Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        });

        /// Thêm reque vào hàng đợi
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        Runtime.getRuntime().gc();
        super.onDestroy();
    }



}