package younin.use;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平台无关的模组入口。Fabric / Forge 各自在自己的入口里调用 {@link #init()}。
 */
public final class Use {
    public static final String MOD_ID = "use";
    public static final String MOD_NAME = "USE";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private Use() {
    }

    /** 主初始化：注册表、网络包、事件等后续都挂在这里。 */
    public static void init() {
        LOGGER.info("[{}] initialised", MOD_NAME);
    }

    /** 客户端初始化：渲染器、按键、配置界面等。 */
    public static void initClient() {
        LOGGER.info("[{}] client initialised", MOD_NAME);
    }
}
