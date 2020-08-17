package al.ahgitdevelopment.municion.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import al.ahgitdevelopment.municion.NavigationActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.datamodel.Guia;
import al.ahgitdevelopment.municion.sandbox.FragmentMainContent;
import al.ahgitdevelopment.municion.sandbox.Utils;

/**
 * Created by Alberto on 28/05/2016.
 */
public class GuiaArrayAdapter extends ArrayAdapter<Guia> {

    private Context context;

    public GuiaArrayAdapter(Context context, int resource, List<Guia> guias) {
        super(context, resource, guias);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Guia guia = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guia_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        ImageView imagen = convertView.findViewById(R.id.imageArma);
        TextView apodo = convertView.findViewById(R.id.item_apodo_guia);
        TextView numGuia = convertView.findViewById(R.id.item_form_num_guia);
        TextView cupo = convertView.findViewById(R.id.item_cupo_guia);
        TextView gastado = convertView.findViewById(R.id.item_gastados_guia);

//        if (guia.getImagePath() != null && !guia.getImagePath().equals("null")) {
//            Bitmap bitmap = BitmapFactory.decodeFile(guia.getImagePath());
//            imagen.setImageBitmap(Utils.resizeImage(bitmap, imagen));
//        } else {
//            imagen.setImageResource(Utils.getResourceWeapon(guia.getTipoLicencia(), guia.getTipoArma()));
//        }

        apodo.setText(guia.getApodo());
        numGuia.setText(guia.getNumGuia());
        cupo.setText(guia.getGastado() + " / " + guia.getCupo());
        gastado.setText(String.format("%.2f", (1.0 * guia.getGastado() / guia.getCupo() * 100)) + "%");

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentMainContent.imagePosition = position;

                // Creamos la vista del dialogo para mostrar la imagen
                View layout = ((NavigationActivity) context).getLayoutInflater().inflate(R.layout.dialog_image_view, null);
                if (guia.getImagePath() != null && !guia.getImagePath().equals("null")) {
                    ImageView imageViewDialog = layout.findViewById(R.id.image_view);
                    final Bitmap bitmap = Utils.resizeImage(BitmapFactory.decodeFile(guia.getImagePath()), imageViewDialog);
                    imageViewDialog.setImageBitmap(bitmap);

                    imageViewDialog.setOnClickListener(v1 -> Utils.showImage(context, guia.getImagePath()));
                }
//                else {
//                    ((ImageView) layout.findViewById(R.id.image_view)).setImageResource(
//                            Utils.getResourceWeapon(guia.getTipoLicencia(), guia.getTipoArma()));
//                }


                new AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setView(layout)
                        .setNeutralButton(R.string.change_image, (dialog, which) -> dispatchTakePictureIntent())
                        .setNegativeButton(R.string.delete_image, (dialog, which) -> {
                            guia.setImagePath(null);
                            notifyDataSetChanged();

//                            DataBaseSQLiteHelper dbSqlHelper = new DataBaseSQLiteHelper(context.getApplicationContext());
//                            dbSqlHelper.saveListGuias(null, FragmentMainContent.guias);
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(context.getPackageName(), "IOException");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "al.ahgitdevelopment.municion.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                ((AppCompatActivity) context).startActivityForResult(takePictureIntent, FragmentMainContent.REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File storageDir = context.getCacheDir() /*+ File.separator + "Armas")*/;
//        File storageDir = context.getFilesDir() /*+ File.separator + "Armas")*/;
//        File storageDir = context.getExternalCacheDir(); /*+ File.separator + "Armas")*/
        File storageDir;
        storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getFilesDir();
        }

        if (!storageDir.exists()) {
            storageDir.mkdir();
            Log.d(context.getPackageName(), "Directory created");
        } else
            Log.d(context.getPackageName(), "Directory exist");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
//        image.setWritable(true, false); //Da permisos para que otra aplicacion pueda escribir en el fichero temporal de la memoria cache reservada

        FragmentMainContent.fileImagePath = image.getAbsolutePath();
        return image;
    }
}
