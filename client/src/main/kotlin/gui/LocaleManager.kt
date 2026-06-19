package gui

import java.io.InputStreamReader
import java.text.DateFormat
import java.text.MessageFormat
import java.text.NumberFormat
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

object LocaleManager {
    val availableLocales: List<Pair<Locale, String>> = listOf(
        Locale("ru") to "Русский",
        Locale("is") to "Íslenska",
        Locale("bg") to "Български",
        Locale("es", "CR") to "Español (CR)",
    )

    private var _locale: Locale = Locale("ru")
    private var _bundle: ResourceBundle = loadBundle(Locale("ru"))
    private val listeners = mutableListOf<() -> Unit>()

    val locale: Locale get() = _locale

    fun setLocale(locale: Locale) {
        _locale = locale
        _bundle = loadBundle(locale)
        listeners.toList().forEach { it() }
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun getString(key: String): String =
        try { _bundle.getString(key) } catch (_: Exception) { key }

    fun format(key: String, vararg args: Any): String =
        MessageFormat.format(getString(key), *args)

    fun getNumberFormat(): NumberFormat = NumberFormat.getNumberInstance(_locale)

    fun getDateFormat(): DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, _locale)

    private fun loadBundle(locale: Locale): ResourceBundle {
        val base = "i18n/messages"
        val candidates = buildList {
            if (locale.country.isNotEmpty()) add("${base}_${locale.language}_${locale.country}")
            add("${base}_${locale.language}")
            add(base)
        }
        val loader = LocaleManager::class.java.classLoader
        for (name in candidates) {
            val stream = loader.getResourceAsStream("$name.properties") ?: continue
            return PropertyResourceBundle(InputStreamReader(stream, Charsets.UTF_8))
        }
        throw IllegalStateException("No resource bundle found for $locale")
    }
}
