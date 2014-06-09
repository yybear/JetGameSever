package com.handwin.event;

/**
 * User: roger
 * Date: 13-12-13 上午11:33
 */
public class Events {
    public final static int ACTION_SUCCESS = 1;
    public final static int ACTION_FAILED = 2;
    public final static int ACTION_REJECT = 3;  // 拒绝

    public final static byte CONNECT_PACKET_TYPE = 0x01;     // 连接指令
    public final static byte PING_COMMAND = 0x03;            // ping心跳指令
    public final static byte PONG_COMMAND = 0x04;            // pong心跳指令
    public final static byte LOGOUT_COMMAND = 0x05;          // 退出正在玩的游戏

    /**
     * 玩家玩游戏过程中的type
     */
    public final static int LOG_IN = 0x01;                   // 连接游戏，进入一局
    public final static int RES_LOCK = 0x03;
    public final static int RES_LOCK_COMPLETE = 0x04;

    public final static int GAME_END = 0x07;
    public final static int GAME_RESET = 0x08;

    public static final int START = 0x1a;
    public static final int OVER = 0x1b;

    public static final int USER_CLOSE_GAME_CODE = 0x05;
    public static final int USER_OFFLINE_CODE = 0x06;


    public final static int LOGIN_GAME = 0x10;               // 登陆游戏
    public final static int LOGOUT_GAME = 0x11;              // 登出游戏

    public final static int GET_FRIENDS = 0x12;              // 获取已经安装该游戏的好友
    public final static int ADD_FRIENDS = 0x13;              // 添加好友到chatgame联系列表
    public final static int INVITE_PLAYER = 0x14;            // 邀请玩家玩游戏
    public final static int REPLY_INVITE = 0x15;             // 响应玩家邀请
    public final static int JOIN_WAIT_QUEUE = 0x16;          // 进入等待队列，准备随机匹配
    public final static int PLAYER_READY = 0x17;             // 玩家就绪
    public final static int GAME_START = 0x18;               // 游戏开始

    public final static int SAVE_GAME_INFO = 0x30;
    public final static int SAVE_SEX = 0x31;                 // 设置性别
    public final static int SCORE_LIST = 0x32;               // 查询游戏排行榜


    public final static int SERVER_ERR = 0x50;               // 服务内部异常

}
