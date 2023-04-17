package com.translate.translator.voice.translation.dictionary.all.language.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.text.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.graphics.withTranslation
import androidx.core.util.lruCache
import com.translate.translator.voice.translation.dictionary.all.language.R
import com.translate.translator.voice.translation.dictionary.all.language.databinding.FragmentPdfGeneratorBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PdfGeneratorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PdfGeneratorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: TranslatorMainViewModel by activityViewModels()
    private lateinit var binding: FragmentPdfGeneratorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pdf_generator, container, false)
        // return inflater.inflate(R.layout.fragment_gallery, container, false)
        binding.model = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.translateButton.setOnClickListener {

            // viewModel.setHomeText(it.srcText)

          //  NavHostFragment.findNavController(this).navigate(PdfGeneratorFragmentDirections.actionPdfGeneratorFragmentToMainFragmentId())
            NavHostFragment.findNavController(this).navigate(R.id.action_pdfGeneratorFragment_to_main_fragment_id)
        }

        binding.saveButton.setOnClickListener {

            if (!binding.textViewId.text.isNullOrEmpty()) {
                MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setView(R.layout.dialog_enter_file_name)
                    .setNegativeButton(getString(R.string.pdf_generate_cancel_button)) { dialog, _ ->
                        // Respond to negative button press
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.pdf_generate_ok_button)) { dialog, _ ->
                        // Respond to positive button press

                        val text =
                            (dialog as? AlertDialog)?.findViewById<EditText>(R.id.name_field_id)?.text?.toString()
                        if (text!!.isNotEmpty()) {
                            generatePDF(text, binding.textViewId.text.toString())
                            dialog.dismiss()
                        }

                    }
                    .setCancelable(false)
                    .show()
            } else {
                Toast.makeText(context, getString(R.string.no_text_found), Toast.LENGTH_LONG).show()
            }


        }



        binding.shareButton.setOnClickListener {
            var sendIntent: Intent? = null
            var filePath: Uri?
            var item = -1

            if (!binding.textViewId.text.isNullOrEmpty()) {
                val singleItems = arrayOf(getString(R.string.pdf_generate_option_one), getString(R.string.pdf_generate_option_two))
                val checkedItem = -1

                MaterialAlertDialogBuilder(context!!, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(getString(R.string.pdf_generate_share_title))
                    .setNeutralButton(getString(R.string.pdf_generate_cancel_button)) { dialog, _ ->
                        // Respond to neutral button press
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.pdf_generate_ok_button)) { dialog, _ ->
                        // Respond to positive button press
                        when (item) {
                            0 -> {
                                val shareIntent = Intent.createChooser(sendIntent, getString(R.string.pdf_generate_share_title))
                                startActivity(shareIntent)
                                dialog.dismiss()
                            }
                            1 -> {
                                filePath = generatePDF("File", binding.textViewId.text.toString())
                                if (filePath != null) {
                                    sendIntent?.setDataAndType(filePath, "application/pdf")
                                    sendIntent?.putExtra(Intent.EXTRA_STREAM, filePath)
                                    val shareIntent = Intent.createChooser(sendIntent, getString(R.string.pdf_generate_share_title))
                                    startActivity(shareIntent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.not_find_path),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }


                                dialog.dismiss()
                            }
                            else -> {
                                dialog.dismiss()
                            }
                        }


                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(singleItems, checkedItem) { _, i ->
                        Log.i(TAG, "item $i")
                        when (i) {

                            0 -> {
                                sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, binding.textViewId.text.toString())
                                    type = "text/plain"
                                }

                                item = 0
                            }
                            1 -> {

                                sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    type = "application/pdf"
                                }
                                item = 1
                            }
                        }


                    }
                    .setCancelable(false)
                    .show()


            } else {
                Toast.makeText(context, getString(R.string.no_text_found), Toast.LENGTH_LONG).show()
            }

        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun generatePDF(fileName: String, text: String): Uri {


        // creating an object variable
        // for our PDF document.
        val pdfDocument = PdfDocument()

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        //  val paint = Paint()
        val title = TextPaint()

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        Log.i(TAG, "generatePDF: length" + text.length)

        val pages : Float = text.length.toFloat()/2250F
        Log.i(TAG, "generatePDF: $pages")

        val myPageInfo = PageInfo.Builder(595, 842, 1).create()

        // below line is used for setting
        // start page for our PDF file.
        val myPage = pdfDocument.startPage(myPageInfo)

        // creating a variable for canvas
        // from our page of PDF.
        val canvas: Canvas = myPage.canvas


        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.textSize = 16F

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.color = ContextCompat.getColor(context!!, R.color.primaryTextColor)

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
       // canvas.drawText(text, 20F, 100F, title)
        if(text.length > 2250.minus(17)){
            canvas.drawMultilineText(text,title, 545,20F, 100F, 0 , 2250.minus(17))
        }else{
            canvas.drawMultilineText(text,title, 545,20F, 100F, 0 , text.length)
        }

        //  canvas.drawText("Elite Translator", 209F, 80F, title)


        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage)

        if(pages > 1.0){
            val myPageInfo = PageInfo.Builder(595, 842, 2).create()

            // below line is used for setting
            // start page for our PDF file.
            val myPage = pdfDocument.startPage(myPageInfo)

            // creating a variable for canvas
            // from our page of PDF.
            val canvas: Canvas = myPage.canvas


            // below line is used for adding typeface for
            // our text which we will be adding in our PDF file.
            title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // below line is used for setting text size
            // which we will be displaying in our PDF file.
            title.textSize = 16F

            // below line is sued for setting color
            // of our text inside our PDF file.
            title.color = ContextCompat.getColor(context!!, R.color.primaryTextColor)

            // below line is used to draw text in our PDF file.
            // the first parameter is our text, second parameter
            // is position from start, third parameter is position from top
            // and then we are passing our variable of paint which is title.
            // canvas.drawText(text, 20F, 100F, title)
            canvas.drawMultilineText(text,title, 545,20F, 100F, 2250.minus(17), text.length)
            //  canvas.drawText("Elite Translator", 209F, 80F, title)


            // after adding all attributes to our
            // PDF file we will be finishing our page.
            pdfDocument.finishPage(myPage)
        }


        // below line is used to set the name of
        // our PDF file and its path.
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val t = date.format(calendar.time)

        val file = File(
            context!!.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${fileName + "_" + t.toString()}.pdf"
        )
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(FileOutputStream(file))

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(
                context,
                getString(R.string.pdf_generated) + file.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            // below line is used
            // to handle error
            e.printStackTrace()
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          return FileProvider.getUriForFile(context!!, "com.translate.translator.voice.translation.dictionary.all.language", file)
        } else {
           return Uri.fromFile(file)
        }

      //  return Uri.fromFile(file)
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.setHomeTextNull()
    }


    companion object {
        private const val TAG = "PdfGeneratorFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PdfGeneratorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PdfGeneratorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE,
        justificationMode: Int = Layout.JUSTIFICATION_MODE_NONE) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                "$maxLines-$breakStrategy-$hyphenationFrequency-$justificationMode"

        val staticLayout = StaticLayoutCache[cacheKey] ?:
        StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .setJustificationMode(justificationMode)
            .build().apply { StaticLayoutCache[cacheKey] = this }

        staticLayout.draw(this, x, y)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null,
        maxLines: Int = Int.MAX_VALUE,
        breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-$textDir-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize-" +
                "$maxLines-$breakStrategy-$hyphenationFrequency"

        val staticLayout = StaticLayoutCache[cacheKey] ?:
        StaticLayout.Builder.obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .setIncludePad(includePad)
            .setEllipsizedWidth(ellipsizedWidth)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setBreakStrategy(breakStrategy)
            .setHyphenationFrequency(hyphenationFrequency)
            .build().apply { StaticLayoutCache[cacheKey] = this }

        staticLayout.draw(this, x, y)
    }

    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        includePad: Boolean = true,
        ellipsizedWidth: Int = width,
        ellipsize: TextUtils.TruncateAt? = null) {

        val cacheKey = "$text-$start-$end-$textPaint-$width-$alignment-" +
                "$spacingMult-$spacingAdd-$includePad-$ellipsizedWidth-$ellipsize"

        // The public constructor was deprecated in API level 28,
        // but the builder is only available from API level 23 onwards
        val staticLayout = StaticLayoutCache[cacheKey] ?:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, start, end, textPaint, width)
                .setAlignment(alignment)
                .setLineSpacing(spacingAdd, spacingMult)
                .setIncludePad(includePad)
                .setEllipsizedWidth(ellipsizedWidth)
                .setEllipsize(ellipsize)
                .build()
        } else {
            StaticLayout(text, start, end, textPaint, width, alignment,
                spacingMult, spacingAdd, includePad, ellipsize, ellipsizedWidth)
                .apply { StaticLayoutCache[cacheKey] = this }
        }

        staticLayout.draw(this, x, y)
    }

    private fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
        canvas.withTranslation(x, y) {
            draw(this)
        }
    }

    private object StaticLayoutCache {

        private const val MAX_SIZE = 50 // Arbitrary max number of cached items
        private val cache = lruCache<String, StaticLayout>(MAX_SIZE)

        operator fun set(key: String, staticLayout: StaticLayout) {
            cache.put(key, staticLayout)
        }

        operator fun get(key: String): StaticLayout? {
            return cache[key]
        }
    }
}