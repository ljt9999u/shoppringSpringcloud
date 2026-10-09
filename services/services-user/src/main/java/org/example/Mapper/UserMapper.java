package org.example.Mapper;

import org.example.User.UserPOJO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper {

    /**
     * 注册用户 - 插入新用户（含昵称、邮箱、头像、性别等资料字段）
     */
    @Insert("insert into user (username, password, phone, email, avatar, nickname, gender, role_code, status) " +
            "values (#{username}, #{password}, #{phone}, #{email}, #{avatar}, #{nickname}, #{gender}, #{roleCode}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPOJO userPOJO);

    /**
     * 根据手机号查询用户（登录用）
     */
    @Select("select * from user where phone = #{phone}")
    UserPOJO findByPhone(String phone);

    /**
     * 根据用户名查询用户（注册时校验唯一性）
     */
    @Select("select * from user where username = #{username}")
    UserPOJO findByUsername(String username);

    /**
     * 根据 ID 查询用户
     */
    @Select("select * from user where id = #{id}")
    UserPOJO findById(Long id);

    /**
     * 修改个人资料（昵称、邮箱、头像、性别）
     */
    @Update("update user set nickname = #{nickname}, email = #{email}, avatar = #{avatar}, gender = #{gender} " +
            "where id = #{id}")
    int updateProfile(UserPOJO userPOJO);

    /**
     * 修改密码
     */
    @Update("update user set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 修改账号状态（0禁用 1启用，管理端）
     */
    @Update("update user set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 修改用户角色（商家入驻审核通过时由商家服务经 Feign 调用）
     */
    @Update("update user set role_code = #{roleCode} where id = #{id}")
    int updateRole(@Param("id") Long id, @Param("roleCode") String roleCode);

    /**
     * 用户总数
     */
    @Select("select count(*) from user")
    long count();

    /**
     * 分页查询用户（管理端）
     */
    @Select("select * from user order by id desc limit #{offset}, #{pageSize}")
    List<UserPOJO> findPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
}
