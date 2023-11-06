package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping("/enroll")
    @ApiOperation("employee enroll")
    public Result enroll(@RequestBody EmployeeDTO item) {
        employeeService.enroll(item);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("employee pagemation")
    public Result<PageResult> pagemation(EmployeePageQueryDTO item){
        PageResult pageResult = employeeService.pagemation(item);
        return Result.success(pageResult);
    }

    @PostMapping("/forbidden/{status}")
    @ApiOperation("employee forbidden")
    public Result forbidden(@PathVariable Integer status, Long id){
        employeeService.forbidden(status, id);
        return Result.success();
    }

    @PostMapping("/getbyid/{id}")
    @ApiOperation("employee getbyid")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    @PostMapping("/updateEmployeeInfo")
    @ApiOperation("employee updateEmployeeInfo")
    public Result updateEmployeeInfo(@RequestBody EmployeeDTO employee){
        boolean res =  employeeService.updateEmployeeInfo(employee);
        if (!res){
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("employee delete")
    public Result delete(@PathVariable Long id){
        boolean res = employeeService.delete(id);
        if (!res){
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        return Result.success();
    }

}
