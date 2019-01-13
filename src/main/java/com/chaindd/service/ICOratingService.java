package com.chaindd.service;

import com.chaindd.Constants.DataConstants;
import com.chaindd.entity.ICObenchGeneral;
import com.chaindd.utils.OkHttpClientUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xinyueyan
 * @Date: 1/8/2019 10:30 AM
 */
@Service
@Slf4j
public class ICOratingService {
    protected final Gson gson = new GsonBuilder().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    @Autowired
    private EntityManager em;

    //@Transactional
    @Test
    public void getData() throws IOException {
        String listUrl = "https://icorating.com/ico/all/load/";
        int pages = 1;
        String icorating_page = OkHttpClientUtil.getData(listUrl+"?page=1");
        System.out.println(gson.fromJson(icorating_page,Object.class));
    }
}
