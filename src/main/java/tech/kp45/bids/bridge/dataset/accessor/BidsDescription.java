package tech.kp45.bids.bridge.dataset.accessor;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import tech.kp45.bids.bridge.collection.dataset.BidsDataset;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Data
public class BidsDescription {
    private String content;
    private JSONObject contentJson;

    private String name;
    private String bidsVersion;
    private String datasetType;
    private String license;
    private String doi;

    public BidsDescription(String content) {
        this.content = content;
        if (JSONUtil.isTypeJSONObject(content)) {
            contentJson = JSONUtil.parseObj(content);
            this.name = contentJson.getStr("Name");
            this.bidsVersion = contentJson.getStr("BIDSVersion");
            this.datasetType = contentJson.getStr("DatasetType");
            this.license = contentJson.getStr("License");
            this.doi = contentJson.getStr("DatasetDOI");
        } else {
            throw new BasicRuntimeException("Dataset description file dataset_description.json is not valid");
        }
    }

    public BidsDataset toBidsDataset() {
        BidsDataset bidsDataset = new BidsDataset();
        bidsDataset.setName(this.name);
        bidsDataset.setBidsVersion(this.bidsVersion);
        bidsDataset.setDataType(this.datasetType);
        // Suitable for OpenNeuro
        bidsDataset.setVersion(this.contentJson.getStr("Version"));
        bidsDataset.setDoi(doi);
        return bidsDataset;
    }

}
