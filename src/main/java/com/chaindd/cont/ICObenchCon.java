package com.chaindd.cont;

import com.chaindd.dao.ICOdao;
import com.chaindd.service.ICObenchService;
import com.chaindd.service.ICOratingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

/**
 * @Author: xinyueyan
 * @Date: 12/29/2018 2:12 PM
 */
@RestController
public class ICObenchCon {
    @Autowired
    private ICOdao icoDao;
    @Autowired
    private ICObenchService icoService;
    @Autowired
    private ICOratingService icoRatingService;

    /**
     * 收集ICO列表
     * @throws IOException
     */
    @RequestMapping(value = "/icolist", method = RequestMethod.GET)
    public void getIcoList() throws IOException {
        icoService.getData();
    }
    @RequestMapping(value = "/icoinfo", method = RequestMethod.GET)
    public void getICOinfo() throws IOException, ParseException {
        //获取项目列表
        icoService.getICOinfo();
    }
    @RequestMapping(value = "/icorating", method = RequestMethod.GET)
    public void getICOrating() throws IOException, ParseException {
        //获取项目列表
        icoRatingService.getData();
    }


}
