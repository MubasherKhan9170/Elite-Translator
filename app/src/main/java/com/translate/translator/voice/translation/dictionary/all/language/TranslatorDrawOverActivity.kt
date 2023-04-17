package com.translate.translator.voice.translation.dictionary.all.language

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translate.translator.voice.translation.dictionary.all.language.services.ScreenCaptureService
import com.translate.translator.voice.translation.dictionary.all.language.util.*
import com.translate.translator.voice.translation.dictionary.all.language.util.DragRectView.OnUpCallback
import com.translate.translator.voice.translation.dictionary.all.language.util.callback.CloseEvent
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorDrawOverViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import android.util.DisplayMetrics

import android.view.WindowInsets

import android.view.WindowMetrics
import com.translate.translator.voice.translation.dictionary.all.language.databinding.ActivityTranslatorDrawOverBinding


@AndroidEntryPoint
class TranslatorDrawOverActivity : BaseActivity(), CloseEvent {
    val viewModel: TranslatorDrawOverViewModel by viewModels()
    private lateinit var binding: ActivityTranslatorDrawOverBinding

    private var mGraphicOverlay: GraphicOverlay? = null
    private var view: DragRectView? = null
    private val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var file: File? = null

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            this
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.Builder().setLanguage(viewModel.sourceIso3Code).build()
                textToSpeechEngine.language = locale
                //Log.i(TAG, "Source $locale")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_translator_draw_over)
        // Inflate view and obtain an instance of the binding class.

        if (Build.VERSION.SDK_INT >= 30) {
            window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        binding = ActivityTranslatorDrawOverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

       // binding = DataBindingUtil.setContentView(this, R.layout.activity_translator_draw_over)

        // Specify the current activity as the lifecycle owner.
        binding.lifecycleOwner = this


        Log.i(TAG, "onCreate: ")
        mGraphicOverlay = binding.graphicOverlay

        view = findViewById(R.id.rect)

        mGraphicOverlay?.clear()

        Log.i(TAG, "onCreate: "+ intent)
        val intent = intent

        if(reset == null){
            Log.i(TAG, "onCreate: open the main activity")
            val activityIntent = Intent(this, TranslatorSplashActivity::class.java)
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(activityIntent)
            finish()

        }else{
            try {
                val path = intent.getStringExtra("path")
                file = File(path!!)
                //val bitmap = BitmapFactory.decodeFile(file!!.absolutePath)


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics =
                        this.getWindowManager().getCurrentWindowMetrics()
                    val insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                    windowMetrics.bounds.width() - insets.left - insets.right
                    binding.full.mEndX = windowMetrics.bounds.width()
                    binding.full.mEndY = windowMetrics.bounds.height()

                } else {
                    val displayMetrics = DisplayMetrics()
                    this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
                    binding.full.mEndX = displayMetrics.widthPixels
                    binding.full.mEndY = displayMetrics.heightPixels
                }

                val mStartX = 0
                val mStartY = 0
                val mEndX = binding.full.mEndX
                val mEndY = binding.full.mEndY

                val rect = Rect(
                    Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                    Math.max(mEndX, mStartX), Math.max(mStartY, mEndY)
                )


                val decode = BitmapUtils.decodeSampledBitmapRegion(baseContext, Uri.fromFile(file), rect, rect.width(), rect.height(), 1)
                binding.image.setImageBitmap(decode.bitmap)

                TextGraphic.TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.font_size).toFloat()

                if(intent.getBooleanExtra("type", false)){
                    recognizeText(InputImage.fromBitmap(decode.bitmap, 0), true)
                }else{
                    recognizeText(InputImage.fromBitmap(decode.bitmap, 0), false)
                }

            }catch (e: Exception){
                Log.e(TAG, "onCreate: ", e)

            }

            ScreenCaptureService.event = this

