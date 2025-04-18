package tech.kp45.bids.bridge.iam.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tech.kp45.bids.bridge.iam.entity.Role;
import tech.kp45.bids.bridge.iam.entity.UserRole;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT r.* FROM iam_role r, iam_user_role ur WHERE ur.user_id = #{id}")
    List<Role> findRolesByUserId(Integer id);

}
