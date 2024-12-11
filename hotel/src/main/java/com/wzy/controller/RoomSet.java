package com.wzy.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.wzy.page.Page;
import com.wzy.pojo.AttributePo;
import com.wzy.pojo.RoomSetPo;
import com.wzy.service.AttributeService;
import com.wzy.service.RoomSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

/**
 * 房间设置
 *
 * @Auther: wzy
 * @Date: 2021/01/27/16:23
 * @Description:
 */
@Controller
@RequestMapping("/RoomSet")
public class RoomSet {

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private RoomSetService roomSetService;

    // 抽取参数校验方法，使代码复用且更清晰
    private int validateCurrentPage(Integer currentPage) {
        if (currentPage == null || currentPage == 0) {
            return 1;
        }
        return currentPage;
    }

    private String validateTxtName(String txtname) {
        return txtname == null? "" : txtname;
    }

    // 分页和模糊查询
    @RequestMapping("/tolist")
    public ModelAndView list(HttpServletRequest request, Integer currentPage, String txtname) {
        ModelAndView mv = new ModelAndView("/roomset/roomset");
        currentPage = validateCurrentPage(currentPage);
        txtname = validateTxtName(txtname);

        Page<RoomSetPo> vo = new Page<RoomSetPo>();
        vo.setCurrentPage(currentPage);
        try {
            vo = this.roomSetService.pageFuzzyselect(txtname, vo);
        } catch (Exception e) {
            // 这里可以更详细地记录日志、返回错误信息给前端等，暂简单打印异常
            e.printStackTrace();
        }

        List<AttributePo> listOne = getGuestRoomLevelList();
        mv.addObject("listOne", listOne);
        mv.addObject("list", vo);
        mv.addObject("txtname", txtname);
        return mv;
    }

    private List<AttributePo> getGuestRoomLevelList() {
        return attributeService.selectGuestRoomLevel();
    }

    @RequestMapping("/toadd")
    public ModelAndView toadd() {
        ModelAndView mv = new ModelAndView("/roomset/add");
        mv.addObject("listOne", getGuestRoomLevelList());
        mv.addObject("listTwo", attributeService.selectRoomState());
        return mv;
    }

    @RequestMapping("/add")
    public ModelAndView add(RoomSetPo roomSetPo) {
        try {
            roomSetService.insertAll(roomSetPo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/RoomSet/tolist.do");
    }

    @RequestMapping("/toupdate")
    public ModelAndView toupdate(int id) {
        ModelAndView mv = new ModelAndView("/roomset/update");
        mv.addObject("listOne", getGuestRoomLevelList());
        mv.addObject("listTwo", attributeService.selectRoomState());
        try {
            RoomSetPo listPo = roomSetService.selectById(id);
            mv.addObject("listPo", listPo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mv;
    }

    @RequestMapping("/update")
    public ModelAndView update(RoomSetPo roomSetPo) {
        try {
            roomSetService.updateById(roomSetPo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/RoomSet/tolist.do");
    }


    @RequestMapping("/delete")
    public ModelAndView delete(String id) {
        ModelAndView mv = null;
        String[] FenGe = id.split(",");
        for (int i = 0; i < FenGe.length; i++) {
            roomSetService.deleteById(Integer.parseInt(FenGe[i]));
        }
        mv = new ModelAndView("redirect:/RoomSet/tolist.do");
        return mv;
    }

    @RequestMapping("/addGuestRoomLevel")
    public ModelAndView addGuestRoomLevel(String txtname) {
        try {
            attributeService.insertAll(2, txtname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/RoomSet/tolist.do");
    }

    @RequestMapping("/deleteGuestRoomLevel")
    public ModelAndView deleteGuestRoomLevel(String id) {
        String[] split = id.split(",");
        for (String s : split) {
            attributeService.deleteById(Integer.parseInt(s));
        }
        ModelAndView mv = null;
        mv = new ModelAndView("redirect:/RoomSet/tolist.do");
        return mv;
    }


    @ResponseBody
    @RequestMapping(value = "/YZ")
    public Object YZ(String roomNumber) {
        int YorN;
        try {
            YorN = roomSetService.selectYZ(roomNumber);
        } catch (Exception e) {
            e.printStackTrace();
            // 可以考虑根据异常情况返回合适的默认值或者错误提示信息给前端，这里暂返回0作为示意
            YorN = 0;
        }
        Gson gson = new Gson();
        return gson.toJson(YorN);
    }
}
