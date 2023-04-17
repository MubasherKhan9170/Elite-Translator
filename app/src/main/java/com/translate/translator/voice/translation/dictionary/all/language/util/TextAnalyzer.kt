package com.translate.translator.voice.translation.dictionary.all.language.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translate.translator.voice.translation.dictionary.all.language.viewmodels.TranslatorMainViewModel


/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class TextAnalyzer(
        private val context: Context,
        lifecycle: Lifecycle,
        private val model: TranslatorMainViewModel) : ImageAnalysis.Analyzer {

    // TODO: Instantiate TextRecognition detector
    private val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        lifecycle.addObserver(detector)
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        }else if (mlKitException.errorCode == MlKitException.NETWORK_ISSUE){
            "Check your Interconnection, then retry."
        }
        else exception.message
    }


    companion object {
        private const val TAG = "TextAnalyzer"
        fun recognizeText(
                textAnalyzer: TextAnalyzer, image: InputImage
        ): Task<Text> {
            // Pass image to an ML Kit Vision API
            return textAnalyzer.detector.process(image)
                .addOnSuccessListener { text ->
                    // Task completed successfully

                    //textAnalyzer.result.value = text.text.replace("\n", " ")
                    textAnalyzer.model.setImageExtractedText(text.text.replace("\n", " "))
                    Log.d(TAG, "recognizeText: " + text.text)
                }
                .addOnFailureListener { exception ->
                    // Task failed with an exception
                    Log.e(TAG, "Text recognition error", exception)
                    val message = textAnalyzer.getErrorMessage(exception)
                    message?.let {
                        Toast.makeText(textAnalyzer.context, message, Toast.LENGTH_LONG).show()
                    }
                }
        }




    }

    override fun analyze(image: ImageProxy) {
        TODO("Not yet implemented")
    }


}