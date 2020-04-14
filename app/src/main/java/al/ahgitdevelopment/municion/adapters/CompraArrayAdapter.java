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

import al.ahgitdevelopment.municion.FragmentMainContent;
import al.ahgitdevelopment.municion.NavigationActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
import al.ahgitdevelopment.municion.datamodel.Compra;
import al.ahgitdevelopment.municion.repository.DataBaseSQLiteHelper;

/**
 * Created by Alberto on 28/05/2016.
 */
public class CompraArrayAdapter extends ArrayAdapter<Compra> {
    private Context context;

    public CompraArrayAdapter(Context context, int resource, List<Compra> compras) {
        super(context, resource, compras);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Compra compra = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.compra_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        ImageView imagen = convertView.findViewById(R.id.imageMunicion);
        TextView calibre = convertView.findViewById(R.id.item_calibre_compra);
        TextView unidades = convertView.findViewById(R.id.item_unidades_compra);
        TextView precio = convertView.findViewById(R.id.item_precio_compra);
        TextView year = convertView.findViewById(R.id.item_year_compra);

//        if (compra.getImagePath() != null && !compra.getImagePath().equals("null")) {
//            imagen.setImageBitmap(BitmapFactory.decodeFile(compra.getImagePath()));
//        } else {
//            try {
//                imagen.setImageResource(Utils.getResourceCartucho(
//                        FragmentMainContent.guias.get(compra.getIdPosGuia()).getTipoLicencia(),
//                        FragmentMainContent.guias.get(compra.getIdPosGuia()).getTipoArma()));
//            } catch (Exception ex) {
//                Log.e(context.getPackageName(), "Fallo en la carga de imagen de los cartuchos");
//            }
//        }

        if (compra.getCalibre2() == null || "".equals(compra.getCalibre2()))
            calibre.setText(compra.getCalibre1());
        else
            calibre.setText(compra.getCalibre1() + " / " + compra.getCalibre2());

        unidades.setText(compra.getUnidades() + "");
        precio.setText(String.format("%.2f€", compra.getPrecio()));
        year.setText(compra.getFecha());

        imagen.setOnClickListener(v -> {
            FragmentMainContent.imagePosition = position;

            //Creamos la vista del dialogo para mostar la imagen
            View layout = ((NavigationActivity) context).getLayoutInflater().inflate(R.layout.dialog_image_view, null);
            if (compra.getImagePath() != null && !compra.getImagePath().equals("null")) {
                ImageView imageViewDialog = layout.findViewById(R.id.image_view);
                final Bitmap bitmap = Utils.resizeImage(BitmapFactory.decodeFile(compra.getImagePath()), imageViewDialog);
                imageViewDialog.setImageBitmap(bitmap);
                imageViewDialog.setOnClickListener(v1 -> Utils.showImage(context, compra.getImagePath()));
            }
//            else {
//                ((ImageView) layout.findViewById(R.id.image_view)).setImageResource(
//                        Utils.getResourceCartucho(
//                                FragmentMainContent.guias.get(compra.getIdPosGuia()).getTipoLicencia(),
//                                FragmentMainContent.guias.get(compra.getIdPosGuia()).getTipoArma()));
//            }

            new AlertDialog.Builder(context)
                    .setCancelable(true)
                    .setView(layout)
                    .setNeutralButton(R.string.change_image, (dialog, which) -> dispatchTakePictureIntent())
                    .setNegativeButton(R.string.delete_image, (dialog, which) -> {
                        compra.setImagePath(null);
                        notifyDataSetChanged();

                        DataBaseSQLiteHelper dbSqlHelper = new DataBaseSQLiteHelper(context.getApplicationContext());
                        dbSqlHelper.saveListCompras(null, FragmentMainContent.compras);
                    })
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
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
//        File storageDir = new File(context.getFilesDir() + File.separator + "Municion");
//        File storageDir = context.getFilesDir() /*+ File.separator + "Municion")*/;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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

//https://gist.github.com/Mariovc/f06e70ebe8ca52fbbbe2