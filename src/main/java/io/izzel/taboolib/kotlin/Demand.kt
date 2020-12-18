package io.izzel.taboolib.kotlin

/**
 * @author bkm016
 * @since 2020/11/22 2:51 下午
 */
class Demand(val source: String) {

    val namespace: String
    val dataMap = HashMap<String, String>()
    val args = ArrayList<String>()

    init {
        val args = source.split(" ")
        namespace = args[0]
        var key: String? = null
        val boxes = ArrayList<String>()
        var isText = false
        args.forEachIndexed { index, box ->
            if (index > 0) {
                if (isText) {
                    if (box.endsWith("\"") && !box.endsWith("\\\"")) {
                        boxes.add(box.substring(0, box.length - 1))
                        isText = false
                        if (key != null) {
                            dataMap[key!!] = boxes.joinToString(" ").replace("\\\"", "\"")
                            key = null
                        } else {
                            this.args.add(boxes.joinToString(" "))
                        }
                        boxes.clear()
                    } else {
                        boxes.add(box)
                    }
                } else {
                    if (box.startsWith("\"")) {
                        boxes.add(box.substring(1))
                        isText = true
                    } else {
                        if (box.startsWith("-") && key == null) {
                            key = box.substring(1)
                        } else if (key != null) {
                            dataMap[key!!] = box
                            key = null
                        } else {
                            this.args.add(box)
                        }
                    }
                }
            }
        }
    }

    fun get(key: List<String>, def: String? = null): String? {
        return key.mapNotNull { get(it) }.firstOrNull() ?: def
    }

    fun get(key: String, def: String? = null): String? {
        return dataMap[key] ?: def
    }

    fun get(index: Int, def: String? = null): String? {
        return args.getOrNull(index) ?: def
    }

    override fun toString(): String {
        return "Demand(source='$source', namespace='$namespace', dataMap=$dataMap, args=$args)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Demand) return false
        if (source != other.source) return false
        return true
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }
}