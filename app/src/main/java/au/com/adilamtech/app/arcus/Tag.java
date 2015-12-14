package au.com.adilamtech.app.arcus;



/**
 * Created by Gabriel on 12/13/15.
 */
public class Tag {

    private String itemId;
    private String itemIdHex;
    private boolean isFound;



    public Tag(String id) {
        itemId = id;
        itemIdHex = asciiToHex(itemId);
        isFound = false;
    }


    private String asciiToHex(String ascii) {

        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < ascii.length(); i++) {

            hex.append(Integer.toHexString(ascii.charAt(i)));
        }

        return hex.toString().toUpperCase();
    }


    public String getItemId() {
        return itemId;
    }

    public boolean isFound() {
        return isFound;
    }

    public void found() {
        this.isFound = true;
    }

    public String getItemIdHex() {
        return itemIdHex;
    }
}