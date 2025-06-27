package tech.kp45.bids.bridge.job;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tech.kp45.bids.bridge.bff.JobView;

public interface JobMapper extends BaseMapper<Job> {

    @Select("SELECT j.*, p.`name` AS pipelineName, p.version AS pipelineVersion, d.`name` AS datasetName, d.version AS datasetVersion, d.doi AS datasetDoi FROM job j, pipeline p, dataset d WHERE j.pipeline_id = p.id AND j.dataset_id = d.id AND (#{group} IS NULL OR j.group = #{group})")
    List<JobView> listJobViews(String group);

}
