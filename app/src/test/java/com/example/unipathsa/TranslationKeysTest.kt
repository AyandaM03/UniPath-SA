package com.example.unipathsa

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TranslationKeysTest {

    @Test
    fun testAllEnglishStringKeysExistInTranslationFiles() {
        val resDir = findResDirectory()
        assertTrue("res directory not found at $resDir", resDir.exists())

        val englishFile = File(resDir, "values/strings.xml")
        assertTrue("English strings.xml not found", englishFile.exists())

        val englishKeys = parseStringKeysFromXml(englishFile)
        assertTrue("English strings.xml must contain string keys", englishKeys.isNotEmpty())

        val locales = listOf("zu", "xh", "st")

        for (locale in locales) {
            val translationFile = File(resDir, "values-$locale/strings.xml")
            assertTrue("Translation file missing: values-$locale/strings.xml", translationFile.exists())

            val translationKeys = parseStringKeysFromXml(translationFile)
            val missingKeys = englishKeys.filterNot { translationKeys.contains(it) }

            assertTrue(
                "Missing translation keys in values-$locale/strings.xml: $missingKeys",
                missingKeys.isEmpty()
            )
        }
    }

    private fun parseStringKeysFromXml(xmlFile: File): Set<String> {
        val keys = mutableSetOf<String>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)
        val stringNodes = doc.getElementsByTagName("string")

        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val nameAttr = node.attributes?.getNamedItem("name")?.nodeValue
            if (!nameAttr.isNullOrBlank()) {
                keys.add(nameAttr)
            }
        }
        return keys
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