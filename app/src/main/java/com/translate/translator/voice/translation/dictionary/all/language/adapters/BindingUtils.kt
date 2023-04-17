package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.graphics.Color
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import com.google.android.material.card.MaterialCardView
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.data.SharedItem
import com.translate.translator.voice.translation.dictionary.all.language.database.TranslationItem
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel

@BindingAdapter("flagIcon")
fun ImageView.setCountryFlagImage(item: LanguageItem?) {
    item?.let {
        setImageBitmap(it.flagIcon?.bitmap)
    }
}

@BindingAdapter("langNameEN")
fun TextView.setCountryLangNameEn(item: LanguageItem?) {
    item?.let {
        text = it.displayName
    }
}

@BindingAdapter("langNameLocal")
fun TextView.setCountryLangNameLocal(item: LanguageItem?) {
    item?.let {
        text = it.langNameLocal
    }
}

@BindingAdapter("countryName")
fun TextView.setCountryName(item: LanguageItem?) {
    item?.let {
        text = it.showCountryName
    }
}

@BindingAdapter("selectionIcon", "type")
fun ImageView.setItemSelectionImage(item: LanguageItem?, model: ViewModel?) {
    when(model){
        is TranslatorMainViewModel ->{
            item?.let {
                if(item.langNameEN == model.getSelectedLang() && item.countryName == model.getSelectedLangCountry()){
                    setImageResource(R.drawable.ic_circle_selection_icon)
                }else{
                    setImageResource(R.drawable.ic_circle_unselection_icon)
                }
            }
        }
        is TranslatorMultiViewModel ->{
            if(model.type == "source"){
                item?.let {
                    if(item.langNameEN == model.getSelectedLang() && item.countryName == model.getSelectedLangCountry()){
                        setImageResource(R.drawable.ic_circle_selection_icon)
                    }else{
                        setImageResource(R.drawable.ic_circle_unselection_icon)
                    }
                }

            }else{
                item?.let {
                    //Log.i("Data", "setItemSelectionImage: "+ model.multiselect_list+ "()" + it)

                    val item = it.copy(
                        displayName = it.displayName,
                        langNameEN = it.langNameEN,
                        langNameLocal = it.langNameLocal,
                        showCountryName = it.showCountryName,
                        countryName = it.countryName,
                        flagIcon = null,
                        countryCode = it.countryCode,
                        bcp47 = it.bcp47,
                        iso3 = it.iso3
                    )
                    val sharedItem = SharedItem(
                        displayName = null,
                        langNameEN = item.langNameEN,
                        langNameLocal = item.langNameLocal,
                        showCountryName = null,
                        countryName = item.countryName,
                        countryCode = item.countryCode,
                        bcp47 = item.bcp47,
                        iso3 = item.iso3)
                    if(model.multiselect_list.contains(sharedItem)){
                        Log.i("Data", "contains: yes")
                        setImageResource(R.drawable.ic_circle_selection_icon)
                    }else{
                     //   Log.i("Data", model.multiselect_list[0].toString())
                        setImageResource(R.drawable.ic_circle_unselection_icon)
                    }
                }
            }



        }

    }


}


@BindingAdapter("srcCompat")
fun ImageView.setAutoItemImage(model: ViewModel?) {
    when(model){
        is TranslatorMainViewModel ->{
            if(model.autoItemState){
                setImageResource(R.drawable.ic_check_box_checked)
            }
            else{
                setImageResource(R.drawable.ic_auto_detect_icon)
            }
        }
        is TranslatorMultiViewModel ->{
            if(model.autoItemState){
                setImageResource(R.drawable.ic_check_box_checked)
            }
            else{
                setImageResource(R.drawable.ic_auto_detect_icon)
            }
        }
    }


}


@BindingAdapter("text")
fun TextView.setTranslationText(model: TranslatorMainViewModel?) {
    model?.let {
        text = it.homeText.value.toString() + "\n\n" + it.translatedHomeText.value.toString()
    }
}


@BindingAdapter("text")
fun TextView.setTranslationText(model: TranslatorMultiViewModel?) {
    model?.let {
        text = it.homeText.value.toString() + "\n\n" + it.translatedHomeText.value.toString()
    }
}

/*Voice Fragment
* Binding Adapter*/
@BindingAdapter("colorBackground")
fun MaterialCardView.setBackgroundColor(model: TranslatorMainViewModel) {
    if(model.firstPersonFocusState || !model.firstPersonFocusState ){
        setCardBackgroundColor(Color.parseColor("#ffffff"))
    }
    else{
        setCardBackgroundColor(Color.parseColor("#3d3d3d"))
    }

}



/*History Fragment
* Binding Adapter*/

@BindingAdapter("favourite")
fun ImageView.setFavouriteItemIcon(status: Boolean) {
    if(status){
        setImageResource(R.drawable.ic_star_selected_icon)
    }
    else{
        setImageResource(R.drawable.ic_star_unselected_icon)
    }

}

@BindingAdapter("srctotar")
fun TextView.setSrcToTar(item: TranslationItem) {
    val bcp47CodeString = resources.getStringArray(R.array.lang_array_bcp_47_code)
    val displayLanguage = resources.getStringArray(R.array.lang_array_display_name)
    var src: String = ""
    var tar: String = ""
    item?.let {
        if(it.srcLanguage == "Auto Detected"){
            src = resources.getString(R.string.auto_detected)
        }else{
            if(bcp47CodeString.contains(it.srcBcp47)){
                src = displayLanguage[bcp47CodeString.indexOf(it.srcBcp47)]
            }
        }

        if(bcp47CodeString.contains(it.tarBcp47)){
            tar = displayLanguage[bcp47CodeString.indexOf(it.tarBcp47)]
        }


        text = "$src ${resources.getString(R.string.to)} $tar"
    }
}


/*@BindingAdapter("gone")
fun TextView.setVisibility(item: TranslatorMultiViewModel) {
    *//*visibility = if(item.selectedItemsList.value!!.isNullOrEmpty()){
        View.VISIBLE
    }else{
        View.GONE
    }
}*/









