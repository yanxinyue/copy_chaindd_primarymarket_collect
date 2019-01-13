package com.chaindd.dao;

import com.chaindd.entity.ICObenchGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @Author: xinyueyan
 * @Date: 1/1/2019 10:30 PM
 */
public interface ICOdao extends JpaRepository<ICObenchGeneral, String>{
    @Query(value = "select ico.info_url from tb_icobenchlist ico", nativeQuery = true)
    List getICOnames();
}
