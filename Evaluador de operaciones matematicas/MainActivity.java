package com.example.escanermult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    TextView textView2;
    CameraSource cameraSource;
    ImageButton swapButton;
    RelativeLayout layout;

    private static final int requestpermissionID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.detectedTextView);
        textView2 = findViewById(R.id.resultTextView);
        swapButton = findViewById(R.id.swapButton);
        layout = findViewById(R.id.relativeLayout);

        swapButton.setOnClickListener(view -> {
            if (cameraView.getVisibility() == View.VISIBLE) {
                cameraSource.stop();
                cameraView.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
            } else {
                cameraView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            textView.setText("");

        });

        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestpermissionID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, "NO hay permisos",Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraSource(){
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if(textRecognizer.isOperational()){
            cameraSource= new CameraSource.Builder(getApplicationContext(),textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280,1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(4.0f)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    try{
                        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CALL_COMPANION_APP)!=PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},requestpermissionID);
                        }
                        cameraSource.start(cameraView.getHolder());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        textView.post(() -> {
                            try {
                                String detectedText = items.valueAt(0).getValue();

                                // Muestra el texto detectado en el TextView correspondiente
                                textView.setText(detectedText);

                                // Busca el signo de igual en el texto detectado
                                if (detectedText.contains("=")) {
                                    // Separa la expresión antes y después del signo de igual
                                    String[] parts = detectedText.split("=");
                                    String expression = parts[0].trim();
                                    String expectedValue = parts[1].trim();

                                    // Realiza la evaluación normal
                                    String result = new MathEvaluation(expression).parse();

                                    // Formatea el resultado para no mostrar ".0" en los números enteros
                                    result = result.endsWith(".0") ? result.substring(0, result.length() - 2) : result;

                                    // Si el resultado coincide con el número después del igual, es correcto
                                    boolean isCorrect = result.equals(expectedValue);

                                    // Muestra información adicional en el TextView de resultados
                                    textView2.setText("Antes: " + expression + "\nDespués: " + expectedValue +
                                            "\nResultado: " + result + (isCorrect ? " (Correcto)" : " (Incorrecto)"));
                                } else {
                                    // Si no se encuentra el signo de igual, muestra un mensaje indicando que no se evaluó
                                    textView2.setText("Expresión no evaluada");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });

        }
    }
}