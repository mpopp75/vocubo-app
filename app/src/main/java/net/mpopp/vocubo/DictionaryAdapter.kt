package net.mpopp.vocubo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject

class DictionaryAdapter(
    private val mContext: Context,
    private val mDictionaryList: ArrayList<JSONObject>
) : ArrayAdapter<JSONObject?>(
    mContext, 0, mDictionaryList as List<JSONObject?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext)
                .inflate(R.layout.activity_dictionaries_list_item, parent, false)
        }
        val tvLanguages = listItem!!.findViewById<TextView>(R.id.tvLanguages)
        val cbDictionary = listItem.findViewById<CheckBox>(R.id.cbDictionary)
        try {
            val dictionary = mDictionaryList[position]
            val dictName = dictionary.getString("dictname")
            cbDictionary.text = dictName
            val languages = dictionary.getString("baselang_long") + " -> " +
                    dictionary.getString("lang_long")
            tvLanguages.text = languages
            val enabledString = dictionary.getString("enabled")
            if (enabledString == "y") {
                cbDictionary.isChecked = true
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return listItem
    }
}