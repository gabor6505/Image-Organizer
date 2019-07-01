package net.gabor6505.imageorganizer.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class XmlParser {

    public enum XmlParseResult {
        SUCCESS("Parse completed successfully!"),
        FILE_DOESNT_EXIST("The specified file doesn't exist!"),
        IO_ERROR("An IO error occurred while parsing!"),
        MALFORMED_DOCUMENT("The specified xml document is malformed!"),
        UNKNOWN("An unknown error happened while parsing!");

        private final String localizedMessage;

        XmlParseResult(String locMsg) {
            localizedMessage = locMsg;
        }

        public String getMessage() {
            return localizedMessage;
        }
    }

    private final static Map<String, Document> documentCache = new HashMap<>(0);

    private XmlParser() {

    }

    /**
     * Loads an xml file, and then passes it's data to a class that implements {@link IXmlDocumentEditor},
     * then saves the modifications made in the {@link IXmlDocumentEditor} back to disk
     * <br><br>
     * If a document in the same path was already loaded before, it won't get loaded again, instead the previously loaded instance will be returned
     * <br>
     * It can still be modified, because this method overwrites the document that already exists after editing
     *
     * @param filePath The path of the xml file to load
     * @param editor   The editor which handles modifying the document
     * @return The result of the xml parse operation
     */
    public static XmlParseResult editXml(String filePath, IXmlDocumentEditor editor) {
        try {
            // Read file
            File file = new File(filePath);
            if (!file.exists()) return XmlParseResult.FILE_DOESNT_EXIST;

            Document doc = getDocument(file);
            NodeList docNodes = new NodeList(doc.getDocumentElement().getChildNodes());

            // Process and modify it's contents
            boolean shouldSave = editor.editDocument(doc, docNodes);

            // Save to disk if it should be saved
            if (shouldSave) saveDocument(file, doc);

            return XmlParseResult.SUCCESS;
        } catch (SAXException e) {
            e.printStackTrace();
            return XmlParseResult.MALFORMED_DOCUMENT;
        } catch (IOException e) {
            e.printStackTrace();
            return XmlParseResult.IO_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return XmlParseResult.UNKNOWN;
        }
    }

    /**
     * Loads an xml file, and then passes it's data to a class that implements {@link IXmlDocumentViewer}
     * <br><br>
     * If a document in the same path was already loaded before, it won't get loaded again, instead the previously loaded instance will be returned
     *
     * @param filePath The path of the xml file to load
     * @param viewer   The viewer which handles viewing of the content and getting the information from the document
     * @return The result of the xml parse operation
     */
    public static XmlParseResult viewXml(String filePath, IXmlDocumentViewer viewer) {
        try {
            // Read xml
            File file = new File(filePath);
            if (!file.exists()) return XmlParseResult.FILE_DOESNT_EXIST;

            Document doc = getDocument(file);
            NodeList docNodes = new NodeList(doc.getDocumentElement().getChildNodes());

            // Process it's contents
            viewer.viewDocument(doc, docNodes);

            return XmlParseResult.SUCCESS;
        } catch (SAXException e) {
            e.printStackTrace();
            return XmlParseResult.MALFORMED_DOCUMENT;
        } catch (IOException e) {
            e.printStackTrace();
            return XmlParseResult.IO_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return XmlParseResult.UNKNOWN;
        }
    }

    private static Document getDocument(File file) throws Exception {
        if (documentCache.containsKey(file.getPath())) {
            return documentCache.get(file.getPath());
        } else {
            System.out.println("File at " + file.getPath() + " is not in cache, loading it...");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = null;
            while(doc == null) {
                try {
                    doc = dBuilder.parse(file);
                } catch (SAXParseException e) {
                    if (!e.getMessage().contains("Premature end of file")) {
                        throw e;
                    }
                }
            }

            documentCache.put(file.getPath(), doc);
            return doc;
        }
    }

    private static void saveDocument(File file, Document doc) throws Exception {
        // Fix spacing
        XPath xPath = XPathFactory.newInstance().newXPath();
        org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            org.w3c.dom.Node node = nodeList.item(i);
            node.getParentNode().removeChild(node);
        }

        // Write back to disk
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Create a new file if it somehow disappeared between loading it and editing it
        if (!file.exists()) file.createNewFile();

        System.out.println("Saving file to " + file.getPath() + " ...");
        DOMSource domSource = new DOMSource(doc);
        StreamResult sr = new StreamResult(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
        tf.transform(domSource, sr);
    }
}
