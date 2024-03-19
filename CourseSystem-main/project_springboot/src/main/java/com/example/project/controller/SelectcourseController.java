package com.example.project.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.project.common.JsonResultUtil;
import com.example.project.common.Result;
import com.example.project.entity.*;
import com.example.project.mapper.SelectedCoursesMapper;
import com.example.project.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core._ArrayEnumeration;
import io.swagger.models.auth.In;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.sql.Types.NULL;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 *
 * @author ge
 * @since 2023-10-21
 */
@RestController
@RequestMapping("/api")
public class SelectcourseController {
    private static final Logger logger = LoggerFactory.getLogger(SelectcourseController.class);

    @Autowired
    private CurrentCoursesService currentCoursesService;

    @Autowired
    private CoursePlanService coursePlanService;

    @Autowired
    private SelectedCoursesService selectedCoursesService;

    @Autowired
    private TeachersService teachersService;

    @GetMapping("/list")
    public List<CurrentCourses> list() {
        return currentCoursesService.list();
    }

    @GetMapping("/courses")//根据给定条件查询预约记录
    public Result selclass(
        @RequestParam(name = "week_number", required = false) Integer week_number,
        @RequestParam(name = "day_number", required = false) Integer day_number,
        @RequestParam(name = "batch_number", required = false) Integer batch_number) throws JsonProcessingException {
        // `required = false` 表示这些参数是可选的，如果客户端未提供某个参数，它将为null或空字符串

        Integer weeknumber = week_number;
        Integer daynumber = day_number;
        Integer batchnumber = batch_number;


        List<CurrentCourses> list;//存放符合查询条件课程的列表

        if (batchnumber == null) { //无批次号
            if (weeknumber == null && daynumber != null) { //无周数 有天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getDayNumber, daynumber).list();//筛选出对应天数的批次
            }
            else if (weeknumber != null && daynumber == null) { //有周数 无天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getWeekNumber, weeknumber).list(); //筛选对应周数的批次
            }
            else { //有周数 有天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getWeekNumber, weeknumber)
                        .eq(CurrentCourses::getDayNumber, daynumber).list();
            }
        }
        else { //有批次号
            if (weeknumber == null && daynumber != null) { //无周数 有天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getDayNumber, daynumber) //筛选天数
                        .eq(CurrentCourses::getBatchNumber, batchnumber).list(); //筛选批次
            }
            else if (weeknumber != null && daynumber == null) { //有周数 无天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getWeekNumber, weeknumber) //筛选周数
                        .eq(CurrentCourses::getBatchNumber, batchnumber).list(); //筛选批次
            }
            else if (weeknumber != null && daynumber != null) { //有周数 有天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getWeekNumber, weeknumber) //筛选周数
                        .eq(CurrentCourses::getDayNumber, daynumber) //筛选天数
                        .eq(CurrentCourses::getBatchNumber, batchnumber).list(); //筛选批次
                //System.out.println("CurrentCourses list: " + list);
            }
            else { //无周数 无天数
                list = currentCoursesService.lambdaQuery()
                        .eq(CurrentCourses::getBatchNumber, batchnumber).list(); //筛选批次
            }
        }

        List<String> response = new ArrayList<>();
        Integer no;

        Courses courses = new Courses();

        for (int i = 0; i < list.size(); i++) {

            //weeknumber = list.get(i).getWeek_number();
            courses.setWeek_number(weeknumber);

            //daynumber = list.get(i).getDay_number();
            courses.setDay_number(daynumber);

            //batchnumber = list.get(i).getBatch_number();
            courses.setBatch_number(batchnumber);

            courses.setCapacity(50);

            no = list.get(i).getNo();
            List<SelectedCourses> selectno = selectedCoursesService.lambdaQuery()
                    .eq(SelectedCourses::getCurrentCourseId, no).list();//统计这门课程的已选人数

            courses.setSelected_number(selectno.size());//设置这门课程的已选人数

            //courses.setTime(list.get(i).getTime());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(courses);
            response.add(i, json);
        }

        return list.size() > 0 ? Result.suc(response, list.size()) : Result.fail();
    }

    @GetMapping("/students/{userId}/courses")//查询预约后的该用户的预约记录(通过userid)
    public Result selectedclass(@PathVariable("userId") String userId) throws JsonProcessingException {
        List<SelectedCourses> selectno = selectedCoursesService.lambdaQuery()//从全员预约记录中
                .eq(SelectedCourses::getStudentId, userId).list();//找到该用户的预约记录
        System.out.println("SelectedCourses list: " + selectno);

        if (selectno.size() == 0)//没有找到
            return Result.fail();//失败
        List<String> response = new ArrayList<>();
        Integer no;
        Integer week_number;
        Integer day_number;
        Integer batch_number;

        Courses courses = new Courses();

        for (int i = 0; i < selectno.size(); i++) { //遍历该用户全部预约记录

            no = selectno.get(i).getCurrentCourseId();//预约记录的批次序号

            List<CurrentCourses> list = currentCoursesService.lambdaQuery()//从批次计划中
                    .eq(CurrentCourses::getNo, no).list();//根据批次序号找到该批次
                    //.eq(CurrentCourses::getWeek_number, 2)//可以正确检索
            System.out.println("CurrentCourses list: " + list);

            if (list != null && !list.isEmpty()) {
                week_number = list.get(0).getWeekNumber();
                courses.setWeek_number(week_number);//设置周数

                day_number = list.get(0).getDayNumber();
                courses.setDay_number(day_number);//设置天数

                batch_number = list.get(0).getBatchNumber();
                courses.setBatch_number(batch_number);//设置批次
            }
            else{
                return Result.fail();//失败
            }
            courses.setCapacity(50);//设置最大容量
            no = list.get(0).getNo();
            List<SelectedCourses> selectno1 = selectedCoursesService.lambdaQuery()
                    .eq(SelectedCourses::getCurrentCourseId, no).list();
            courses.setSelected_number(selectno1.size());//设置该批次当前预约人数

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(courses);
            response.add(i, json);
        }

        return selectno.size() > 0 ? Result.suc(response, selectno.size()) : Result.fail();
    }

