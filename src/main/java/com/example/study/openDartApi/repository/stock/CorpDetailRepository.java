package com.example.study.openDartApi.repository.stock;

import com.example.study.openDartApi.entity.stock.CorpDetail;
import com.example.study.openDartApi.entity.stock.pk.CorpDetailPK;
import org.springframework.data.repository.CrudRepository;

public interface CorpDetailRepository extends CrudRepository<CorpDetail, CorpDetailPK> {
}
