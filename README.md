# ImageOrganizer
Simple Java utility app for sorting images

**Tested with Java 8 & 12 on Windows 10**

### Usage
1. Select a folder containing your images you want to sort
2. Set keybinds and specify which keybind should move image to what folder name
3. Navigate between the images using the arrow keys, and press any key you assigned to move the currently visible image to that subfolder

After moving an image using an assigned keybind, the image will still be among the other images until you close the app

**Caching:** If checked, the app will load all images inside the selected directory into RAM, to allow for much faster image viewing

### TODOs
* Only cache images in proximity of currently visible image, to significantly reduce RAM consumption
* Add support for non-US layout keys (like á, é, ó)
* Implement some kind of way to send images to the platform-specific recycle bin/trash
* Load images from 1st level subfolders as well, so that images previously moved could also be viewed after app restart (should be toggleable)
