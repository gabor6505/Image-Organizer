package net.gabor6505.imageorganizer;

import net.gabor6505.imageorganizer.xml.Node;
import net.gabor6505.imageorganizer.xml.NodeList;
import net.gabor6505.imageorganizer.xml.XmlParser;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PreferenceManager {

    public final static String APP_DIRECTORY_NAME = System.getProperty("user.home") + "/.imageorganizer";
    public final static File APP_DIRECTORY = new File(APP_DIRECTORY_NAME);

    public final static File KEYBIND_CONFIG_FILE = new File(APP_DIRECTORY_NAME + "/keybinds.xml");
    public final static String KEYBIND_ROOT_NODE_NAME = "KeyBinds";
    public final static String KEYBIND_NODE_NAME = "KeyBind";

    private final static List<String> INITIAL_XML_LINES = Arrays.asList(
            "<root>",
            "    <KeyBinds>",
            //"        <KeyBind code=\"69\">Example</KeyBind>",
            "    </KeyBinds>",
            "</root>");

    // TODO move to a different map implementation (probably 2 arraylists), bc keys cant be edited in a way which preserves the order of entries
    private static Map<Integer, String> keyBindMap = new LinkedHashMap<>();

    static {
        AtomicBoolean docNeedsRepair = new AtomicBoolean(false);

        // Create app dir if it doesnt exist
        if (!APP_DIRECTORY.exists()) APP_DIRECTORY.mkdir();

        // Create config file if it doesnt exist
        if (!KEYBIND_CONFIG_FILE.exists()) {
            Path file = KEYBIND_CONFIG_FILE.toPath();
            try {
                Files.write(file, INITIAL_XML_LINES, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("Error occurred while writing initial config file!");
                e.printStackTrace();
            }
        }

        // Try reading the config file and load the keybinds
        XmlParser.XmlParseResult result = XmlParser.viewXml(KEYBIND_CONFIG_FILE.getPath(), (doc, nodes) -> {
            if (nodes.getNodes(KEYBIND_ROOT_NODE_NAME).size() > 0) loadKeyBinds(nodes);
            else docNeedsRepair.set(true);
        });

        if (result != XmlParser.XmlParseResult.SUCCESS || docNeedsRepair.get()) {
            System.out.println("Error occurred while loading keybinds, attempting to repair the config file...");
            XmlParser.editXml(KEYBIND_CONFIG_FILE.getPath(), (doc, nodes) -> {
                if (nodes.getNodes(KEYBIND_ROOT_NODE_NAME).size() > 0) {
                    loadKeyBinds(nodes);
                    return false;
                } else {
                    Element keyBinds = doc.createElement(KEYBIND_ROOT_NODE_NAME);

                    /*Element exampleKeyBind = doc.createElement(KEYBIND_NODE_NAME);
                    exampleKeyBind.setAttribute("code", String.valueOf(69));
                    exampleKeyBind.appendChild(doc.createTextNode("Example"));*/

                    doc.getDocumentElement().appendChild(keyBinds);
                    return true;
                }
            });
        }
    }

    private static void loadKeyBinds(NodeList nodes) {
        Node keyBinds = nodes.getNode(KEYBIND_ROOT_NODE_NAME);
        NodeList keyBindList = keyBinds.getNodes(KEYBIND_NODE_NAME);

        for (Node keyBind : keyBindList) {
            keyBindMap.put(Integer.parseInt(keyBind.getNodeAttributeContent("code")), keyBind.getTextContent());
            System.out.println(String.format("Loaded KeyBind(%s, %s)", keyBind.getNodeAttributeContent("code"), keyBind.getTextContent()));
        }
    }

    private PreferenceManager() {

    }

    public static Map<Integer, String> getKeyBindMap() {
        return keyBindMap;
    }

    public static ArrayList<Integer> getOccupiedKeyCodes() {
        return new ArrayList<>(keyBindMap.keySet());
    }

    public static void updateKeyBindFolder(int code, String newFolderName) {
        System.out.println("Updated keybind folder!");
        String prevValue = keyBindMap.replace(code, newFolderName);
        if (prevValue != null) saveKeyBindsToDisk();
    }

    public static void updateKeyBindCode(int oldCode, int newCode) {
        System.out.println("Updated keybind code!");
        String folderName = keyBindMap.get(oldCode);
        keyBindMap.remove(oldCode);
        keyBindMap.put(newCode, folderName);
        saveKeyBindsToDisk();
    }

    public static void addKeyBind(int code, String folderName) {
        System.out.println("Added keybind!");
        keyBindMap.put(code, folderName);
        saveKeyBindsToDisk();
    }

    public static void removeKeyBind(int code) {
        System.out.println("Removed keybind!");
        keyBindMap.remove(code);
        saveKeyBindsToDisk();
    }

    private static void saveKeyBindsToDisk() {
        XmlParser.editXml(KEYBIND_CONFIG_FILE.getPath(), (doc, nodes) -> {
            if (nodes.getNodes(KEYBIND_ROOT_NODE_NAME).size() > 0) {
                org.w3c.dom.Node node = nodes.getNode(KEYBIND_ROOT_NODE_NAME).getNode();
                node.getParentNode().removeChild(node);
            }

            Element keyBinds = doc.createElement(KEYBIND_ROOT_NODE_NAME);

            for (Integer code : keyBindMap.keySet()) {
                Element keyBind = doc.createElement(KEYBIND_NODE_NAME);
                keyBind.setAttribute("code", String.valueOf(code));
                keyBind.appendChild(doc.createTextNode(keyBindMap.get(code)));
                keyBinds.appendChild(keyBind);
            }

            doc.getDocumentElement().appendChild(keyBinds);
            return true;
        });
    }
}
