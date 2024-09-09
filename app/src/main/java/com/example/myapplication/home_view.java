package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class home_view extends AppCompatActivity {
    private ImageView imageViewScreens1;
    private  ImageView imageViewScreens2;
    private boolean isConnected1 = false;
    private boolean isConnected2=false;

    private Socket socket;
    private Socket socket2;
    private OutputStream outputStream;

    private EditText editText1;
    private EditText editTextIP1;
    private Button buttonConnect1;
    private TextView txtUser1;
    private ImageView imgLogo1;
    private FrameLayout frmStateConnect1;
    private  FrameLayout frmStateConnect2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageViewScreens1=findViewById(R.id.imageViewScreens1);
        imageViewScreens2 = findViewById(R.id.imageViewScreens2);

        imgLogo1 = findViewById(R.id.imgLogo1);
        txtUser1 = findViewById(R.id.txtUser1);

        imageViewScreens1.setImageResource(R.drawable.bgforimgview);
        imageViewScreens2.setImageResource(R.drawable.bgforimgview);

        frmStateConnect1 = findViewById(R.id.frmStateConnect1);
        frmStateConnect2 = findViewById(R.id.frmStateConnect2);


        if(!isConnected1){
            frmStateConnect1.setBackgroundResource(R.drawable.rounnded_full_frame_dv);
        }
        else{
            frmStateConnect1.setBackgroundResource(R.drawable.rounnded_full_frame_dv_true);
        }

        if(!isConnected2){
            frmStateConnect2.setBackgroundResource(R.drawable.rounnded_full_frame_dv);
        }
        else{
            frmStateConnect1.setBackgroundResource(R.drawable.rounnded_full_frame_dv_true);
        }

        imageViewScreens1.setOnClickListener(v -> {
            if(isConnected1 == false){
                showInputDialog();
            }else{
                Intent intent = new Intent(home_view.this,show_detail_viewscr.class);
                startActivity(intent);
            }
        });
    }

    private void showInputDialog() {
        // Tạo một AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Gắn giao diện tùy chỉnh cho hộp thoại
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        // Lấy tham chiếu đến các EditText và Button trong layout tùy chỉnh
        EditText editText1 = dialogView.findViewById(R.id.editText1);
        EditText editTextIP1 = dialogView.findViewById(R.id.editTextIP1);
        Button buttonConnect1 = dialogView.findViewById(R.id.buttonConnect1);

        // Tạo và hiển thị hộp thoại
        AlertDialog dialog = builder.create();
        dialog.show();

        // Thiết lập sự kiện cho nút bấm
        buttonConnect1.setOnClickListener(view -> {
            String input1 = editText1.getText().toString().trim();
            String input2 = editTextIP1.getText().toString().trim();
            // Xử lý giá trị nhập vào nếu cần
            if(isConnected1 == false){
                Log.d("test",input2);
                new ConnectTask().execute(input2);
                txtUser1.setText(input2);
                imgLogo1.setImageResource(R.drawable.lgn);
            }
            else{
                disconnect();
            }

            dialog.dismiss();
        });
    }

    private class ConnectTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                socket = new Socket(params[0], 5000);
                isConnected1 = true;
                frmStateConnect1.setBackgroundResource(R.drawable.rounnded_full_frame_dv_true);
                outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                while (isConnected1) {
                    // Nhận kích thước của ảnh
                    byte[] sizeBuffer = new byte[4];
                    if (inputStream.read(sizeBuffer, 0, 4) != 4) break;

                    int size = ((sizeBuffer[0] & 0xFF) << 24) | ((sizeBuffer[1] & 0xFF) << 16)
                            | ((sizeBuffer[2] & 0xFF) << 8) | (sizeBuffer[3] & 0xFF);

                    // Nhận dữ liệu ảnh
                    byte[] imageBuffer = new byte[size];
                    int bytesRead = 0;
                    while (bytesRead < size) {
                        int read = inputStream.read(imageBuffer, bytesRead, size - bytesRead);
                        if (read == -1) break;
                        bytesRead += read;
                    }

                    if (bytesRead != size) break;

                    // Chuyển đổi dữ liệu ảnh thành Bitmap và hiển thị lên ImageView
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBuffer, 0, size);
                    runOnUiThread(() -> {
                        imageViewScreens1.setImageBitmap(bitmap);
                        imageViewScreens1.setBackgroundColor(0); // Xóa màu nền khi hiển thị hình ảnh
                    });
                }
                return true;
            } catch (Exception e) {
                Log.e("ConnectTask", "Lỗi khi kết nối: " + e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(home_view.this, "Kết nối thành công", Toast.LENGTH_SHORT).show();
                buttonConnect1.setText("Ngắt kết nối");
//                updateButtonStates(true);
            } else {
                Toast.makeText(home_view.this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void disconnect() {
        try {
            if (socket != null) {
                isConnected1 = false;
                socket.close();
                imageViewScreens1.setImageBitmap(null); // Xóa hình ảnh khi ngắt kết nối
                imageViewScreens1.setBackgroundColor(getResources().getColor(android.R.color.black)); // Đặt lại nền đen
                buttonConnect1.setText("Kết nối");
//                updateButtonStates(false);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi đóng socket: " + e.getMessage(), e);
        }

        try{
            if (socket2 != null) {
                isConnected2 = false;
                socket2.close();
                imageViewScreens2.setImageBitmap(null); // Xóa hình ảnh khi ngắt kết nối
                imageViewScreens2.setBackgroundColor(getResources().getColor(android.R.color.black)); // Đặt lại nền đen
                buttonConnect1.setText("Kết nối");
//                updateButtonStates(false);
            }

        }catch (Exception e){
            Log.e("MainActivity", "Lỗi khi đóng socket: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                isConnected1 = false;
                socket.close();
            }
            if(socket2 != null){
                isConnected2 = false;
                socket2.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi đóng socket: " + e.getMessage(), e);
        }
    }
    /////
}
