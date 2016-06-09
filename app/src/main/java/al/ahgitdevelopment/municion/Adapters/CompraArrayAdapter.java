package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 28/05/2016.
 */
public class CompraArrayAdapter extends ArrayAdapter<Compra> {
    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private Context context;
    private String mCurrentPhotoPath;

    public CompraArrayAdapter(Context context, int resource, List<Compra> compras) {
        super(context, resource, compras);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Compra compra = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.compra_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        ImageView imagen = (ImageView) convertView.findViewById(R.id.imageMunicion);
        TextView calibre = (TextView) convertView.findViewById(R.id.item_calibre_compra);
        TextView unidades = (TextView) convertView.findViewById(R.id.item_unidades_compra);
        TextView precio = (TextView) convertView.findViewById(R.id.item_precio_compra);

        if (compra.getImagen() != null)
            imagen.setImageBitmap(compra.getImagen());
        else
            imagen.setImageResource(R.drawable.municion1);

        if (compra.getCalibre2() == null || "".equals(compra.getCalibre2()))
            calibre.setText(compra.getCalibre1());
        else
            calibre.setText(compra.getCalibre1() + " / " + compra.getCalibre2());

        unidades.setText(compra.getUnidades() + "");
        precio.setText(String.format("%.2f€", compra.getPrecio()));

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(position);
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }

    private void dispatchTakePictureIntent(int position) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                FragmentMainActivity.imagePosition = position;
                ((AppCompatActivity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(context.getFilesDir() + File.separator + "Armas");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}