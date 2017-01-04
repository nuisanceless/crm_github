package com.drugoogle.sellscrm.visit;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.ImageDecodeType;
import com.drugoogle.sellscrm.Utils.ImageUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.CommonDialog;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Lei on 2016/5/26.
 */
@EActivity(R.layout.activity_delete_image)
public class ViewImageActivity extends BaseActivity {
    @Extra
    Uri imageUri;

    @Extra
    boolean canDelete = false;

    @ViewById(R.id.image)
    SimpleDraweeView imageView;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    Bitmap image;

    @Override
    protected void onDestroy() {
        if (image != null)
        {
            image.recycle();
            image = null;
        }
        super.onDestroy();
    }

    @AfterViews
    void afterViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
        hierarchy.setProgressBarImage(new ProgressBarDrawable());
        if (imageUri.getScheme().equals("file"))
        {
            image = ImageUtils.decodeBitmap(imageUri.getPath(), ImageDecodeType.SIZE_FIT_IN, 600, 600, Bitmap.Config.ARGB_8888, null, null);
            if (image != null)
            {
                imageView.setImageBitmap(image);
            }
        }
        else
        {
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (canDelete)
            getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                final CommonDialog dialog = new CommonDialog(ViewImageActivity.this);
                dialog.builder().setOnConfirmClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).setContent(getString(R.string.confirm_to_delete)).show();
                break;
        }
        return true;
    }
}
