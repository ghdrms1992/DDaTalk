package com.example.acer.login.Profile_Tab.MyPage_Related;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.acer.login.BuildConfig;
import com.example.acer.login.Login_Related.LoginActivity;
import com.example.acer.login.Login_Related.SharedPrefManager;
import com.example.acer.login.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.example.acer.login.R.id.textView7;

public class MyPage_Fragment_Sub extends Fragment {

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private static final int MULTIPLE_PERMISSIONS = 101;

    private Uri mImageCaptureUri;
    TextView nameView,mtextView1, mtextView2, mtextView3;
    String name, birthday, email, absolutepath, userimg, userimg2;

    String HttpUrl = "http://104.198.211.126/insertUserimgUri.php";
    String HttpUrl2 = "http://104.198.211.126/getUserimgUri.php";
    String HttpUrl3 = "http://104.198.211.126/deleteuser.php";

    private ImageLoader mImageLoader;

    Bitmap bm;

    NetworkImageView user_profile;
    ImageButton photo_btn;

    ProgressDialog progressDialog;
    RequestQueue requestQueue, queue;

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        checkPermissions();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_mypage_sub, container, false);


        //발리 준비작업
        requestQueue = Volley.newRequestQueue(rootView.getContext());
        queue = Volley.newRequestQueue(rootView.getContext());
        progressDialog = new ProgressDialog(rootView.getContext());

        photo_btn = (ImageButton)rootView.findViewById(R.id.photoButton);
        nameView = (TextView)rootView.findViewById(R.id.textView);
        mtextView1 = (TextView)rootView.findViewById(textView7);
        mtextView2 = (TextView)rootView.findViewById(R.id.textView9);
        mtextView3 = (TextView)rootView.findViewById(R.id.textView11);

        user_profile = (NetworkImageView)rootView.findViewById(R.id.user_profile);
        //user_profile = (ImageView)rootView.findViewById(R.id.user_profile);

        name = SharedPrefManager.getInstance(getActivity().getApplicationContext()).getUsername();
        birthday = SharedPrefManager.getInstance(getActivity().getApplicationContext()).getUserBirthday();
        email = SharedPrefManager.getInstance(getActivity().getApplicationContext()).getUserEmail();


        nameView.setText(name);
        mtextView1.setText(name);
        mtextView2.setText(birthday);
        mtextView3.setText(email);


        ImageButton deleteButton = (ImageButton)rootView.findViewById(R.id.deleteButton);
        ImageButton logoutButton = (ImageButton)rootView.findViewById(R.id.logoutButton);
        ImageButton photoButton = (ImageButton)rootView.findViewById(R.id.photoButton);

        //유저 이미지 가져오기 실행
        ReceiveImg();
        user_profile.setImageUrl("http://104.198.211.126/getUserimgUri.php?email="+email, mImageLoader);




        //카메라 버튼 누르면
        photoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakePhotoAction();
                    }
                };

                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        doTakeAlbumAction();
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(getContext())
                        .setTitle("업로드할 이미지 선택")
                        // .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택",albumListener)
                        .setNegativeButton("취소",cancelListener)
                        .show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefManager.getInstance(getActivity().getApplicationContext()).logout();
                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
                Toast.makeText(rootView.getContext(), "회원탈퇴에 성공했습니다.", Toast.LENGTH_LONG).show();
                SharedPrefManager.getInstance(getActivity().getApplicationContext()).logout();
                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            }
        });


        return rootView;
    }


    public void doTakePhotoAction() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            mImageCaptureUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),BuildConfig.APPLICATION_ID+".provider", photoFile);



            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri); //사진을 찍어 해당 Content uri를 photoUri에 적용시키기 위함
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "IP" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/DDaTalk/"); //DDaTalk이라는 경로에 이미지 저장.
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public void doTakeAlbumAction()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode != RESULT_OK)
            return;

        switch (requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                try{
                    mImageCaptureUri = data.getData();
                    bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), mImageCaptureUri);

                    Log.d("ddaTalk", mImageCaptureUri.getPath().toString());
                }
                catch (Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), "앨범선택시에러", Toast.LENGTH_LONG).show();
                }

            }
            case PICK_FROM_CAMERA:
            {

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(mImageCaptureUri, "image/*");
                intent.putExtra("outputX",200);
                intent.putExtra("outputY",200);
                intent.putExtra("aspectX",1);
                intent.putExtra("aspectY",1);
                intent.putExtra("scale",true);
                intent.putExtra("return-data",true);
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }
            case CROP_FROM_IMAGE:
            {
                if(resultCode != RESULT_OK)
                {
                    return;
                }

                final Bundle extras = data.getExtras();

                //crop된 이미지를 저장하기 위한 file경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DDaTalk/"+System.currentTimeMillis()+".jpg";

                if(extras != null)
                {

                    Bitmap photo = extras.getParcelable("data");
                    userimg2 = getStringImage(photo);
                    SendImg(userimg2);
                    //   user_profile.setImageBitmap(photo);

                    storeCropImage(photo, filePath);
                    absolutepath = filePath;
                    break;
                }

                userimg = getStringImage(bm);
                SendImg(userimg);








                File f = new File(mImageCaptureUri.getPath());
                if(f.exists())
                {
                    f.delete();
                }
            }
        }
    }

    private void storeCropImage(Bitmap bitmap, String filePath){
        //DDaTalk 폴더를 생성하여 이미지를 저장하는방식이다.
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DDaTalk/";
        File directory_DDaTalk = new File(dirPath);

        if(!directory_DDaTalk.exists()){
            directory_DDaTalk.mkdir();

            File copyFile = new File(filePath);
            BufferedOutputStream out = null;

            try{
                copyFile.createNewFile();
                out = new BufferedOutputStream(new FileOutputStream(copyFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                getActivity().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

                out.flush();
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            File copyFile = new File(filePath);
            BufferedOutputStream out = null;

            try{
                copyFile.createNewFile();
                out = new BufferedOutputStream(new FileOutputStream(copyFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                getActivity().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

                out.flush();
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }

    //권한요청
    public boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(getContext(), pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(getActivity(), permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(getContext(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
    }


    //디비에 유저이미지 저장하기 메소드
    public void SendImg(final String userimg) {

        requestQueue = Volley.newRequestQueue(getContext());
        StringRequest request = new StringRequest(Request.Method.POST, HttpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("email",email);
                parameters.put("imgUri", userimg);
                return parameters;
            }
        };
        requestQueue.add(request);


    }

    //이미지로더




    //디비에서 유저이미지 가져오기 메소드
    public void ReceiveImg(){
        mImageLoader = VolleySingleton.getInstance(getContext()).getImageLoader();

        queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //서버에서 가져온 이미지 셋팅
                // Bitmap myBitmap = BitmapFactory.decodeFile(userimg);
                //      user_profile.setImageBitmap(myBitmap);



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Something went wrong",Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String, String>();
                parameters.put("email", email);
                return parameters;
            }
        };
        queue.add(stringRequest);
    }

    //이미지 셋팅작업
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }



    public void delete() {

        requestQueue = Volley.newRequestQueue(getContext());
        StringRequest request = new StringRequest(Request.Method.POST, HttpUrl3, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("email",email);
                return parameters;
            }
        };
        requestQueue.add(request);
    }
}