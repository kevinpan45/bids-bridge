package tech.kp45.bids.bridge.pipeline;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.extern.slf4j.Slf4j;
import tech.kp45.bids.bridge.common.exception.BasicRuntimeException;

@Slf4j
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

    public void create(Pipeline pipeline) {
        if (!StringUtils.hasText(pipeline.getName()) || !StringUtils.hasText(pipeline.getVersion())) {
            throw new BasicRuntimeException("name or version is empty");
        }

        if (!StringUtils.hasText(pipeline.getWorkflow())) {
            throw new BasicRuntimeException("workflow of job engine is empty");
        }
        pipelineMapper.insert(pipeline);
    }

    public Pipeline findByWorkflow(String workflow) {
        if (!StringUtils.hasText(workflow)) {
            throw new BasicRuntimeException("workflow is empty");
        }
        LambdaQueryWrapper<Pipeline> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pipeline::getWorkflow, workflow);
        return pipelineMapper.selectOne(queryWrapper);
    }
}
