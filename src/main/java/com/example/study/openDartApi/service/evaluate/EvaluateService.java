package com.example.study.openDartApi.service.evaluate;

import com.example.study.openDartApi.dto.stock.DartDto;

public interface EvaluateService {
    public void     evaluate(DartDto corpInfo);
    public String   getServiceName();
    public String   getSimpleName();
}
