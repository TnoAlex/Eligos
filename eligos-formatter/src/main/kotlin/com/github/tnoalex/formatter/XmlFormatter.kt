package com.github.tnoalex.formatter

import com.github.tnoalex.formatter.utils.encodeXml
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class XmlFormatter : IFormatter {
    override val fileExtension: String
        get() = "xml"

    override fun format(obj: Any): String {
        val domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = domBuilder.newDocument()
        val rootElement = document.createElement("root")
        document.appendChild(rootElement)
        val encodedContent = encodeXml(obj)
        createDom(encodedContent, document, rootElement)
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty("indent", "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val source = DOMSource(document)
        val writer = StringWriter()
        val result = StreamResult(writer)
        transformer.transform(source, result)
        val xmlString = writer.toString()
        return xmlString
    }

    private fun createDom(element: Any, document: Document, root: Element) {
        when (element) {
            is String, is Number -> {
                root.textContent = element.toString()
            }

            is List<*> -> {
                element.forEach {
                    val nextElement = document.createElement("item")
                    createDom(it!!, document, nextElement)
                    root.appendChild(nextElement)
                }
            }

            is Map<*, *> -> {
                element.forEach { (k, v) ->
                    val entry = document.createElement("Entry")
                    val key = document.createElement("key")
                    key.textContent = k.toString()
                    entry.appendChild(key)
                    val value = document.createElement("value")
                    createDom(v!!, document, value)
                    entry.appendChild(value)
                    root.appendChild(entry)
                }
            }

            else -> throw RuntimeException("Can not create xml dom")
        }
    }

}