package org.example.Mapper;

import org.example.User.UserPOJO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper {

    /**
     * 注册用户 - 插入新用户
     */
    @Insert("insert into user (username, password, phone, role_code, status) " +
            "values (#{username}, #{password}, #{phone}, #{roleCode}, #{status})")
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
}
