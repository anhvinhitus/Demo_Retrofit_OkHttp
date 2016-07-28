package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.PermissionEntity;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.Permission;
import vn.com.vng.zalopay.domain.model.User;

@Singleton
public class UserEntityDataMapper {

    @Inject
    public UserEntityDataMapper() {
    }

    public User transform(LoginResponse response) {
        User user = null;
        if (response != null) {
            user = new User();
            user.accesstoken = response.accesstoken;
            user.expirein = response.expirein;
            user.uid = response.userid;
            user.profilelevel = response.profilelevel;
            user.profilePermissions = transform(response.permission);
            user.phonenumber = response.phonenumber;
        }

        return user;
    }

    public Permission transform(PermissionEntity entity) {
        Permission permission = null;
        if (entity != null) {
            permission = new Permission();
            permission.allow = entity.allow;
            permission.pmcid = entity.pmcid;
            permission.profilelevel = entity.profilelevel;
            permission.requireotp = entity.requireotp;
            permission.transtype = entity.transtype;
            permission.requirepin = entity.requirepin;
        }
        return permission;
    }

    public List<Permission> transform(List<PermissionEntity> entities) {
        if (Lists.isEmptyOrNull(entities)) {
            return Collections.emptyList();
        }
        List<Permission> permissions = new ArrayList<>();
        for (PermissionEntity entity : entities) {
            Permission permission = transform(entity);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        return permissions;
    }
}
