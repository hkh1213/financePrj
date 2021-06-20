package com.example.study.openDartApi.repository.stock;

import com.example.study.openDartApi.entity.stock.CorpUpdate;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CorpUpdateRepository extends CrudRepository<CorpUpdate, Integer> {
    Optional<CorpUpdate> findTopByOrderByIdDesc();
//    Optional<CorpUpdate> findTopByProgressOrderByIdDesc(String progress);
    Optional<CorpUpdate> findTopByProgressNotOrderByIdDesc(String progress);
}
