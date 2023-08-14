package org.csg.group.task.cast;


import customgo.Group;
import org.csg.Fwmain;
import org.csg.group.Lobby;

import java.lang.reflect.Type;

public class GroupTypeCaster extends TypeCaster{
    @Override
    public Class<?> targetType() {
        return Group.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    @Override
    protected String serializeRule(Object s) throws Exception {
        Group g = (Group) s;
        return g.getLobby().getName() + "_" + g.getName();
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null
     */
    @Override
    protected Object deserializeRule(String s, Type... typeArguments) throws Exception {
        String[] lobbyAndGroup = s.split("_");
        if (lobbyAndGroup.length == 2) {
            for (Lobby lobby : Fwmain.lobbyList) {
                if (lobby.getName().equals(lobbyAndGroup[0])) {
                    return lobby.getGroupList().stream()
                            .filter(group -> lobby.getName().equals(lobbyAndGroup[1]))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
        return null;
    }
}
