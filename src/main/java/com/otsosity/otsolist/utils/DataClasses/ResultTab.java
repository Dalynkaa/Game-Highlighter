
package com.otsosity.otsolist.utils.DataClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ResultTab {

    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("status")
    @Expose
    private Boolean status;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ResultTab() {
    }
    /**
     *
     * @param status
     * @param data
     */
    public ResultTab(Boolean status, Data data) {
        super();
        this.data = data;
        this.status = status;
    }


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public ResultTab withData(Data data) {
        this.data = data;
        return this;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public ResultTab withStatus(Boolean status) {
        this.status = status;
        return this;
    }

}