    @PostMapping("/students/{userId}/courses")//预约（选课）
    public Result selectedclass(@PathVariable("userId") String userid,
            @RequestBody List<Courses> courses) {//请求预约的批次信息：courses
        Integer i = null;
        if (userid != null) {
            i = Integer.valueOf(userid);
        }
        for (int j = 0; j < courses.size(); j++) {
            List<SelectedCourses> selectno = selectedCoursesService.lambdaQuery()//从全员预约记录中
                    .eq(SelectedCourses::getStudentId, userid).list();//查找该用户的预约记录
            if (!selectno.isEmpty()) {
                return Result.fail("已预约");//若已预约则报错
            }

            List<CurrentCourses> selectcourse = currentCoursesService.lambdaQuery()//从批次计划中
                    .eq(CurrentCourses::getWeekNumber, courses.get(j).getWeek_number())//找出当前预约请求的预约批次
                    .eq(CurrentCourses::getDayNumber, courses.get(j).getDay_number())
                    .eq(CurrentCourses::getBatchNumber, courses.get(j).getBatch_number()).list();

            List<SelectedCourses> num = selectedCoursesService.lambdaQuery()//从全员预约记录中
                    .eq(SelectedCourses::getCurrentCourseId, selectcourse.get(0).getNo()).list();//找出已经选了这门课的所有记录
            if (num.size() == 50)//统计是否超容量
                return Result.fail("预约容量已满");

            SelectedCourses selectedCourses = new SelectedCourses();//新建一条预约记录

            List<SelectedCourses> selectedCourse = selectedCoursesService.lambdaQuery()//从全员预约记录中
                    .eq(SelectedCourses::getStudentId, userid)//查找该用户的预约记录中
                    .eq(SelectedCourses::getCurrentCourseId, selectcourse.get(0).getNo()).list();//预约批次=当前预约批次的记录

            if (selectedCourse.size() > 0)//如果已经有该批次的预约记录
                return Result.fail();//报错
            else {
                selectedCourses.setCurrentCourseId(selectcourse.get(0).getNo());//将当前预约的批次信息（no）存入
                selectedCourses.setStudentId(i);
                boolean savecourse = selectedCoursesService.save(selectedCourses);
                if (savecourse)
                    continue;
                else
                    return Result.fail();
            }

        }
        return Result.suc();
    }

    @DeleteMapping("/students/{userId}/courses")
    public Result delcourse(@RequestBody List<Courses> courses,
            @PathVariable("userId") String userid) {
        Integer i = null;
        if (userid != null) {
            i = Integer.valueOf(userid);
        }
        for (int j = 0; j < courses.size(); j++) {
            List<CurrentCourses> selectcourse = currentCoursesService.lambdaQuery()//从批次计划中
                    .eq(CurrentCourses::getWeekNumber, courses.get(j).getWeek_number())//找出当前退约请求的预约批次
                    .eq(CurrentCourses::getDayNumber, courses.get(j).getDay_number())
                    .eq(CurrentCourses::getBatchNumber, courses.get(j).getBatch_number()).list();

            List<SelectedCourses> selectedCourse = selectedCoursesService.lambdaQuery()//从全员预约记录中
                    .eq(SelectedCourses::getStudentId, userid)//查找当前用户
                    .eq(SelectedCourses::getCurrentCourseId, selectcourse.get(0).getNo()).list();//对应的预约记录
            if (selectedCourse.size() == 0)
                return Result.fail();//没找到对应记录报错
            else {
                Map<String, Object> selmap = new HashMap<>();
                selmap.put("student_id", i);
                selmap.put("current_course_id", selectcourse.get(0).getNo());
                boolean savecourse = selectedCoursesService.removeByMap(selmap);
                if (savecourse)
                    continue;
                else
                    return Result.fail();
            }
        }
        return Result.suc();
    }

    /*@GetMapping("/students/{userId}/courses/score")
    public Result scoreclass(@PathVariable("userId") String userid) throws JsonProcessingException {

        List<SelectedCourses> selectno = selectedCoursesService.lambdaQuery()
                .eq(SelectedCourses::getStudentId, userid).list();
        if (selectno.size() == 0)
            return Result.fail();
        List<String> response = new ArrayList<>();
        Integer no;
        int courseid, teacherid;

        CourseScore courseScore = new CourseScore();

        for (int i = 0; i < selectno.size(); i++) {
            no = selectno.get(i).getCurrentCourseId();
            List<CurrentCourses> list = currentCoursesService.lambdaQuery()
                    .eq(CurrentCourses::getNo, no).list();
            courseid = list.get(0).getCourseId();
            courseScore.setCourse_id(courseid);
            List<CoursePlan> coursesname = coursePlanService.lambdaQuery()
                    .eq(CoursePlan::getCourseId, courseid).list();
            courseScore.setCourse_name(coursesname.get(0).getCourseName());
            teacherid = list.get(0).getTeacherId();
            List<Teachers> teachersname = teachersService.lambdaQuery()
                    .eq(Teachers::getId, teacherid).list();
            courseScore.setTeacher_name(teachersname.get(0).getName());
            courseScore.setScore(selectno.get(i).getScore());
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(courseScore);
            response.add(i, json);
        }

        return selectno.size() > 0 ? Result.suc(response, selectno.size()) : Result.fail();
    }*/

}
