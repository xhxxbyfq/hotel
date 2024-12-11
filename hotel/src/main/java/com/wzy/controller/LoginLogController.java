package com.wzy.controller;

import com.wzy.page.Page;
import com.wzy.pojo.Loginlog;
import com.wzy.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * 日志管理类，负责处理与登录日志相关的各种业务操作，如查询、更新、删除等
 *
 * @Auther: wzy
 * @Date: 2021/04/05/19:57
 * @Description:
 */
@Controller
@RequestMapping("/loginLog")
public class LoginLogController {

    // 注入登录日志服务，用于调用具体的业务逻辑方法
    @Autowired
    private LoginLogService loginLogService;

    // 从配置文件中读取的密码，用于管理员权限验证等（此处应考虑加密等安全机制改进）
    @Value("${pwd}")
    private String pwd;

    // 定义合法页码的正则表达式模式，用于校验传入的页码参数是否合法
    private static final Pattern PAGE_NUMBER_PATTERN = Pattern.compile("^[1-9]\\d*$");

    // 分页和模糊查询
    @RequestMapping("/tolist")
    public ModelAndView list(HttpServletRequest request, Integer currentPage, String txtname) {
        ModelAndView mv = new ModelAndView("/loginLog/list");
        Page<Loginlog> vo = new Page<Loginlog>();

        // 校验并设置当前页码，如果未传入或传入不合理则设置默认值为1
        currentPage = validateAndSetCurrentPage(currentPage);
        vo.setCurrentPage(currentPage);

        // 如果模糊查询文本为空，设置为空字符串
        txtname = StringUtils.isEmpty(txtname)? "" : txtname;

        try {
            vo = this.loginLogService.pageFuzzyselect(txtname, vo);
            mv.addObject("list", vo);
            mv.addObject("txtname", txtname);
        } catch (Exception e) {
            // 记录异常日志，此处可使用日志框架进行详细记录
            e.printStackTrace();
            // 根据实际情况，可以返回错误提示页面等更友好的反馈
            mv = new ModelAndView("errorPage");
        }

        return mv;
    }

    /**
     * 校验并设置当前页码，如果传入页码为空、为0或者不符合正整数格式，则设置为1
     *
     * @param currentPage 传入的当前页码参数
     * @return 校验后的当前页码
     */
    private Integer validateAndSetCurrentPage(Integer currentPage) {
        if (currentPage == null || currentPage == 0 ||!PAGE_NUMBER_PATTERN.matcher(currentPage.toString()).matches()) {
            currentPage = 1;
        }
        return currentPage;
    }

    @RequestMapping("/findLoginNameByuserName")
    public int findLoginNameByuserName(String userName, String userId) {
        try {
            int userIdAsInt = Integer.parseInt(userId);
            Loginlog loginlog = loginLogService.selectOne(userIdAsInt);
            return userName.equals(loginlog.getLoginname())? 1 : 0;
        } catch (NumberFormatException e) {
            // 记录异常日志
            e.printStackTrace();
            return -1; // 返回特殊值表示参数格式错误等异常情况
        } catch (Exception e) {
            // 处理其他可能出现的异常情况
            e.printStackTrace();
            return -1;
        }
    }

    @RequestMapping("/toupdate")
    public ModelAndView toupdate(String id) {
        ModelAndView mv = new ModelAndView("/loginLog/update");
        try {
            int idAsInt = Integer.parseInt(id);
            Loginlog loginlog = loginLogService.selectOne(idAsInt);
            mv.addObject("log", loginlog);
        } catch (NumberFormatException e) {
            // 记录异常日志
            e.printStackTrace();
            // 根据实际情况返回相应错误提示页面等
            mv = new ModelAndView("errorPage");
        } catch (Exception e) {
            // 处理其他可能的异常
            e.printStackTrace();
            mv = new ModelAndView("errorPage");
        }
        return mv;
    }

    @RequestMapping("/update")
    public ModelAndView update(Loginlog loginlog) {
        ModelAndView mv = null;
        loginLogService.updateLoginLogById(loginlog);
        mv = new ModelAndView("redirect:/loginLog/tolist.do");
        return mv;
    }
    @RequestMapping("/isAdmin")
    public boolean isAdmin(String adminPwd) {
        // 此处应改进为密码加密及验证的安全机制，比如对配置文件中的密码和传入密码进行哈希处理后比较
        return adminPwd.equals(pwd);
    }

    @RequestMapping("/delete")
    public ModelAndView delete(String ids) {
        ModelAndView mv = new ModelAndView();
        if (StringUtils.isEmpty(ids)) {
            // 如果传入的ID字符串为空，返回相应错误提示页面等
            mv.setViewName("errorPage");
            return mv;
        }
        String[] FenGe = ids.split(",");
        try {
            for (String id : FenGe) {
                int idAsInt = Integer.parseInt(id);
                loginLogService.deleteByUserId(idAsInt);
            }
            mv.setViewName("redirect:/loginLog/tolist.do");
        } catch (NumberFormatException e) {
            // 记录异常日志
            e.printStackTrace();
            mv.setViewName("errorPage");
        } catch (Exception e) {
            // 处理其他可能出现的异常情况
            e.printStackTrace();
            mv.setViewName("errorPage");
        }
        return mv;
    }
}