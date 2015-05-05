package andrewhammer.hammeruberapplication;

/**
 * Created by andrewhammer on 5/3/15.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> imageUrls;

    public ImageAdapter(Context context, int resource, List<String> imageUrls) {
        super(context, resource, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        ViewHolder holder;
        if (cellView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            cellView = inflater.inflate(R.layout.image_grid_element, parent, false);

            holder = new ViewHolder();
            holder.imageView = (ImageView) cellView;
            cellView.setTag(holder);
        } else {
            holder = (ViewHolder) cellView.getTag();
        }

        String url = imageUrls.get(position);

        //way simpler than implementing custom bitmap loading w/ weakreferences and local caching - thanks picasso
        Picasso.with(context)
                .load(url)
                .into(holder.imageView);

        return cellView;
    }


    //uses the viewholder pattern to improve scroll speed
    private class ViewHolder {
        private ImageView imageView;
    }
}
