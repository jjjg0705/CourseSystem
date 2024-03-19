<template>
  <div class="common-layout">
    <el-container>

      <el-header class="top-bar">
        <el-row>

          <el-col :span="10">
            <div style="display: flex; align-items: center; justify-content: center; height: 100%;">
              <span class="text-xl font-bold" style="font-size: 55px;">游泳测试预约系统</span>
            </div>
          </el-col>

          <el-col :span="6" :offset="8">
            <div class="flex items-center justify-end h-full">
              <div style="display: flex; align-items: center; justify-content: center; height: 100%;  margin-top: 100px;">
                <span class="text-lg font-semibold mr-5" style="margin-left: 20px ; margin-right: 10px;">{{ this.userName
                }}</span>
                <span class="text-sm mr-4"
                  style="color: var(--el-text-color-regular); position: relative; top: 2px;margin-right: 10px;">{{
                    this.userId }}</span>
                <el-tag type="success" class="ml-2">学生</el-tag>
              </div>
            </div>
          </el-col>

        </el-row>
      </el-header>

      <el-container style="margin-top: 50px;">
        <el-aside width="200px">
          <el-menu default-active="1" class="el-menu-vertical-demo" style="display: flex; flex-direction: column; align-items: center;">
            <el-menu-item index="1" @click="selectFunction('预约测试')">预约测试</el-menu-item>
            <el-menu-item index="2" @click="selectFunction('取消预约'); fetchCourses()">取消预约</el-menu-item>
            <el-menu-item index="3" @click="selectFunction('预约查询'); fetchCourses()">预约查询</el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <div class="main-content-right">

            <div v-if="selectedFunction === '预约测试'">
              <el-form :model="queryInfo" label-width="100px">
                <el-row>
                  <el-col :span="8">
                    <el-form-item label="周数" prop="WeekNumber">
                      <el-select v-model="queryInfo.WeekNumber" placeholder="请选择第几周">
                        <el-option v-for="week in 10" :key="week" :label="`第 ${week} 周`" :value="week"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="时间" prop="DayNumber">
                      <el-select v-model="queryInfo.DayNumber" placeholder="请选择周几">
                        <el-option label="周一" value="1"></el-option>
                        <el-option label="周二" value="2"></el-option>
                        <el-option label="周三" value="3"></el-option>
                        <el-option label="周四" value="4"></el-option>
                        <el-option label="周五" value="5"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="批次" prop="BatchNumber">
                      <el-select v-model="queryInfo.BatchNumber" placeholder="请选择批次">
                        <el-option label="第一批次" value="1"></el-option>
                        <el-option label="第二批次" value="2"></el-option>
                        <el-option label="第三批次" value="3"></el-option>
                        <el-option label="第四批次" value="4"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>

              <div style="margin: 10px;">
                <el-button type="primary" @click="queryCourses">查询</el-button>
              </div>

              <div v-if="showForm">
                <el-table :data="courseInfo" style="width: 100%" @selection-change="handleSelectionChange">
                  <el-table-column type="selection"></el-table-column>
                  <el-table-column prop="week_number" label="周数" :formatter="weekFomat"></el-table-column>
                  <el-table-column prop="day_number" label="时间" :formatter="dayFomat"></el-table-column>
                  <el-table-column prop="batch_number" label="批次" :formatter="batchFomat"></el-table-column>
                  <el-table-column prop="capacity" label="预约容量"></el-table-column>
                  <el-table-column prop="selected_number" label="已选人数"></el-table-column>
                </el-table>
                <div style="margin: 10px;">
                  <el-button type="primary" @click="selectCourses">确认预约</el-button>
                </div>
              </div>
            </div>

            <div v-else-if="selectedFunction === '取消预约'">
              <div v-if="showselectedrecord">
                <el-table :data="myCourses" style="width: 100%" @selection-change="handleSelectionChange_delete">
                  <el-table-column type="selection"></el-table-column>
                  <el-table-column prop="week_number" label="周数" :formatter="weekFomat"></el-table-column>
                  <el-table-column prop="day_number" label="时间" :formatter="dayFomat"></el-table-column>
                  <el-table-column prop="batch_number" label="批次" :formatter="batchFomat"></el-table-column>
                  <el-table-column prop="capacity" label="预约容量"></el-table-column>
                  <el-table-column prop="selected_number" label="已选人数"></el-table-column>
                </el-table>
                <div style="margin: 10px;">
                  <el-button type="primary" @click="dropCourses">取消预约测试</el-button>
                </div>
              </div>
            </div>


            <div v-else-if="selectedFunction === '预约查询'">
              <div v-if="showselectedrecord">
                <el-table :data="myCourses" style="width: 100%">
                  <el-table-column prop="week_number" label="周数" :formatter="weekFomat"></el-table-column>
                  <el-table-column prop="day_number" label="时间" :formatter="dayFomat"></el-table-column>
                  <el-table-column prop="batch_number" label="批次" :formatter="batchFomat"></el-table-column>
                  <el-table-column prop="capacity" label="预约容量"></el-table-column>
                  <el-table-column prop="selected_number" label="已选人数"></el-table-column>
                </el-table>
              </div>
            </div>

          </div>
        </el-main>

      </el-container>
    </el-container>
  </div>
