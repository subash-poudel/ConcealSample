package com.poudel.subash.concealsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.facebook.crypto.Entity;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  public static final int REQUEST_CODE_ASK_PERMISSIONS = 1001;
  public static final int REQUEST_CODE_MANUAL = 1002;

  Button button_select_image;
  ImageView iv_image_selected;
  Button btn_encrypt;
  Button btn_decrypt;
  ImageView iv_image_decrypted;
  private String imagePath;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    button_select_image = findViewById(R.id.button_select_image);
    iv_image_selected = findViewById(R.id.iv_image_selected);
    btn_encrypt = findViewById(R.id.btn_encrypt);
    btn_decrypt = findViewById(R.id.btn_decrypt);
    iv_image_decrypted = findViewById(R.id.iv_image_decrypted);
    button_select_image.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        galleryPermissionDialog();
      }
    });
    btn_encrypt.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        encryptImage(imagePath);
      }
    });
    btn_decrypt.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Bitmap bitmap = decryptAsBitmap("encrypt.txt");
        iv_image_decrypted.setImageBitmap(bitmap);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
    super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    switch (requestCode) {
      case 1:
        if (resultCode == RESULT_OK) {
          try {
            final Uri imageUri = imageReturnedIntent.getData();
                   /* final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    img_profile.setImageBitmap(selectedImage);*/
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();

            iv_image_selected.setImageBitmap(BitmapFactory.decodeFile(imagePath));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
    }
  }

  void openGallry() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, 1);
  }

  void galleryPermissionDialog() {

    int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,
        android.Manifest.permission.READ_EXTERNAL_STORAGE);
    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
          new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE },
          REQUEST_CODE_ASK_PERMISSIONS);
    } else {
      openGallry();
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CODE_ASK_PERMISSIONS: {
        Map<String, Integer> perms = new HashMap<>();
        // Initial
        perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            PackageManager.PERMISSION_GRANTED);
        // Fill with results
        for (int i = 0; i < permissions.length; i++)
          perms.put(permissions[i], grantResults[i]);
        // Check for READ_EXTERNAL_STORAGE

        boolean showRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED) {
            // All Permissions Granted
            galleryPermissionDialog();
          } else {
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (showRationale) {
              showMessageOKCancel("Read Storage Permission required for this app ",
                  new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                      galleryPermissionDialog();
                    }
                  });
            } else {
              showMessageOKCancel("Read Storage Permission required for this app ",
                  new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                      Toast.makeText(MainActivity.this,
                          "Please Enable the Read Storage permission in permission",
                          Toast.LENGTH_SHORT).show();
                      Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                      Uri uri = Uri.fromParts("package", getPackageName(), null);
                      intent.setData(uri);
                      startActivityForResult(intent, REQUEST_CODE_MANUAL);
                    }
                  });
            }
          }
        } else {
          galleryPermissionDialog();
        }
      }
      break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  public void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
    new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton("OK", okListener)
        .setNegativeButton("Cancel", null)
        .create()
        .show();
  }

  private File getExternalOutputFile(String filename) {
    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
    return file;
  }

  private void encryptImage(String imagePath) {
    try {

      OutputStream fileOutputStream =
          new BufferedOutputStream(new FileOutputStream(getExternalOutputFile("encrypt.txt")));
      FileInputStream fileInputStream = new FileInputStream(new File(imagePath));
      // Creates an output stream which encrypts the data as
      // it is written to it and writes it out to the file.
      OutputStream outputStream = CryptoUtil.get()
          .getCrypto()
          .getCipherOutputStream(fileOutputStream, Entity.create("entity_id"));

      int read;
      byte[] buffer = new byte[1024];
      while ((read = fileInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      // Write plaintext to it.
      fileInputStream.close();
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Bitmap decryptAsBitmap(String encryptedFilePath) {
    try {
      // Get the file to which ciphertext has been written.
      FileInputStream fileStream = new FileInputStream(getExternalOutputFile(encryptedFilePath));
      // Creates an input stream which decrypts the data as
      // it is read from it.
      InputStream inputStream =
          CryptoUtil.get().getCrypto().getCipherInputStream(fileStream, Entity.create("entity_id"));
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
      //inputStream.close();
      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
