package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    public static final String WX_APPID = "appid";
    public static final String WX_SECRET = "secret";
    public static final String WX_JS_CODE = "js_code";
    public static final String WX_GRANT_TYPE = "grant_type";
    public static final String WX_AUTHORIZATION_CODE = "authorization_code";
    public static final String WX_OPENID = "openid";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登入
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openId = getOpenId(userLoginDTO.getCode());

        // 判断openID是否为空,如果为空,抛出业务异常
        if (openId == null ){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenId(openId);

        // 如果是新用户,自动完成注册
        if (user == null){
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        // 返回这个用户对象

        return user;
    }

    /**
     * 调用微信接口服务,获取微信用户的OpenID
     * @param code
     * @return
     */
    private String getOpenId(String code){
        // 调用微信服务器接口,获得当前微信用户的OpenID
        HashMap<String, String> map = new HashMap<>();
        map.put(WX_APPID, weChatProperties.getAppid());
        map.put(WX_SECRET, weChatProperties.getSecret());
        map.put(WX_JS_CODE, code);
        map.put(WX_GRANT_TYPE, WX_AUTHORIZATION_CODE);
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString(WX_OPENID);
        return openid;
    }
}
