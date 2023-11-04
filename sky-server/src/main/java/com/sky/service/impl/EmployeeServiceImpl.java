package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!employee.getPassword().equals(md5Password)) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    public Employee enroll(EmployeeDTO employeeDTO) {
        Employee domain = new Employee();
        BeanUtils.copyProperties(employeeDTO, domain);
        domain.setStatus(StatusConstant.ENABLE);
        domain.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        domain.setCreateTime(LocalDateTime.now());
        domain.setUpdateTime(LocalDateTime.now());

        domain.setCreateUser(BaseContext.getCurrentId());
        domain.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(domain);

        return domain;
    }

    public PageResult pagemation(EmployeePageQueryDTO item){
        PageHelper.startPage(item.getPage(), item.getPageSize());
        Page<Employee> domain =  employeeMapper.pagemation(item);
        System.out.print(domain.getResult().size());
        return new PageResult(domain.getTotal(), domain.getResult());
    }

    public void forbidden(Integer status, Long id) {
        Employee domain = new Employee();
        domain.setId(id);
        domain.setStatus(status);

        employeeMapper.updateStatus(domain);

    }

    public Employee getById(Long id) {
       Employee domain = employeeMapper.getbyid(id);
       return domain;
    }

    public boolean updateEmployeeInfo(EmployeeDTO employee){
        if (getById(employee.getId()) == null){
            return false;
        }

        Employee domain = new Employee();
        BeanUtils.copyProperties(employee, domain);
        domain.setUpdateTime(LocalDateTime.now());
        domain.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateStatus(domain);

        return true;
    }

}
