package com.wzy.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.wzy.pojo.*;
import com.wzy.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户登录
 * @Auther: wzy
 * @Date: 2021/01/21/11:00
 * @Description:
 */
@Controller
@RequestMapping("/Login")
public class Login {

    // 用户服务，用于处理用户相关业务，例如验证用户登录信息等
    @Autowired
    private UserService userService;

    // 入住登记服务，用于查询入住登记相关记录等业务操作
    @Autowired
    private StayRegisterService stayRegisterService;

    // 登录日志服务，负责登录日志的保存、更新等操作
    @Autowired
    private LoginLogService loginLogService;

    // 房间设置服务，用于获取房间设置相关信息，例如查询所有房间设置情况
    @Autowired
    private RoomSetService roomSetService;

    // 格式化日期的格式，统一采用这种格式来处理日期的显示和存储相关逻辑
    @Autowired
    private UserServieAuto userServieAuto;

    @RequestMapping("/tologin")
    public String tologin() {

        return "/login/login";
    }

    /**
     * 处理用户访问登录页面的请求，直接返回登录页面的视图名称
     * @return 登录页面的视图名称，对应前端的登录页面模板
     */
    @RequestMapping("/quit")
    public ModelAndView tologin(HttpSession session) {
        Loginlog loginlog = (Loginlog) session.getAttribute("login");
        Date date = new Date();
        Loginlog log = new Loginlog();
        BeanUtils.copyProperties(loginlog, log);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.setExittime(format.format(date));
        loginLogService.updateLoginLogById(log);
        session.invalidate();

        List<RoomSetPo> list = roomSetService.selectAll();
        ModelAndView mv = null;
        mv = new ModelAndView("redirect:/index.jsp");
        mv.addObject("list", list);
        return mv;
    }
    /**
     * 处理用户退出登录的请求逻辑
     * 1. 更新登录日志中的退出时间信息
     * 2. 使当前会话失效，清除会话中的所有数据
     * 3. 查询所有房间设置信息，并将其添加到模型数据中，重定向到首页
     * @param session 当前的HTTP会话对象，用于获取登录日志信息以及使会话失效等操作
     * @return 重定向到首页的ModelAndView对象，包含了房间设置信息列表
     */
    @RequestMapping("/tomain")
    public ModelAndView tomain(UserPo user, HttpSession session) {
        System.out.println("登录！！！");
        ModelAndView mv = null;
        double zongFeiYongOne = 0;
        double zongFeiYongTwo = 0;
        UserPo u = userService.selectLogin(user);
        List<StayRegisterPo> list = stayRegisterService.selectAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getReceiveTargetID() == 2) {
                zongFeiYongOne += list.get(i).getSumConst();
            } else {
                zongFeiYongTwo += list.get(i).getSumConst();
            }
        }

        if (u != null) {
            if ("admin".equals(u.getUserName())) {
                mv = new ModelAndView("/main/main_admin");
            } else {
                mv = new ModelAndView("/main/main");
            }
            mv.addObject("user", u);
            if (session.getAttribute("login") == null) {
                Loginlog loginlog = new Loginlog();
                loginlog.setLoginname(u.getUserName());
                loginlog.setExittime(null);
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                loginlog.setLogintime(format.format(date));
                session.setAttribute("login", loginlog);
                loginLogService.saveLoginLog(loginlog);
            }
        } else {
            mv = new ModelAndView("/login/login");
            System.out.println("登录失败");
        }
        mv.addObject("zongFeiYongOne", zongFeiYongOne);
        mv.addObject("zongFeiYongTwo", zongFeiYongTwo);
        return mv;
    }


}
