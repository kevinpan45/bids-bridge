package tech.kp45.bids.bridge.iam.mapper;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tech.kp45.bids.bridge.iam.entity.User;

public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM iam_user WHERE username = #{username}")
    User findByUsername(String username);
}
