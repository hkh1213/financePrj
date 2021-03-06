package com.example.study.openDartApi.entity.stock;

import com.example.study.openDartApi.dto.stock.DartUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
public class CorpUpdate implements Serializable, DartUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int     id;
    private String  progress;
    /**
     * updateDate
     * 
     * updating : 서버에서 업데이트 중 
     * success  : 완료
     * failed   : 실패
     */
    private Date    updateDate;

    public CorpUpdate() {
    }

    public CorpUpdate(String progress, Date updateDate) {
        this.progress = progress;
        this.updateDate = updateDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "CorpUpdate{" +
                "id=" + id +
                ", progress='" + progress + '\'' +
                ", updateDate=" + updateDate +
                '}';
    }
}
