package oracle.xml.parser.v2

import org.w3c.dom.Node

// Variant of DOMParser that allows retrieving XML line numbers from each node
class MyDOMParser: DOMParser() {
    private class MyDocumentBuilder: DocumentBuilder() {
        override fun addChild(node: XMLNode?) {
            super.addChild(node)
            node?.setUserData(LINE_NUMBER_ATTRIB, locator?.lineNumber ?: -1, null)
        }
    }

    override fun init() {
        hndl = MyDocumentBuilder()
        hndl.xmlParser = this
        hndl.err = parser.err
        parser.cntHandler = hndl
        parser.lexHandler = hndl
        parser.declHandler = hndl
        super.init()
    }

    companion object {
        private const val LINE_NUMBER_ATTRIB = "_lineNumber"

        val Node.customLineNumber: Int
            get() = getUserData(LINE_NUMBER_ATTRIB) as? Int ?: -1
    }
}
