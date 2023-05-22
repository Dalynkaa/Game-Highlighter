
package com.otsosity.otsolist.utils.DataClasses;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Data {

    @SerializedName("display_color")
    @Expose
    private String displayColor;
    @SerializedName("prefix")
    @Expose
    private String prefix;
    @SerializedName("prefix_color")
    @Expose
    private String prefixColor;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    /**
     * 
     * @param prefixColor
     * @param prefix
     * @param displayColor
     */
    public Data(String displayColor, String prefix, String prefixColor) {
        super();
        this.displayColor = displayColor;
        this.prefix = prefix;
        this.prefixColor = prefixColor;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public void setDisplayColor(String displayColor) {
        this.displayColor = displayColor;
    }

    public Data withDisplayColor(String displayColor) {
        this.displayColor = displayColor;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Data withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getPrefixColor() {
        return prefixColor;
    }

    public void setPrefixColor(String prefixColor) {
        this.prefixColor = prefixColor;
    }

    public Data withPrefixColor(String prefixColor) {
        this.prefixColor = prefixColor;
        return this;
    }

}
