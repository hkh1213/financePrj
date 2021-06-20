package com.example.study.openDartApi.repository.count;

import com.example.study.openDartApi.entity.count.RequestCount;
import com.example.study.openDartApi.entity.count.RequestCountPK;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RequestCountRepository extends CrudRepository<RequestCount, RequestCountPK> {
    List<RequestCount> findAllByRequestCountPKLocalDate(LocalDate localDate);
}
