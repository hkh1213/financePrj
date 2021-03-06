package com.example.study.openDartApi.dtoImpl.api;

import com.example.study.openDartApi.dto.api.DartApiDto;

import java.io.Serializable;
import java.util.List;

public class DartApiResponseDto implements Serializable, DartApiDto {
    private String                  status;
    private String                  message;
    private List<DartApiDetailDto>  list;

    public DartApiResponseDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DartApiDetailDto> getList() {
        return list;
    }

    public void setList(List<DartApiDetailDto> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "DartResponseDto{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", list=" + list +
                '}';
    }
}
