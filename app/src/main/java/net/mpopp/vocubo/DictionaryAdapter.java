package net.mpopp.vocubo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DictionaryAdapter extends ArrayAdapter<JSONObject> {
    private Context mContext;
    private ArrayList<JSONObject> mDictionaryList;

    public DictionaryAdapter(Context context, ArrayList<JSONObject> dictionaryList) {
        super(context, 0, dictionaryList);
        mContext = context;
        mDictionaryList = dictionaryList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.activity_dictionaries_list_item, parent, false);
        }

        TextView tvLanguages = listItem.findViewById(R.id.tvLanguages);
        CheckBox cbDictionary = listItem.findViewById(R.id.cbDictionary);

        try {
            JSONObject dictionary = mDictionaryList.get(position);
            String dictName = dictionary.getString("dictname");
            cbDictionary.setText(dictName);

            String languages = dictionary.getString("baselang_long") + " -> " +
                    dictionary.getString("lang_long");
            tvLanguages.setText(languages);

            String enabledString = dictionary.getString("enabled");

            if (enabledString.equals("y")) {
                cbDictionary.setChecked(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listItem;
    }
}