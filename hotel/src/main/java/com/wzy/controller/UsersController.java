package com.wzy.controller;

import com.google.gson.Gson;
import com.wzy.page.Page;
import com.wzy.pojo.AttributePo;
import com.wzy.pojo.RoomSetPo;
import com.wzy.pojo.UserPo;
import com.wzy.pojo.Users;
import com.wzy.service.UserService;
import com.wzy.service.UserServieAuto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户
 * 该类作为用户相关操作的控制器，处理各种与用户相关的HTTP请求，如查询用户列表、添加用户、更新用户信息、删除用户等操作。
 *
 * @Auther: wzy
 * @Date: 2021/03/25/9:00
 * @Description:
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    // 自动注入用户相关的服务层接口实现类，用于处理复杂的业务逻辑，比如数据库操作等
    @Autowired
    private UserServieAuto userServiceAuto;
    // 注入另一个用户服务接口实现类，可能用于不同的业务场景下的用户操作
    @Autowired
    private UserService userService;

    /**
     * 处理获取用户列表的请求，支持分页和模糊查询功能。
     * 对传入的当前页码和用户名参数进行校验后，调用服务层方法获取分页数据，并将数据添加到视图模型中返回。
     *
     * @param request       HttpServletRequest对象，用于获取请求相关的信息（当前方法暂未使用）
     * @param currentPage   当前页码，用于分页查询，若传入为null或者小于等于0，则默认设置为1
     * @param userName      用户名，用于模糊查询用户，若传入为null，则默认设置为空字符串
     * @return ModelAndView对象，包含视图名称和查询到的用户数据，视图名称为 "/users/list"
     */
    @RequestMapping("/tolist")
    public ModelAndView tolist(HttpServletRequest request, Integer currentPage, String userName) {
        ModelAndView mv = new ModelAndView("/users/list");

        // 校验当前页码
        currentPage = validateCurrentPage(currentPage);

        // 校验用户名
        userName = validateUserName(userName);

        // 创建分页对象并获取用户数据，调用服务层的pageFuzzyselect方法进行分页模糊查询
        Page<Users> userPage = createUserPage(currentPage, userName);

        // 添加数据到视图，将查询到的用户分页数据添加到ModelAndView中，同时添加用户名用于页面展示等用途
        mv.addObject("list", userPage);
        mv.addObject("userName", userName);

        return mv;
    }

    /**
     * 校验当前页码参数的合法性，若为null或者小于等于0，则返回默认页码1，否则返回传入的页码值。
     *
     * @param currentPage 传入的当前页码参数
     * @return 合法的当前页码值
     */
    private int validateCurrentPage(Integer currentPage) {
        if (currentPage == null || currentPage <= 0) {
            return 1; // 默认页码为1
        }
        return currentPage;
    }

    /**
     * 校验用户名参数的合法性，若为null，则返回空字符串，否则返回传入的用户名。
     *
     * @param userName 传入的用户名参数
     * @return 合法的用户名值
     */
    private String validateUserName(String userName) {
        return (userName == null)? "" : userName; // 默认用户名为空字符串
    }

    /**
     * 创建用于分页查询的Page对象，并调用服务层的pageFuzzyselect方法进行分页模糊查询，获取用户数据。
     *
     * @param currentPage 当前页码
     * @param userName    用户名
     * @return 查询到的包含用户数据的Page对象
     */
    private Page<Users> createUserPage(int currentPage, String userName) {
        Page<Users> page = new Page<>();
        page.setCurrentPage(currentPage);
        return userServiceAuto.pageFuzzyselect(userName, page);
    }

    /**
     * 普通的获取用户列表请求处理方法（功能与tolist类似，但可能存在一些细节差异，从代码看主要是视图名称不同）。
     * 同样对当前页码和用户名参数进行校验，获取用户分页数据后添加到视图模型中返回。
     *
     * @param request       HttpServletRequest对象，用于获取请求相关的信息（当前方法暂未使用）
     * @param currentPage   当前页码，用于分页查询，若传入为null或者等于0，则默认设置为1
     * @param userName      用户名，用于模糊查询用户，若传入为null，则默认设置为空字符串
     * @return ModelAndView对象，包含视图名称和查询到的用户数据，视图名称为 "/users/list_user"
     */
    @RequestMapping("/toListUser")
    public ModelAndView tolist_normal(HttpServletRequest request, Integer currentPage, String userName) {
        ModelAndView mv = null;
        mv = new ModelAndView("/users/list_user");
        Page<Users> vo = new Page<Users>();
        if (currentPage == null) {
            currentPage = 1;
        } else if (currentPage == 0) {
            currentPage = 1;
        }
        if (userName == null) {
            userName = "";
        }
        vo.setCurrentPage(currentPage);
        vo = userServiceAuto.pageFuzzyselect(userName, vo);
        mv.addObject("list", vo);
        mv.addObject("userName", userName);
        return mv;
    }

    /**
     * 处理跳转到添加用户页面的请求，返回指向 "/users/add" 页面的ModelAndView对象。
     *
     * @return ModelAndView对象，视图名称为 "/users/add"，用于展示添加用户的页面
     */
    @RequestMapping("/toadd")
    public ModelAndView toadd() {
        ModelAndView mv = null;
        mv = new ModelAndView("/users/add");
        return mv;
    }

    /**
     * 处理添加用户的请求，调用服务层的insertUsers方法将传入的用户对象插入数据库，
     * 操作完成后重定向到用户列表页面（"/users/tolist.do"）。
     *
     * @param user 要添加的用户对象，包含了用户相关的各种信息
     * @return ModelAndView对象，重定向到用户列表页面的视图，用于展示更新后的用户列表
     */
    @RequestMapping("/add")
    public ModelAndView add(Users user) {
        ModelAndView mv = null;
        userServiceAuto.insertUsers(user);
        mv = new ModelAndView("redirect:/users/tolist.do");
        return mv;
    }

    /**
     * 处理跳转到更新用户信息页面的请求，根据传入的用户ID，调用服务层的selectUserById方法查询出对应的用户对象，
     * 将用户对象添加到ModelAndView中，然后返回指向 "/users/update" 页面的ModelAndView对象，以便在页面上展示用户信息进行更新操作。
     *
     * @param id 用户的ID，用于查询要更新的具体用户
     * @return ModelAndView对象，视图名称为 "/users/update"，包含了要更新的用户对象数据
     */
    @RequestMapping("/toupdate")
    public ModelAndView toupdate(String id) {
        ModelAndView mv = null;
        mv = new ModelAndView("/users/update");
        Users user = userServiceAuto.selectUserById(Integer.parseInt(id));
        mv.addObject("user", user);
        return mv;
    }

    /**
     * 处理更新用户信息的请求，调用服务层的updateByUserName方法根据传入的用户对象更新数据库中的用户信息，
     * 操作完成后重定向到用户列表页面（"/users/tolist.do"）。
     *
     * @param user 包含更新后信息的用户对象，用于更新数据库中对应用户的记录
     * @return ModelAndView对象，重定向到用户列表页面的视图，用于展示更新后的用户列表
     */
    @RequestMapping("/update")
    public ModelAndView update(Users user) {
        ModelAndView mv = null;
        userServiceAuto.updateByUserName(user);
        mv = new ModelAndView("redirect:/users/tolist.do");
        return mv;
    }

    /**
     * 处理删除用户的请求，根据传入的用户ID字符串（多个ID以逗号分隔），拆分字符串获取每个ID，
     * 然后调用服务层的deleteByUserId方法逐个删除对应的用户记录，操作完成后重定向到用户列表页面（"/users/tolist.do"）。
     *
     * @param ids 包含要删除用户的ID字符串，多个ID之间用逗号隔开
     * @return ModelAndView对象，重定向到用户列表页面的视图，用于展示更新后的用户列表
     */
    @RequestMapping("/delete")
    public ModelAndView delete(String ids) {
        ModelAndView mv = null;
        String[] FenGe = ids.split(",");
        for (int i = 0; i < FenGe.length; i++) {
            userServiceAuto.deleteByUserId(Integer.parseInt(FenGe[i]));
        }
        mv = new ModelAndView("redirect:/users/tolist.do");
        return mv;
    }

    /**
     * 根据用户名和用户ID判断两者是否对应（可能用于某种权限验证或者数据一致性检查等场景）。
     * 先通过服务层的findIdByuserName方法根据用户名查找用户，再根据传入的用户ID查找用户，
     * 比较两个用户的用户名是否相等，相等则返回1，否则返回0。
     *
     * @param userName 要查找的用户名
     * @param userId   要查找的用户ID
     * @return 1表示用户名和用户ID对应的用户信息匹配，0表示不匹配
     */
    @RequestMapping("/findIdByuserName")
    public int findIdByuserName(String userName, String userId) {
        UserPo user = userService.findIdByuserName(userName);
        Users users = userServiceAuto.selectUserById(Integer.parseInt(userId));
        if (user.getUserName().equals(users.getUsername())) {
            return 1;
        }
        return 0;
    }

    /**
     * 处理验证用户名是否存在的请求，调用服务层的selectIsExit方法根据传入的用户名判断其是否存在，
     * 将返回的结果（存在为1，不存在为0等类似逻辑）转换为JSON格式并返回，供前端进行相应处理。
     *
     * @param userName 要验证是否存在的用户名
     * @return JSON格式的字符串，表示用户名是否存在的结果，可被前端解析使用
     */
    @ResponseBody
    @RequestMapping(value = "/YZ")
    public Object YZ(String userName) {
        int YorN = userServiceAuto.selectIsExit(userName);
        Gson gson = new Gson();
        return gson.toJson(YorN);
    }
}