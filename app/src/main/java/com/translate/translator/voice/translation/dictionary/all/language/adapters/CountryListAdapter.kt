package com.translate.translator.voice.translation.dictionary.all.language.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.data.LanguageItem
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutAutoDetectHeaderBinding
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutLanguageItemHeaderBinding
import com.translate.translator.voice.translation.dictionary.all.language.databinding.LayoutLanguageListItemBinding
import com.translate.translator.voice.translation.dictionary.all.language.util.LanguageManager
import com.translate.translator.voice.translation.dictionary.all.language.util.UserPreferencesRepository
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMultiViewModel
import java.util.ArrayList

private val AUTO_DETECT_LAANGUAGE_HEADER =0
private val RECENT_ITEM_VIEW_TYPE_HEADER = 1
private val RECENT_ITEM_VIEW_TYPE = 2
private val ITEM_VIEW_TYPE_HEADER = 3
private val ITEM_VIEW_TYPE_ITEM = 4
private const val TAG = "CountryListAdapter"
class CountryListAdapter<T : ViewModel?>(val clickListener: SleepNightListener, val autoClickListener: AutoNightListener, val model: T, context: Context):  RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var positionSize: Int = 0
  //  private val adapterScope = CoroutineScope(Dispatchers.Default)
    var data = mutableListOf<DataItem>()
    val filterItem = mutableListOf<DataItem>()

    init {
        setContext(context)
    }

   // var usersList: ArrayList<LanguageItem> = ArrayList<LanguageItem>()
    //var selectedUsersList: ArrayList<LanguageItem> = ArrayList<LanguageItem>()


    /*add the items in the viewHolder*/
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        
        when(model){
            is TranslatorMainViewModel ->{
                Log.i(TAG, "onBindViewHolder: Main")
                when(holder){
                    is AutoViewHolder -> {
                        val item = data[position] as DataItem.AutoDetect
                        holder.bind(autoClickListener, item, model as TranslatorMainViewModel)
                    }

                    is TextViewHolder -> {
                        Log.i("CountryListAdapter", "onBindViewHolder: " + positionSize)
                        if(positionSize>0){
                            val item = data[position] as DataItem.Recent
                            Log.d(TAG, "onBindViewHolder: recent"+ item.id)
                            holder.bind(item)
                        }

                    }
                    is RecentItemViewHolder -> {
                        Log.i("CountryListAdapter", "onBindViewHolder: " + holder)
                        holder.binding.headerId.visibility = View.GONE
                        val item = data[position] as DataItem.RecentNightItem
                        holder.bind(clickListener, item, model as TranslatorMainViewModel)
                    }

                    is LanguageTextViewHolder -> {
                        if(UserPreferencesRepository.getInstance(newContext).getAppLanguage(
                                LanguageManager.LANGUAGE_KEY) == "en"){
                                val item = data[position] as DataItem.LanguageHeader
                        holder.bind(item)

                                }

                    }
                    is ViewHolder -> {
                        // if not first item, check if item above has the same Recent
                        if (position > positionSize + 1  && data[position - 1].id!!.substring(0, 1) == data[position].id!!.substring(0, 1)) {
                            holder.binding.headerId.visibility = View.GONE
                        } else {
                            holder.binding.headerId.text = data[position].id!!.substring(0, 1)
                            if(holder.binding.headerId.text  == data[0].id){
                                holder.binding.headerId.visibility = View.GONE
                            }else{
                                holder.binding.headerId.visibility = View.VISIBLE
                            }

                        }

                        val item = data[position] as DataItem.SleepNightItem
                        holder.bind(clickListener, item, model as TranslatorMainViewModel)
                    }
                }
            }

            is TranslatorMultiViewModel ->{
                Log.i(TAG, "onBindViewHolder: Multi")
                when(holder){
                    is AutoViewHolder -> {
                        val item = data[position] as DataItem.AutoDetect
                        holder.bind(autoClickListener, item, model as TranslatorMultiViewModel)
                    }

                    is TextViewHolder -> {
                        Log.i(TAG, "onBindViewHolder: " + positionSize)
                        if(positionSize>0){
                            val item = data[position] as DataItem.Recent
                            holder.bind(item)
                        }

                    }
                    is RecentItemViewHolder -> {
                        Log.i(TAG, "onBindViewHolder: " + holder)
                        holder.binding.headerId.visibility = View.GONE
                        val item = data[position] as DataItem.RecentNightItem
                        holder.bind(clickListener, item, model as TranslatorMultiViewModel)
                    }

                    is LanguageTextViewHolder -> {
                        if(UserPreferencesRepository.getInstance(newContext).getAppLanguage(
                                LanguageManager.LANGUAGE_KEY) == "en") {
                            val item = data[position] as DataItem.LanguageHeader
                            holder.bind(item)
                        }


                    }
                    is ViewHolder -> {

                        // if not first item, check if item above has the same Recent
                        if (position > 0  && data[position - 1].id!!.substring(0, 1) == data[position].id!!.substring(0, 1)) {
                            holder.binding.headerId.visibility = View.GONE
                        } else {
                            holder.binding.headerId.text = data[position].id!!.substring(0, 1)
                            holder.binding.headerId.visibility = View.VISIBLE
                            Log.i(TAG, "onBindViewHolder: " + positionSize)
                        }

                        /*if(selectedUsersList.contains((data.get(position)))){
                            Log.i(TAG, "onBindViewHolder: selected")
                            holder.binding.langItemCardId.background.setTint(holder.binding.root.context.getColor(R.color.primaryColor))
                        } else{
                            holder.binding.langItemCardId.background.setTint(holder.binding.root.context.getColor(R.color.white))
                        }*/


                        val item = data[position] as DataItem.SleepNightItem
                        holder.bind(clickListener, item, model as TranslatorMultiViewModel)

                    }
                }

            }
        }
        
        
        



    }



    /*create the view Holder for first items to fill the screen*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Create a new view, which defines the UI of the list item
        return when (viewType) {
            AUTO_DETECT_LAANGUAGE_HEADER -> AutoViewHolder.from(parent)
            RECENT_ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            RECENT_ITEM_VIEW_TYPE -> RecentItemViewHolder.from(parent)
            ITEM_VIEW_TYPE_HEADER -> LanguageTextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }



    override fun getItemViewType(position: Int): Int {

        return when (data[position]) {
            is DataItem.AutoDetect -> AUTO_DETECT_LAANGUAGE_HEADER
            is DataItem.Recent -> RECENT_ITEM_VIEW_TYPE_HEADER
            is DataItem.RecentNightItem -> RECENT_ITEM_VIEW_TYPE
            is DataItem.LanguageHeader -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }


    class AutoViewHolder private constructor(val binding: LayoutAutoDetectHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(clickListener: AutoNightListener, Recent: DataItem.AutoDetect, viewModel: ViewModel) {
            binding.auto = Recent.id
            binding.clickListener = clickListener
            when(viewModel){
                is TranslatorMainViewModel ->{
                    binding.model = viewModel
                }
                is TranslatorMultiViewModel ->{
                    binding.model = viewModel
                }
            }

            binding.executePendingBindings()

        }
        companion object{
            fun from(parent: ViewGroup): AutoViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutAutoDetectHeaderBinding.inflate(layoutInflater, parent, false)
                return AutoViewHolder(binding)
            }
        }
    }


    class TextViewHolder private constructor(val binding: LayoutLanguageItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(Recent: DataItem.Recent) {
            binding.headerName = Recent.id
            binding.recentStatus = true
            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutLanguageItemHeaderBinding.inflate(layoutInflater, parent, false)
                return TextViewHolder(binding)
            }
        }
    }

    class RecentItemViewHolder private constructor(val binding: LayoutLanguageListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: SleepNightListener, item: DataItem.RecentNightItem, model: ViewModel) {
            binding.language = item.item
            binding.clickListener = clickListener
            when(model){
                is TranslatorMainViewModel ->{
                    binding.dataSource = model
                }
                is TranslatorMultiViewModel ->{
                    binding.dataSource = model
                }
            }
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): RecentItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutLanguageListItemBinding.inflate(layoutInflater, parent, false)
                return RecentItemViewHolder(binding)
            }
        }
    }

    class LanguageTextViewHolder private constructor(val binding: LayoutLanguageItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(Recent: DataItem.LanguageHeader) {
            binding.headerName = Recent.id
            binding.recentStatus = false
            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): LanguageTextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutLanguageItemHeaderBinding.inflate(layoutInflater, parent, false)
                return LanguageTextViewHolder(binding)
            }
        }
    }


    class ViewHolder private constructor(val binding: LayoutLanguageListItemBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(clickListener: SleepNightListener, item: DataItem.SleepNightItem, model: ViewModel) {
            binding.language = item.item
            binding.clickListener = clickListener

            when(model){
                is TranslatorMainViewModel ->{

                    binding.dataSource = model
                }
                is TranslatorMultiViewModel ->{
                    binding.dataSource = model
                   // binding.layoutItemCheckedId.setOnClickListener(this)
                }
            }
            binding.executePendingBindings()

            }
            companion object {
                fun from(parent: ViewGroup): ViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = LayoutLanguageListItemBinding.inflate(layoutInflater, parent, false)
                    return ViewHolder(binding)
                }
            }

        override fun onClick(v: View?) {
            Log.i(TAG, "onClick: ")
            binding.langItemCardId.background.setTint(binding.root.context.getColor(R.color.primaryColor))
        }
    }

    fun updateList(type: String?, newRecent: List<LanguageItem>, newList: List<LanguageItem>){
        val newItems: List<DataItem>
        positionSize = newRecent.size
        if(type == "source"){
            newItems = when {
                else -> listOf(DataItem.AutoDetect) + listOf(DataItem.Recent) + newRecent.map { DataItem.RecentNightItem(it) }+ listOf(DataItem.LanguageHeader) + newList.map { DataItem.SleepNightItem(it) }
            }
        }else if(type == "target"){
            newItems = when {
                else -> listOf(DataItem.Recent) + newRecent.map { DataItem.RecentNightItem(it) }+ listOf(DataItem.LanguageHeader) + newList.map { DataItem.SleepNightItem(it) }
            }
        }else{
            newItems = when {
                else -> listOf(DataItem.Recent) + newRecent.map { DataItem.RecentNightItem(it) }+ listOf(DataItem.LanguageHeader) + newList.map { DataItem.SleepNightItem(it) }
            }
        }

        Log.e("CountryListAdapter", "addHeaderAndSubmitList: " + newItems.size)

        val diffCallback = LanguageItemDiffCallback(this.data, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.data.clear()
        this.data.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<DataItem> = ArrayList<DataItem>()
            if (constraint.isEmpty()) {
                filteredList.addAll(filterItem)
                Log.i("CountryListAdapter", "performFiltering: " + filterItem.size
                )
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in filterItem) {
                    if (item.id!!.lowercase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            data.clear()
            data.addAll(results.values as List<DataItem>)
            notifyDataSetChanged()
        }
    }

    companion object{
         lateinit var newContext: Context
        fun setContext(con: Context) {
            newContext = con
        }
    }

}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minumum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class LanguageItemDiffCallback(private val oldList: List<DataItem>, private val newList: List<DataItem>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //Log.i("CountryListAdapter", "areContentsTheSame: ")
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}


class SleepNightListener(val clickListener: (LanguageName: LanguageItem) -> Unit) {
    fun onClick(language: LanguageItem){
        clickListener(language)
    }
}

class AutoNightListener( val autoClickListener: () -> Unit) {

    fun onAutoClick(){
        autoClickListener()
    }

}

sealed class DataItem {

    object AutoDetect: DataItem() {
        override val id = "Auto Detect Language"
    }

    data class SleepNightItem(val item: LanguageItem): DataItem() {
        override val id = item.displayName
    }

    object Recent: DataItem() {
        override val id = CountryListAdapter.newContext.getString(R.string.recent)
    }
    data class RecentNightItem(val item: LanguageItem): DataItem() {
        override val id = item.displayName
    }

    object LanguageHeader: DataItem() {
        override val id = CountryListAdapter.newContext.getString(R.string.list_word_header)
    }
    abstract val id: String?
}