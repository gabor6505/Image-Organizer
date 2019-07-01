package net.gabor6505.imageorganizer;

import java.util.ArrayList;

public class KeyBindMap {

    private final ArrayList<Integer> keyCodes = new ArrayList<>();
    private final ArrayList<String> folderNames = new ArrayList<>();

    public KeyBindMap() {

    }

    public void put(int code, String folder) {
        if (!keyCodes.contains(code)) {
            keyCodes.add(code);
            folderNames.add(keyCodes.indexOf(code), folder);
        } else {
            folderNames.set(keyCodes.indexOf(code), folder);
        }
    }

    public ArrayList<Integer> getKeyCodes() {
        return keyCodes;
    }

    public ArrayList<String> getFolderNames() {
        return folderNames;
    }

    public String replace(int code, String newFolderName) {
        int idx = keyCodes.indexOf(code);
        if (idx == -1) return null;
        String prevValue = folderNames.get(idx);
        folderNames.set(idx, newFolderName);
        return prevValue;
    }

    public void replaceCode(int oldCode, int newCode) {
        if (!keyCodes.contains(oldCode)) return;
        keyCodes.set(keyCodes.indexOf(oldCode), newCode);
    }

    public String get(Integer code) {
        if (!keyCodes.contains(code)) return null;
        return folderNames.get(keyCodes.indexOf(code));
    }

    public void remove(int code) {
        folderNames.remove(keyCodes.indexOf(code));
        keyCodes.remove((Integer) code);
    }
}
