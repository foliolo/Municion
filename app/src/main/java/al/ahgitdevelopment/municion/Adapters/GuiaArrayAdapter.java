package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import al.ahgitdevelopment.municion.CameraActivity;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 28/05/2016.
 */
public class GuiaArrayAdapter extends ArrayAdapter<Guia> {
    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private Context context;
    private String mCurrentPhotoPath;

    public GuiaArrayAdapter(Context context, int resource, List<Guia> guias) {
        super(context, resource, guias);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Guia guia = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guia_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        ImageView imagen = (ImageView) convertView.findViewById(R.id.imageArma);
        TextView apodo = (TextView) convertView.findViewById(R.id.item_apodo_guia);
        TextView numGuia = (TextView) convertView.findViewById(R.id.item_form_num_guia);
        TextView cupo = (TextView) convertView.findViewById(R.id.item_cupo_guia);
        TextView gastado = (TextView) convertView.findViewById(R.id.item_gastados_guia);

        if (guia.getImagen() != null)
            imagen.setImageBitmap(guia.getImagen());
        else
            imagen.setImageResource(R.drawable.pistola);
        apodo.setText(guia.getApodo());
        numGuia.setText(String.valueOf(guia.getNumGuia()));
        cupo.setText(guia.getGastado() + " / " + guia.getCupo());
        gastado.setText(String.format("%.2f", (1.0 * guia.getGastado() / guia.getCupo() * 100)) + "%");

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if()
                dispatchTakePictureIntent(position);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    private void dispatchTakePictureIntent(int position) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        Intent takePictureIntent = new Intent(context, CameraActivity.class);

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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                FragmentMainActivity.imagePosition = position;
                ((AppCompatActivity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = context.getFilesDir() /*+ File.separator + "Armas")*/;

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

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
