package tech.kp45.bids.bridge.job.artifact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Service
public class ArtifactService {
    @Autowired
    private ArtifactMapper artifactMapper;

    public Artifact findByJob(Integer jobId) {
        LambdaQueryWrapper<Artifact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Artifact::getJobId, jobId);
        return artifactMapper.selectOne(queryWrapper);
    }

    public void create(Artifact artifact) {
        artifactMapper.insert(artifact);
    }

    public void deleteById(Integer id) {
        artifactMapper.deleteById(id);
    }
}
