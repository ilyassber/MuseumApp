package com.alpha.museum.museum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alpha.museum.museum.models.Category;
import com.alpha.museum.museum.models.Image;
import com.alpha.museum.museum.models.Monument;
import com.alpha.museum.museum.preference.ManagePreference;
import com.alpha.museum.museum.tools.StringFormat;
import com.ramotion.foldingcell.FoldingCell;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.alpha.museum.museum.MainActivity.TAG;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class FoldingCellListAdapter extends ArrayAdapter<Monument> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;
    private List<Monument> monuments;
    private Monument monument;
    private ViewHolder viewHolder;
    private Context context;

    public FoldingCellListAdapter(Context context, List<Monument> objects) {
        super(context, 0, objects);
        this.monuments = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // get item for selected view
        monument = monuments.get(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        if (cell == null) {
                viewHolder = new ViewHolder();
                LayoutInflater vi = LayoutInflater.from(getContext());
                cell = (FoldingCell) vi.inflate(R.layout.monument, parent, false);
                viewHolder.titleText1 = cell.findViewById(R.id.title_text_1);
                viewHolder.titleText2 = cell.findViewById(R.id.title_text_2);
                viewHolder.descriptionText1 = cell.findViewById(R.id.monument_description_1);
                viewHolder.descriptionText2 = cell.findViewById(R.id.monument_description_2);
                viewHolder.iconImage = cell.findViewById(R.id.icon_img);
                viewHolder.headImage = cell.findViewById(R.id.head_image);
                viewHolder.showMore = cell.findViewById(R.id.content_request_btn);
                cell.setTag(viewHolder);
                viewHolder.titleText1.setText(monument.getMonumentTitle());
                viewHolder.titleText2.setText(monument.getMonumentTitle());
                viewHolder.descriptionText1.setText(monument.getMonumentDescription());
                viewHolder.descriptionText2.setText(monument.getMonumentDescription());
                viewHolder.iconImage.setImageBitmap(monument.getImages().get(0).getImgBitmap());
                viewHolder.headImage.setImageBitmap(monument.getImages().get(0).getImgBitmap());
        } else {
            // for existing cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }
        return cell;
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView titleText1;
        TextView titleText2;
        TextView descriptionText1;
        TextView descriptionText2;
        ImageView iconImage;
        ImageView headImage;
        TextView showMore;
    }
}
