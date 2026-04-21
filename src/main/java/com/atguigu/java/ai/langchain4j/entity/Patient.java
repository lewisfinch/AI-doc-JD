package com.atguigu.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 就诊人实体
 */
@TableName("patient")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前登录用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 就诊人姓名
     */
    @TableField("name")
    private String name;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 与用户关系
     */
    @TableField("relation")
    private String relation;

    /**
     * 既往病史
     */
    @TableField("medical_history")
    private String medicalHistory;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public Patient setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public Patient setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Patient setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public Patient setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public Patient setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getRelation() {
        return relation;
    }

    public Patient setRelation(String relation) {
        this.relation = relation;
        return this;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public Patient setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Patient setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Patient setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public Patient setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }
}