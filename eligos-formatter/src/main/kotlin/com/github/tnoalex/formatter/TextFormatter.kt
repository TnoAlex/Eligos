package com.github.tnoalex.formatter


class TextFormatter : IFormatter {
    override val fileExtension: String
        get() = "txt"

    override fun format(obj: Any): String {
        val builder = StringBuilder()
        render(obj, builder, 0)
        val text = builder.toString()
        return text
    }

    private fun render(content: Any, builder: StringBuilder, nest: Int) {
        when (content) {
            is Number, is Boolean -> {
                builder.append("\t".repeat(nest)).append(content).append("\n")
            }

            is String -> {
                val prefix = "\t".repeat(nest)
                if (content.contains("\n")) {
                    content.split("\n").forEach {
                        builder.append(prefix).append(it).append("\n")
                    }
                } else {
                    builder.append(prefix).append(content).append("\n")
                }
            }

            is List<*> -> {
                content.forEach {
                    render(it!!, builder, nest)
                }
            }

            is Map<*, *> -> {
                val prefix = "\t".repeat(nest)
                content.forEach { (k, v) ->
                    builder.append(prefix).append(k).append(":")
                    if (v is String || v is Number) {
                        val value = v.toString()
                        if (value.length < 10) {
                            builder.append(" ").append(v).append("\n")
                        } else {
                            builder.append("\n")
                            render(v, builder, nest + 1)
                        }
                    } else {
                        builder.append("\n")
                        render(v!!, builder, nest + 1)
                    }
                }
            }

            else -> throw RuntimeException("Can not create render text")
        }
    }
}