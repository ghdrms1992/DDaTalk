package com.example.acer.login.Profile_Tab;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.acer.login.Login_Related.Constants;
import com.example.acer.login.Profile_Tab.Home_Related.TogetherItem;
import com.example.acer.login.Profile_Tab.Home_Related.TogetherItemAdapter;
import com.example.acer.login.Profile_Tab.Home_Related.Writing;
import com.example.acer.login.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class Home_Fragment extends Fragment {


    RequestQueue rq;
    String content, date, email,rental_spot,imgPath;
    int reply_cnt, with_cnt, writing_no;

    int get_writing_no, get_reply_cnt;

    EditText editTextSearch;
    ProgressDialog progressDialog;

    //real time
    Handler handler;
    Timer timerMTimer;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);


        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getActivity().getApplication(), "Refresh success", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        final TogetherItemAdapter adapter = new TogetherItemAdapter();
        final ListView together2 = (ListView) rootView.findViewById(R.id.together2);
        progressDialog = new ProgressDialog(rootView.getContext());
        rq = Volley.newRequestQueue(getActivity());

    //    ReceiveImg();

        progressDialog.setMessage("로딩중.. 좀만 기둘려주떼염");
        progressDialog.show();
        handler = new Handler();
        timerMTimer = new Timer(true);
        timerMTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        final JsonArrayRequest jsonArrayRequest;
                        jsonArrayRequest = new JsonArrayRequest(Constants.URL_WRITING_INFO, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                progressDialog.dismiss();
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject obj = response.getJSONObject(i);
                                        content = obj.getString("content");
                                        date = obj.getString("date");
                                        email = obj.getString("email");
                                        reply_cnt = obj.getInt("reply_cnt");
                                        with_cnt = obj.getInt("with_cnt");
                                        writing_no = obj.getInt("writing_no");
                                        rental_spot = obj.getString("rental_spot");
                                        imgPath = obj.getString("userimg");
                                        byte [] encodeByte= Base64.decode(imgPath,Base64.DEFAULT);

                                        InputStream inputStream  = new ByteArrayInputStream(encodeByte);
                                        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
                                        Writing w = new Writing(content, reply_cnt, with_cnt, date, writing_no, email,rental_spot);


                                        TogetherItem togetherItem = new TogetherItem(w.getWriting_no(), w.getEmail(), w.getContent(), w.getDate(), bitmap,
                                                w.getWith_cnt(), w.getReply_cnt(), w.getRental_spot());

                                        adapter.addItem(togetherItem);
                                        together2.setAdapter(adapter);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        rq.add(jsonArrayRequest);
                    }
                });
            }
        },0,60000*4);

        Bundle extra = getArguments();
        if(extra != null) {
            get_writing_no = extra.getInt("writing_no");
            get_reply_cnt = extra.getInt("reply_cnt");
            adapter.replaceItem(get_writing_no,get_reply_cnt);
        }

        editTextSearch = (EditText)rootView.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editTextSearch.getText().toString().trim();
                adapter.filter(text);
            }
        });

        return rootView;
    }




}
