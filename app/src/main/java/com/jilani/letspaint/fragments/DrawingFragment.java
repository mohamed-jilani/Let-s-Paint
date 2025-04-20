package com.jilani.letspaint.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.fragment.app.Fragment;

import com.jilani.letspaint.CustomView.PaintView;
import com.jilani.letspaint.R;
import com.jilani.letspaint.models.ToolType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawingFragment extends Fragment {

    public DrawingFragment() {
        // Obligatoire constructeur vide
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawing, container, false);
        PaintView paintView = view.findViewById(R.id.paintView);

        // Gestion des outils
        view.findViewById(R.id.brushToolBtn).setOnClickListener(v -> paintView.setToolType(ToolType.BRUSH));
        view.findViewById(R.id.pencilToolBtn).setOnClickListener(v -> paintView.setToolType(ToolType.PENCIL));
        view.findViewById(R.id.forkToolBtn).setOnClickListener(v -> paintView.setToolType(ToolType.FORK));

        ImageButton backgroundColorBtn = view.findViewById(R.id.backgroundColorBtn);
        backgroundColorBtn.setOnClickListener(v -> {
            AmbilWarnaDialog colorDialog = new AmbilWarnaDialog(
                    requireContext(),
                    Color.WHITE, // couleur initiale (ou paintView.getBackgroundColor() si tu l'ajoutes)
                    new AmbilWarnaDialog.OnAmbilWarnaListener() {
                        @Override
                        public void onOk(AmbilWarnaDialog dialog, int color) {
                            paintView.setBackgroundColor(color); // changement ici
                        }

                        @Override
                        public void onCancel(AmbilWarnaDialog dialog) {
                            // Rien
                        }
                    }
            );
            colorDialog.show();
        });

        ImageButton sizePickerBtn = view.findViewById(R.id.sizePickerBtn);

        sizePickerBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_brush_size, null);
            builder.setView(dialogView);
            builder.setTitle("Choisir la taille du trait");

            TextView sizeLabel = dialogView.findViewById(R.id.sizeLabel);
            SeekBar sizeSeekBar = dialogView.findViewById(R.id.sizeSeekBar);

            // Valeur actuelle du trait
            int currentSize = paintView.getBrushSize();
            sizeSeekBar.setProgress(currentSize);
            sizeLabel.setText("Taille du trait : " + currentSize);

            sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    sizeLabel.setText("Taille du trait : " + progress);
                    paintView.setBrushSize(progress);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            builder.setPositiveButton("OK", null);
            builder.setNegativeButton("Annuler", null);
            builder.show();
        });

        ImageButton saveBtn = view.findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(v -> {
            Bitmap bitmap = paintView.exportToBitmap(); // méthode à créer
            String filename = "LetsPaint_" + System.currentTimeMillis() + ".png";

            OutputStream outputStream;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LetsPaint");

                    ContentResolver resolver = requireActivity().getContentResolver();
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    outputStream = resolver.openOutputStream(imageUri);
                } else {
                    String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    File image = new File(imagesDir, filename);
                    outputStream = new FileOutputStream(image);
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(getContext(), "Image sauvegardée avec succès", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton eraserToolBtn = view.findViewById(R.id.eraserToolBtn);

        eraserToolBtn.setOnClickListener(v -> {
            int backgroundColor = paintView.getBackgroundColor();
            paintView.setColor(backgroundColor);
        });



        // Choix de la couleur
        ImageButton colorBtn = view.findViewById(R.id.colorPickerBtn);
        View currentColorView = view.findViewById(R.id.currentColorView);
        colorBtn.setOnClickListener(v -> {
            // Simple color picker dialog (on pourra faire mieux après)
            new AmbilWarnaDialog(requireContext(), paintView.getColor(), new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    paintView.setColor(color);
                    currentColorView.setBackgroundColor(color);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {}
            }).show();
        });

        // Effacer
        view.findViewById(R.id.clearBtn).setOnClickListener(v -> paintView.clear());

        // TODO : taille du trait (popup/dialog plus tard)

        return view;
    }

}
