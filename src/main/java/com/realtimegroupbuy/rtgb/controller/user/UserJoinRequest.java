package com.realtimegroupbuy.rtgb.controller.user;


public record UserJoinRequest(
    String nickname,
    String username,
    String password
) {

}
