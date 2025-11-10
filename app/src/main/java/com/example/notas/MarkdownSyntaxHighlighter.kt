package com.example.notas

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import java.util.regex.Pattern

class MarkdownSyntaxHighlighter(context: Context) : TextWatcher {

    // Define los patrones de Regex para la sintaxis de Markdown
    private val patterns = mapOf(
        // Encabezados (#, ##, ...)
        Pattern.compile("^#{1,6} .*", Pattern.MULTILINE) to R.color.my_blue_primary,
        // Negrita (**texto** o __texto__)
        Pattern.compile("(\\*\\*|__)(.*?)\\1") to R.color.my_red_error,
        // Cursiva (*texto* o _texto_)
        Pattern.compile("(\\*|_)(.*?)\\1") to R.color.my_blue_primary,
        // Citas (> texto)
        Pattern.compile(
            "^> .*",
            Pattern.MULTILINE
        ) to R.color.teal_700 // Usa el color que prefieras
    )

    private val colorMap = patterns.mapValues {
        ContextCompat.getColor(context, it.value)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        // Primero, elimina todos los estilos anteriores para evitar solapamientos
        clearSpans(s)

        // Aplica el resaltado para cada patrón
        for ((pattern, _) in patterns) {
            val color = colorMap[pattern]!!
            val matcher = pattern.matcher(s)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                // Resalta la sintaxis de control (ej: los ** o el #)
                s.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Aplica estilos visuales (negrita, tamaño, etc.)
                when (pattern.pattern()) {
                    "^#{1,6} .*" -> {
                        s.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        s.setSpan(
                            RelativeSizeSpan(1.3f),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    "(\\*\\*|__)(.*?)\\1" -> {
                        s.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    "(\\*|_)(.*?)\\1" -> {
                        s.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    "^> .*" -> {
                        s.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
    }

    private fun clearSpans(s: Editable) {
        val spans = s.getSpans(0, s.length, Any::class.java)
        for (span in spans) {
            if (span is ForegroundColorSpan || span is StyleSpan || span is RelativeSizeSpan) {
                s.removeSpan(span)
            }
        }
    }
}