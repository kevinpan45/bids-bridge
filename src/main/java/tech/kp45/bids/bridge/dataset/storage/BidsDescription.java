package tech.kp45.bids.bridge.dataset.storage;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import tech.kp45.bids.bridge.exception.BasicRuntimeException;

@Data
public class BidsDescription {
    private String content;

    public BidsDescription(String content) {
        this.content = content;
        if (JSONUtil.isTypeJSONObject(content)) {
            JSONObject contentJson = JSONUtil.parseObj(content);

        } else {
            throw new BasicRuntimeException("Dataset description file dataset_description.json is not valid");
        }
    }

    public BidsDataset toBidsDataset() {
        // TODO 
        return null;
    }

}
