/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class CameraActivity extends Activity implements Camera2BasicFragment.OnCameraCaptureListener {

    private static final int REQUEST_IMAGE_CAPTURE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance(), "camera")
                    .commit();
        }


    }

    @Override
    public void onReturnCameraCapture(File mFile) {
        Toast.makeText(CameraActivity.this, "File saved: " + mFile, Toast.LENGTH_SHORT).show();
        unpopFragment(getFragmentManager().findFragmentByTag("camera"));

        // Retorno de la camara al fragment activity principal
        Intent resultIntent = new Intent();
        Bundle data = new Bundle();
        resultIntent.putExtra("imageFile", mFile);
        setResult(REQUEST_IMAGE_CAPTURE, resultIntent);
        finish();
    }

    public void popFragment(int idContenedor, Fragment pFragment) {
        FragmentTransaction myTrans = getFragmentManager().beginTransaction();
        myTrans.replace(idContenedor, pFragment);
        myTrans.commit();
    }

    public void unpopFragment(Fragment pFragment) {
        FragmentTransaction myTrans = getFragmentManager().beginTransaction();
        myTrans.remove(pFragment).commit();
    }


}