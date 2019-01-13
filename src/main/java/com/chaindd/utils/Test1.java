package com.chaindd.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: xinyueyan
 * @Date: 1/7/2019 3:54 PM
 */
public class Test1 {
    public static void main(String[] args) {
        String a = "  &nbsp;a b c  ";
        System.out.println("--"+StringUtils.trim(a)+"----");
    }
}
