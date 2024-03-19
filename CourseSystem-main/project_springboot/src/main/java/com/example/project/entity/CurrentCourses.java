package com.example.project.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author ge
 * @since 2023-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="CurrentCourses对象", description="")
public class CurrentCourses implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer no;

    @TableField(value = "week_number")
    private Integer weekNumber;

    private Integer dayNumber;

    private Integer batchNumber;


    //已选人数
    @TableField(exist = false)
    private Integer selectedCount;

    //课程容量
    @TableField(exist = false)
    private Integer capacity;
}