            view?.setOnUpCallback(object : OnUpCallback {
                override fun onRectFinished(rect: Rect?) {
                    Log.i(TAG, "onRectFinished: ")
                    /*if(rect!!.width() > 4 && rect!!.height() > 4){
                        val decode = BitmapUtils.decodeSampledBitmapRegion(baseContext, Uri.fromFile(file), rect, rect!!.width(), rect!!.height(), 1)
                        if(decode.bitmap != null){
                            recognizeText(InputImage.fromBitmap(decode.bitmap, 0), false)

                        }
                        view?.invalidate()
                    }*/

                }

                override fun onTextClicked(text: String?) {
                    Log.i(TAG, "onTextClicked: " + text)
                    InternetUtils.get(this@TranslatorDrawOverActivity).observe(this@TranslatorDrawOverActivity, Observer {
                        if(it){
                            if (text?.isNotEmpty() == true) {

                                viewModel.doTranslation(text)

                                viewModel.workManager.getWorkInfoByIdLiveData(viewModel.request.id)
                                    .observe(this@TranslatorDrawOverActivity, { workInfo: WorkInfo? ->
                                        Log.i(TAG, "Work State: " + workInfo?.state)
                                        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                                            val viewPopupWindow: View = createPopWindow()
                                            viewPopupWindow.rootView.findViewById<ImageButton>(R.id.pop_speak_button_id).setOnClickListener {
                                                //Toast.makeText(baseContext, "Text to Speak", Toast.LENGTH_LONG).show()
                                                if (textToSpeechEngine.isLanguageAvailable(
                                                        Locale(viewModel.sourceIso3Code!!)
                                                    ) == TextToSpeech.LANG_NOT_SUPPORTED
                                                ) {
                                                    Log.i(TAG, "Not Supporting Languages: ")

                                                    Toast.makeText(baseContext, "This Language is not supported", Toast.LENGTH_SHORT)
                                                        .show()
                                                } else {
                                                    Log.i(TAG, "SourceSpeakIcon: " + textToSpeechEngine.language.toString())
                                                    textToSpeechEngine.language = Locale(viewModel.sourceIso3Code.toString())
                                                    textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                                                }

                                            }

                                            viewPopupWindow.rootView.findViewById<ImageButton>(R.id.pop_edit_button_id).setOnClickListener {
                                                if(reset != null){
                                                    reset!!.reset(true)
                                                }

                                                val activityIntent = Intent(this@TranslatorDrawOverActivity, TranslatorMainActivity::class.java)
                                                activityIntent.putExtra("SourceText", text)
                                                activityIntent.putExtra("TargetText", workInfo.outputData.getString("translatedText"))
                                                activityIntent.putExtra("showDialog", true)
                                                startActivity(activityIntent)
                                                finish()
                                            }

                                            viewPopupWindow.findViewById<ImageButton>(R.id.pop_copied_button_id).setOnClickListener {
                                                val clipboard = this@TranslatorDrawOverActivity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = ClipData.newPlainText(
                                                    "Copied Text",
                                                    text + " \n"+ workInfo.outputData.getString("translatedText")
                                                )
                                                clipboard.setPrimaryClip(clip)
                                                Snackbar.make(binding.root.rootView, resources.getText(R.string.text_copy_label), Snackbar.LENGTH_SHORT)
                                                    .show()

                                            }
                                            viewModel.doTranslation(text)
                                            viewPopupWindow.findViewById<TextView>(R.id.pop_source_text_id).text = text
                                            viewPopupWindow.findViewById<TextView>(R.id.pop_target_text_id).text = workInfo.outputData.getString("translatedText")

                                        }
                                        if (workInfo?.state == WorkInfo.State.FAILED) {
                                            // show dialog
                                        }
                                    })
                            } else {
                                Toast.makeText(baseContext, "Text cannot be empty", Toast.LENGTH_LONG).show()
                            }
                        }else{
                            Toast.makeText(baseContext, "This feature isn't available offline", Toast.LENGTH_SHORT).show()
                        }
                    })
                }


            })
        }

    }





    private fun createPopWindow(): View {
        val layoutInflater =
            getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        setTheme(R.style.Theme_ELITE_TRANSLATOR)
        val viewPopupWindow: View =
            layoutInflater.inflate(R.layout.popupwindowlayout, null)
        val popupWindow = PopupWindow(
            viewPopupWindow,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(
            mGraphicOverlay, Gravity.CENTER,
            0, 0
        )
        viewPopupWindow.rootView.findViewById<ImageButton>(R.id.pop_close_button_id).setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.isOutsideTouchable = false
        return viewPopupWindow
    }


    private fun recognizeText(image: InputImage, flag: Boolean): Task<Text> {
        // Pass image to an ML Kit Vision API
        return detector.process(image)
            .addOnSuccessListener { text ->
                // Task completed successfully
                // val result = text.text
                val result = text.text.toString()
                if(flag){
                    processTextRecognitionResult(text)
                }else{
                  //  val viewPopupWindow: View = createPopWindow()
                   // viewPopupWindow.findViewById<TextView>(com.maximus.elitetranslator.R.id.pop_source_text_id).text = text.text.replace("\n", " ")
                    Log.i(TAG, "recognizeText: $result")
                    if(result.isNotEmpty()){
                        val activityIntent = Intent(this, TranslatorMainActivity::class.java)
                        activityIntent.putExtra("SourceText", result)
                        activityIntent.putExtra("showDialog", true)
                        startActivity(activityIntent)
                         finish()
                    }


                }


            }
            .addOnFailureListener { _ ->
                // Task failed with an exception

            }
    }


    private fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            //showToast("No text found")

            Toast.makeText(this, "No text found", Toast.LENGTH_LONG).show()
            return
        }
        mGraphicOverlay?.clear()
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            Log.i(TAG, "processTextRecognitionResult: ")
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic: GraphicOverlay.Graphic = TextGraphic(mGraphicOverlay, elements[k])
                    mGraphicOverlay?.add(textGraphic)
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")

    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
        if(reset != null){
            reset!!.reset(true)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")

    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "onRestart: ")
        val activityIntent = Intent(this, TranslatorSplashActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(activityIntent)
        finish()

    }




    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
        reset = null

    }




    companion object{
        private const val TAG = "TestActivity"
        var  reset: CloseEvent? = null
    }

    override fun close() {
       // intent.putExtra("path", "")
        finish()
        reset = null
    }

    override fun reset(boolean: Boolean) {
    }


    override fun onBackPressed() {

    }



}


