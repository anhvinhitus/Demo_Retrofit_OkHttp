package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "SENT_BUNDLE_SUMMARY_DB".
 */
@Entity
public class SentBundleSummaryDB {

    @Id
    private Long id;
    private Long totalOfSentAmount;
    private Integer totalOfSentBundle;
    private Long timeCreate;

    @Generated
    public SentBundleSummaryDB() {
    }

    public SentBundleSummaryDB(Long id) {
        this.id = id;
    }

    @Generated
    public SentBundleSummaryDB(Long id, Long totalOfSentAmount, Integer totalOfSentBundle, Long timeCreate) {
        this.id = id;
        this.totalOfSentAmount = totalOfSentAmount;
        this.totalOfSentBundle = totalOfSentBundle;
        this.timeCreate = timeCreate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTotalOfSentAmount() {
        return totalOfSentAmount;
    }

    public void setTotalOfSentAmount(Long totalOfSentAmount) {
        this.totalOfSentAmount = totalOfSentAmount;
    }

    public Integer getTotalOfSentBundle() {
        return totalOfSentBundle;
    }

    public void setTotalOfSentBundle(Integer totalOfSentBundle) {
        this.totalOfSentBundle = totalOfSentBundle;
    }

    public Long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(Long timeCreate) {
        this.timeCreate = timeCreate;
    }

}
