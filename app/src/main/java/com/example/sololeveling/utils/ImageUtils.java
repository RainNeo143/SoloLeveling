package com.example.sololeveling.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.example.sololeveling.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Безопасная загрузка аватара в ImageView
     */
    public static void loadAvatar(Context context, String avatarPath, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }

        // Если путь пустой, показываем дефолтную аватарку
        if (avatarPath == null || avatarPath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_avatar);
            return;
        }

        try {
            // Пытаемся загрузить изображение
            Uri uri = Uri.parse(avatarPath);

            // Проверяем, существует ли файл
            File file = new File(avatarPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(avatarPath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            }

            // Если файл не существует, пытаемся загрузить через URI
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            }
        } catch (Exception e) {
            // Если что-то пошло не так, показываем дефолтную аватарку
            e.printStackTrace();
        }

        // По умолчанию показываем дефолтную аватарку
        imageView.setImageResource(R.drawable.ic_avatar);
    }

    /**
     * Сохранение изображения во внутреннее хранилище
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri, int userId) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                return null;
            }

            // Сохраняем во внутреннее хранилище
            File directory = context.getFilesDir();
            File imageFile = new File(directory, "avatar_" + userId + ".jpg");

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение Bitmap из пути
     */
    public static Bitmap getBitmapFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            File file = new File(path);
            if (file.exists()) {
                return BitmapFactory.decodeFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}