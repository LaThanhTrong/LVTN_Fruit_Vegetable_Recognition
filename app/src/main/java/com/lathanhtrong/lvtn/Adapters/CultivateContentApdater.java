package com.lathanhtrong.lvtn.Adapters;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Activities.AddContentActivity;
import com.lathanhtrong.lvtn.Activities.CultivateContentActivity;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Cultivate;
import com.lathanhtrong.lvtn.Models.CultivateContent;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.CultivateContentRvBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CultivateContentApdater extends RecyclerView.Adapter<CultivateContentApdater.ViewHolder> {

    private List<CultivateContent> items = new ArrayList<>();

    public CultivateContentApdater(List<CultivateContent> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (Utils.getLanguage(holder.itemView.getContext()).equals("vi")) {
            holder.binding.tvName.setText(items.get(position).getCulcon_nameVi());
            holder.binding.tvDescription.setText(items.get(position).getCulcon_descriptionVi());
        }
        else {
            holder.binding.tvName.setText(items.get(position).getCulcon_name());
            holder.binding.tvDescription.setText(items.get(position).getCulcon_description());
        }

        File imageFile = new File(holder.itemView.getContext().getFilesDir(), "content_images/" + items.get(position).getCulcon_image() + ".png");
        if (imageFile.exists()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.binding.ivImage);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            int culcon_id = items.get(holder.getAdapterPosition()).getCulcon_id();
            int cul_id = items.get(holder.getAdapterPosition()).getCul_id();
            Intent intent = new Intent(holder.itemView.getContext(), CultivateContentActivity.class);
            intent.putExtra("culcon_id", culcon_id);
            intent.putExtra("cul_id", cul_id);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.binding.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] options = {v.getResources().getString(R.string.edit), v.getResources().getString(R.string.delete)};
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                ProgressDialog progressDialog = new ProgressDialog(holder.itemView.getContext());
                builder.setTitle(v.getResources().getString(R.string.chooseAction));
                int culcon_id = items.get(holder.getAdapterPosition()).getCulcon_id();
                String culcon_name = "";
                if (Utils.getLanguage(holder.itemView.getContext()).equals("vi")) {
                    culcon_name = items.get(holder.getAdapterPosition()).getCulcon_nameVi();
                }
                else {
                    culcon_name = items.get(holder.getAdapterPosition()).getCulcon_name();
                }
                String finalCulcon_name = culcon_name;
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(holder.itemView.getContext(), AddContentActivity.class);
                            intent.putExtra("culcon_id", culcon_id);
                            holder.itemView.getContext().startActivity(intent);
                        }
                        else {
                            ArrayList<Cultivate> cultivate = new ArrayList<>();
                            cultivate.add(new Cultivate(1, v.getResources().getString(R.string.cultivar)));
                            cultivate.add(new Cultivate(2, v.getResources().getString(R.string.landscape)));
                            cultivate.add(new Cultivate(3, v.getResources().getString(R.string.plant_propagate)));
                            cultivate.add(new Cultivate(4, v.getResources().getString(R.string.planting)));
                            cultivate.add(new Cultivate(5, v.getResources().getString(R.string.plant_caring)));
                            cultivate.add(new Cultivate(6, v.getResources().getString(R.string.harvest)));
                            int cul_id = items.get(holder.getAdapterPosition()).getCul_id();
                            String cul_name = cultivate.get(cul_id - 1).getCul_name();
                            AlertDialog.Builder delete = new AlertDialog.Builder(holder.itemView.getContext());
                            delete.setTitle(v.getResources().getString(R.string.delete_content));
                            delete.setMessage(v.getResources().getString(R.string.warnDelete)+" "+ finalCulcon_name +" "+v.getResources().getString(R.string.from)+" "+ cul_name +"?");
                            delete.setPositiveButton(v.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressDialog.setMessage(v.getResources().getString(R.string.delContentDb));
                                    progressDialog.show();
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setCancelable(false);

                                    DBHandler dbHandler = new DBHandler(holder.itemView.getContext());
                                    String fileImage = items.get(holder.getAdapterPosition()).getCulcon_image();
                                    String html = items.get(holder.getAdapterPosition()).getCulcon_html();

                                    dbHandler.deleteCultivateContent(culcon_id);
                                    progressDialog.dismiss();

                                    progressDialog.setMessage(v.getResources().getString(R.string.delContentStorage));
                                    progressDialog.show();
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setCancelable(false);
                                    File imageFile = new File(holder.itemView.getContext().getFilesDir(), "content_images/" + fileImage + ".png");
                                    if (imageFile.exists()) {
                                        imageFile.delete();
                                    }

                                    Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
                                    Matcher matcher = pattern.matcher(html);
                                    while (matcher.find()) {
                                        String fileName = matcher.group(1);
                                        File htmlImageFile = new File(holder.itemView.getContext().getFilesDir(), "content_html_images/" + fileName);
                                        if (htmlImageFile.exists()) {
                                            htmlImageFile.delete();
                                        }
                                    }

                                    items.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyItemRangeChanged(holder.getAdapterPosition(), items.size());
                                    progressDialog.dismiss();
                                    Toast.makeText(holder.itemView.getContext(), v.getResources().getString(R.string.delContentSuccess), Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton(v.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                    }
                }).show();
            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.from(parent);
    }

    public void setData(List<CultivateContent> newData) {
        items = newData;
        notifyDataSetChanged();
    }

    public void clear() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CultivateContentRvBinding binding;

        private ViewHolder(CultivateContentRvBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder from(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            CultivateContentRvBinding binding = CultivateContentRvBinding.inflate(layoutInflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}