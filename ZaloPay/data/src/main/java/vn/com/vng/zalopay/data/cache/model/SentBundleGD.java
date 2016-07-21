package vn.com.vng.zalopay.data.cache.model;

import java.util.List;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "SENT_BUNDLE_GD".
 */
public class SentBundleGD {

    private long id;
    /** Not-null value. */
    private String sendZaloPayID;
    private Integer type;
    private Long createTime;
    private Long lastOpenTime;
    private Integer totalLuck;
    private Integer numOfOpenedPakages;
    private Integer numOfPackages;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient SentBundleGDDao myDao;

    private List<PackageInBundleGD> sentPackages;

    public SentBundleGD() {
    }

    public SentBundleGD(long id) {
        this.id = id;
    }

    public SentBundleGD(long id, String sendZaloPayID, Integer type, Long createTime, Long lastOpenTime, Integer totalLuck, Integer numOfOpenedPakages, Integer numOfPackages) {
        this.id = id;
        this.sendZaloPayID = sendZaloPayID;
        this.type = type;
        this.createTime = createTime;
        this.lastOpenTime = lastOpenTime;
        this.totalLuck = totalLuck;
        this.numOfOpenedPakages = numOfOpenedPakages;
        this.numOfPackages = numOfPackages;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSentBundleGDDao() : null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getSendZaloPayID() {
        return sendZaloPayID;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSendZaloPayID(String sendZaloPayID) {
        this.sendZaloPayID = sendZaloPayID;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLastOpenTime() {
        return lastOpenTime;
    }

    public void setLastOpenTime(Long lastOpenTime) {
        this.lastOpenTime = lastOpenTime;
    }

    public Integer getTotalLuck() {
        return totalLuck;
    }

    public void setTotalLuck(Integer totalLuck) {
        this.totalLuck = totalLuck;
    }

    public Integer getNumOfOpenedPakages() {
        return numOfOpenedPakages;
    }

    public void setNumOfOpenedPakages(Integer numOfOpenedPakages) {
        this.numOfOpenedPakages = numOfOpenedPakages;
    }

    public Integer getNumOfPackages() {
        return numOfPackages;
    }

    public void setNumOfPackages(Integer numOfPackages) {
        this.numOfPackages = numOfPackages;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<PackageInBundleGD> getSentPackages() {
        if (sentPackages == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PackageInBundleGDDao targetDao = daoSession.getPackageInBundleGDDao();
            List<PackageInBundleGD> sentPackagesNew = targetDao._querySentBundleGD_SentPackages(id);
            synchronized (this) {
                if(sentPackages == null) {
                    sentPackages = sentPackagesNew;
                }
            }
        }
        return sentPackages;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetSentPackages() {
        sentPackages = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
