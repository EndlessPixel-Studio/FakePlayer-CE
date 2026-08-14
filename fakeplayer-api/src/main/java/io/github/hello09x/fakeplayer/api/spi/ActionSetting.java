package io.github.hello09x.fakeplayer.api.spi;

import lombok.EqualsAndHashCode;

/**
 * @author tanyaofei
 * @since 2024/8/9
 **/
@EqualsAndHashCode
public
class ActionSetting implements Cloneable {

    /**
     * 总次数
     */
    public final int maximum;

    /**
     * 剩余次数
     */
    public int remains;

    /**
     * 间隔
     */
    public int interval;

    /**
     * 等待 ticks
     */
    public int wait;

    /**
     * 随动作携带的消息 (如 SAY 动作发送的聊天内容), 可为 null
     */
    public String message;

    public ActionSetting(int maximum, int interval) {
        this(maximum, interval, 0);
    }

    public ActionSetting(int maximum, int interval, int wait) {
        this(maximum, interval, wait, null);
    }

    public ActionSetting(int maximum, int interval, int wait, String message) {
        this.maximum = maximum;
        this.remains = maximum;
        this.interval = interval;
        this.wait = wait;
        this.message = message;
    }

    public static ActionSetting once() {
        return new ActionSetting(1, 0);
    }

    public static ActionSetting stop() {
        return new ActionSetting(0, 0);
    }

    public static ActionSetting interval(int interval) {
        return new ActionSetting(-1, interval);
    }

    public static ActionSetting continuous() {
        return new ActionSetting(-1, 0);
    }

    @Override
    public ActionSetting clone() {
        try {
            return (ActionSetting) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
