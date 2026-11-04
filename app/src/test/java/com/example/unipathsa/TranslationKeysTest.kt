package com.example.unipathsa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

class TranslationKeysTest {

    @Test
    fun testAllEnglishStringKeysExistInTranslationFiles() {
        val resDir = findResDirectory()
        assertTrue("res directory not found at $resDir", resDir.exists())

        val englishFile = File(resDir, "values/strings.xml")
        assertTrue("English strings.xml not found", englishFile.exists())

        val englishMap = parseStringsFromXml(englishFile)
        assertTrue("English strings.xml must contain string keys", englishMap.isNotEmpty())

        val locales = listOf("zu", "xh", "st")

        for (locale in locales) {
            val translationFile = File(resDir, "values-$locale/strings.xml")
            assertTrue("Translation file missing: values-$locale/strings.xml", translationFile.exists())

            val translationMap = parseStringsFromXml(translationFile)
            val missingKeys = englishMap.keys.filterNot { translationMap.containsKey(it) }

            assertTrue(
                "Missing translation keys in values-$locale/strings.xml: $missingKeys",
                missingKeys.isEmpty()
            )

            // Verify format arguments (%1$s, %1$d, etc.) match
            for ((key, enValue) in englishMap) {
                val locValue = translationMap[key] ?: continue
                val enArgs = extractFormatArgs(enValue)
                val locArgs = extractFormatArgs(locValue)

                assertEquals(
                    "Format argument mismatch for key '$key' in locale '$locale': English has $enArgs but $locale has $locArgs",
                    enArgs,
                    locArgs
                )
            }
        }
    }

    private fun extractFormatArgs(text: String): List<String> {
        val list = mutableListOf<String>()
        val matcher = Pattern.compile("%[0-9]+\\$[a-z]").matcher(text)
        while (matcher.find()) {
            list.add(matcher.group())
        }
        return list.sorted()
    }

    private fun parseStringsFromXml(xmlFile: File): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)
        val stringNodes = doc.getElementsByTagName("string")

        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)

            val translatable = node.attributes?.getNamedItem("translatable")?.nodeValue
            if (translatable == "false") continue

            val nameAttr = node.attributes?.getNamedItem("name")?.nodeValue
            if (!nameAttr.isNullOrBlank()) {
                map[nameAttr] = node.textContent ?: ""
            }
        }
        return map
    }

    private fun findResDirectory(): File {
        var currentDir = File(System.getProperty("user.dir") ?: ".")
        while (currentDir.exists()) {
            val candidate = File(currentDir, "app/src/main/res")
            if (candidate.exists()) return candidate
            val candidateRoot = File(currentDir, "src/main/res")
            if (candidateRoot.exists()) return candidateRoot
            currentDir = currentDir.parentFile ?: break
        }
        return File("app/src/main/res")
    }
}