package com.doldol.api.oauth;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;

import java.util.Locale;

public enum SocialProvider {
    KAKAO,
    NAVER;
    public String path(){return name().toLowerCase(Locale.ROOT);}
    public static SocialProvider fromPath(String path){
        for(SocialProvider p : values()){
            if(p.path().equals(path)){
                return p;
            }
        }
        throw new BusinessException(ErrorCode.OAUTH_NOT_ENABLED);
    }
}