</template>

<script>
import axios from "axios";


import { ElMessage } from 'element-plus'

export default {
  name: "StudentPages",


  // 来自父组件的数据
  props: {

  },

  // 在created生命周期钩子中访问路由参数
  created() {
    // console.log("this.$route",this.$route);
    this.userId = this.$route.params.userId;
    this.userName = this.$route.params.userName;
    // console.log("userId", this.userId);
    // console.log("userName", this.userName);
  },

  // data()函数部分
  data() {
    return {
      host: "http://127.0.0.1:9000",
      selectedFunction: "选课", // 默认选中的功能

      // 选课功能中的输入框
      showForm: false,
      showselectedrecord:false,

      queryInfo: {
        WeekNumber: null,
        DayNumber: null,
        BatchNumber: null,
      },

      // 选课功能中的课程信息
      courseInfo: [{
        week_number: "week_number",
        day_number: "day_number",
        batch_number: "batch_number",
        capacity: 0,
        selected_number: 0
      }],

      // 选课功能中选中的课程号
      selectedCourses: [{
        week_number: "week_number",
        day_number: "day_number",
        batch_number: "batch_number",
        capacity: 0,
        selected_number: 0
      }],

      // 退课功能中选中的课程号
      deletedCourses: [{
        week_number: "week_number",
        day_number: "day_number",
        batch_number: "batch_number",
        capacity: 0,
        selected_number: 0
      }],

      // 学生已经选的课程
      myCourses: [{
        week_number: "week_number",
        day_number: "day_number",
        batch_number: "batch_number",
        capacity: 0,
        selected_number: 0
      }],

      // 学生成绩信息
      myScores: [{
        course_id: "",
        course_name: "",
        teacher_name: "",
        score: 0
      }]
    };
  },

  methods: {
    weekFomat(row) {
      const weekMap = {
        1: '第一周',
        2: '第二周',
        3: '第三周',
        4: '第四周',
        5: '第五周',
        6: '第六周',
        7: '第七周',
        8: '第八周',
        9: '第九周',
        10: '第十周'
      };
      return weekMap[row.week_number];
    },
    dayFomat(row) {
      const dayMap = {
        1: '周一',
        2: '周二',
        3: '周三',
        4: '周四',
        5: '周五'
      };
      return dayMap[row.day_number];
    },
    batchFomat(row) {
      const batchMap = {
        1: '第一批次',
        2: '第二批次',
        3: '第三批次',
        4: '第四批次'
      };
      return batchMap[row.batch_number];
    },

    // 选择业务功能
    selectFunction(functionName) {
      this.selectedFunction = functionName;
      this.showForm = false;
      this.queryInfo.WeekNumber = null;
      this.queryInfo.DayNumber = null;
      this.queryInfo.BatchNumber = null;
    },

    // 查询符合条件的课程
    async queryCourses() {
      const week_number = this.queryInfo.WeekNumber;
      const day_number = this.queryInfo.DayNumber;
      const batch_number = this.queryInfo.BatchNumber;

      // 构造请求体
      const apiUrl = `${this.host}/api/courses`;
      const queryParams = {
        week_number: week_number,
        day_number: day_number,
        batch_number: batch_number
      };
      await axios.get(apiUrl, { params: queryParams })
        .then(response => {

          // 将查询选课的结果显示到页面上
          const courseData = response.data.data;

          // 把courseData中的数据传递给this.courseInfo
          if (courseData != null) {
            // 显示响应结果
            ElMessage.success('预约批次信息查询成功');

            console.log("queryCourses method return response.data", response.data);
            // console.log("response.data.data: courseData", courseData);

            this.courseInfo = courseData.map((course) => {
              const selectedCourse = JSON.parse(course);
              return {
                week_number: selectedCourse.week_number,
                day_number: selectedCourse.day_number,
                batch_number: selectedCourse.batch_number,
                capacity: selectedCourse.capacity,
                selected_number: selectedCourse.selected_number,
              };
            });
            this.showForm = true;
          } else {
            ElMessage.error('预约批次信息查询失败');
          }
          console.log("this.courseInfo", this.courseInfo);
          // 显示表单组件

        }, error => {
          // 处理响应失败的情况
          console.error("预约批次信息查询失败", error);
          ElMessage.error('预约批次信息查询失败')
        })
    },

    // 查询已选课程
    async fetchCourses() {

      // 构造请求体
      const apiUrl = `${this.host}/api/students/${this.userId}/courses`;

      try {
        // 发送 GET 请求
        const response = await axios.get(apiUrl);

        console.log("return from fetchCourses, response: ", response);

        const courseData = response.data;
        this.myCourses = courseData.data.map(course => JSON.parse(course));
        console.log("this.myCourses", this.myCourses);
        this.showselectedrecord = true;

      } catch (error) {
        console.error("没有预约记录", error);
        ElMessage.error("没有预约记录");
      }
    },

    // 更新选课功能中的选中课程到selectedCourse
    handleSelectionChange(selectedRows) {
      console.log("选中的课程 selectedRows:", selectedRows);
      this.selectedCourses = selectedRows;
    },

    // 更新退课功能中的选中课程到deletedCourses
    handleSelectionChange_delete(selectedRows) {
      console.log("选中的课程 selectedRows:", selectedRows);
      this.deletedCourses = selectedRows;
    },

    // 预约功能
    async selectCourses() {
      try {
        // 创建一个空数组，用于存储请求体数据
        const requestBody = [];

        // 使用 forEach 方法遍历 selectedCourses 数组
        this.selectedCourses.forEach((course) => {
          // 将每个课程信息转换为一个包含课程信息的对象，并将其添加到 requestBody 数组中
          requestBody.push({
            week_number: course.week_number,
            day_number: course.day_number,
            batch_number: course.batch_number,
            capacity: course.capacity,
            selected_number: course.selected_number,
          });
        });

        console.log("预约请求发送的 requestBody", requestBody);

        const apiUrl = `${this.host}/api/students/${this.userId}/courses`;
        const response = await axios.post(apiUrl, requestBody);

        console.log("selectCourses return response: ", response);

        const result = response.data;
        if (result.code == 200) {
          ElMessage.success("预约成功");
          this.selectedCourses = []; // 清空已约批次
          this.fetchCourses(); // 重新现在（已预约）的预约记录
        }
        else if (result.code == 401) {
          ElMessage.warning(response.data.msg);
        }
        else {
          ElMessage.error("预约失败：" + result.message);
        }
      } catch (error) {
        console.error("预约操作失败", error);
        ElMessage.error("预约操作失败");
      }
    },

    // 取消预约功能
    async dropCourses() {
      // 发送请求进行取消预约操作
      try {
        const requestBody = [];

        console.log("deletedCourses", this.deletedCourses);

        this.deletedCourses.forEach((course) => {
          requestBody.push({
            week_number: course.week_number,
            day_number: course.day_number,
            batch_number: course.batch_number,
            capacity: course.capacity,
            selected_number: course.selected_number,
          });
        });

        console.log("取消预约请求发送的 requestBody", requestBody);

        const apiUrl = `${this.host}/api/students/${this.userId}/courses`;
        const response = await axios.delete(apiUrl, { data: requestBody });

        console.log("response return from dropCourses()", response);

        if (response.data.code == 200) {

          ElMessage.success("取消预约成功");
          this.deletedCourses = []; // 清空已选批次
          this.fetchCourses(); // 重新查询预约
          this.showselectedrecord = false;
        }
        else {
          ElMessage.error("取消预约失败：" + response.data.msg);
        }
      } catch (error) {
        console.error("取消预约操作失败", error);
        ElMessage.error("取消预约操作失败");
      }
    },

    mounted() {
      this.fetchCourses();
      this.showForm = false; // 隐藏表单组件
    }
  },
};
</script>

<style>
.top-bar {
  background: #46b4f0;
  color: #fff;
  padding: 50px;
  height: 200px;
  text-align: center;
  border-radius: 10px;
}

.top-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-name {
  font-weight: bold;
}

.main-content {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}

.sidebar {
  margin-top: 10px;
  width: 100px;
  background: #aee3ed;
  padding: 10px;
  border-radius: 10px;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  cursor: pointer;
  padding: 5px;
  border-bottom: 1px solid #ccc;
}

.main-content-right {
  flex: 1;
  padding: 20px;
}

.input-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

label {
  font-weight: bold;
  font-size: 14px;
  color: #333;
}

input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}


.course-table {
  margin-top: 20px;
  margin-bottom: 20px;
  border-collapse: collapse;
  font-family: Arial, sans-serif;
  background-color: #f2f2f2;
  width: 100%;
}

.course-table th,
.course-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.course-table th {
  background-color: #8ac9e2;
  color: white;
}
</style>
  