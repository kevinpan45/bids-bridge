package tech.kp45.bids.bridge.pipeline;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.kp45.bids.bridge.pipeline.dao.PipelineMapper;

@Service
public class PipelineService {

    @Autowired
    private PipelineMapper pipelineMapper;

    public Pipeline get(Integer id) {
        return pipelineMapper.selectById(id);
    }

    public List<Pipeline> list() {
        return pipelineMapper.selectList(null);
    }
}
