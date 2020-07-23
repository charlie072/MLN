/**
  * Created by MomoLuaNative.
  * Copyright (c) 2020, Momo Group. All rights reserved.
  *
  * This source code is licensed under the MIT.
  * For the full copyright and license information,please view the LICENSE file in the root directory of this source tree.
  */
package com.xfy.shell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Xiong.Fangyu on 2020-02-12
 */
public class NativeGenerator {
    private boolean useStatic;
    private String code;

    public NativeGenerator(Parser parser) {
        useStatic = parser.isStatic();
        StringBuilder sb = new StringBuilder(Template.CreatedByGenerator).append(getDate()).append('\n')
                .append(!useStatic ? Template.UserdataStart : Template.StaticStart)
                .append(String.format(Template.DefineLuaClassName, parser.getLuaClassName()))
                .append(Template.MethodCom);
        StringBuilder meta = new StringBuilder();
        StringBuilder init = new StringBuilder();

        List<Method> methods = parser.getMethods();
        int size = methods.size();
        StringBuilder[] methodImpl = new StringBuilder[size];

        String getMethodId = useStatic ? "ID = (*env)->GetStaticMethodID(env, clz, \"" : "ID = (*env)->GetMethodID(env, clz, \"";
        for (int i = 0; i < size; i ++) {
            Method m = methods.get(i);
            sb.append("static jmethodID ").append(m.name).append("ID;\n");
            sb.append("static int _").append(m.name).append("(lua_State *L);\n");

            meta.append("            {\"").append(m.name).append("\", _").append(m.name).append("},\n");

            init.append("    ").append(m.name).append(getMethodId)
                    .append(m.name).append("\", \"").append(getMethodSig(m)).append("\");\n");

            methodImpl[i] = new StringBuilder("/**\n")
                    .append(" * ").append(m.toString()).append('\n')
                    .append(" */\n")
                    .append("static int _").append(m.name).append("(lua_State *L) {\n")
                    .append("    PRE\n")
                    .append(getMethodImpl(m))
                    .append("}\n");
        }
        String className = parser.getPackageName() + "." + parser.getClassName();
        className = StringReplaceUtils.replaceAllChar(className, '.', '_');
        String jniStart = Template.JNIStart.replace("${ClassName}", className);
        String jniEnd = useStatic ? Template.StaticJNIEnd.replace("${ClassName}", className)
                : Template.UserdataJNIEnd.replace("${ClassName}", className);
        sb.append(Template.EditorEnd)
                .append(Template.METAStart)
                .append(meta)
                .append(Template.METAEnd)
                .append(jniStart)
                .append(init)
                .append(jniEnd)
                .append(Template.IMPStart);

        for (StringBuilder s : methodImpl) {
            sb.append(s);
        }

        sb.append(Template.EditorEnd);

        code = sb.toString();
    }

    private static String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 日期时间转字符串
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

    private static final HashMap<String, String> primitive;
    static {
        primitive = new HashMap<>(9);
        primitive.put("boolean", "Z");
        primitive.put("byte", "B");
        primitive.put("char", "C");
        primitive.put("short", "S");
        primitive.put("int", "I");
        primitive.put("long", "J");
        primitive.put("float", "F");
        primitive.put("double", "D");
        primitive.put("void", "V");
    }

    public static final String getMethodSig(Method m) {
        Type[] params = m.params;
        StringBuilder sb = new StringBuilder().append('(');

        for (Type p : params) {
            appendType(sb, p);
        }

        sb.append(')');
        appendType(sb, m.returnType);

        return sb.toString();
    }

    private static final void appendType(StringBuilder sb, Type p) {
        if (p.isArray)
            sb.append('[');
        if (p.isPrimitive || p.isVoid) {
            sb.append(primitive.get(p.name));
        } else {
            sb.append('L').append(StringReplaceUtils.replaceAllChar(p.name, '.', '/')).append(';');
        }
    }

    private String getMethodImpl(Method m) {
        StringBuilder sb = new StringBuilder();
        Type[] params = m.params;
        Type rType = m.returnType;
        int len = params.length;

        StringBuilder paramsSb = new StringBuilder();
        int paramsOffset = 0;
        for (int i = 0; i < len; i ++) {
            Type pt = params[i];
            if (i == 0 && useStatic && pt.isLong) {
                paramsOffset = -1;
                paramsSb.append(", (jlong) L");
                continue;
            }
            sb.append("    ").append(pt.toCName()).append(" p").append(i + 1 + paramsOffset)
                    .append(" = ").append(getLuaParam(pt, i + paramsOffset + 2)).append('\n');
            paramsSb.append(", ");
            if (pt.isPrimitive)
                paramsSb.append("(j").append(pt.name).append(')');
            paramsSb.append('p').append(i + paramsOffset + 1);
        }

        if (rType.isVoid) {
            sb.append("    ").append(getJniCall(rType)).append(m.name).append("ID").append(paramsSb).append(");\n")
                    .append("    lua_settop(L, 1);\n")
                    .append("    return 1;\n");
        } else if (rType.isPrimitive) {
            sb.append("    j").append(rType.name).append(" ret = ")
                    .append(getJniCall(rType)).append(m.name).append("ID").append(paramsSb).append(");\n");
            switch (rType.primitiveType) {
                case Number:
                    sb.append("    lua_pushnumber(L, (lua_Number) ret);\n");
                    break;
                case Boolean:
                    sb.append("    lua_pushboolean(L, (int) ret);\n");
                    break;
                default:
                    sb.append("    lua_pushinteger(L, (lua_Integer) ret);\n");
            }
            sb.append("    return 1;\n");
        } else if (rType.isString) {
            sb.append("    jobject ret = ").append(getJniCall(rType)).append(m.name).append("ID").append(paramsSb).append(");\n")
                    .append("    pushJavaString(env, L, ret);\n")
                    .append("    return 1;\n");
        } else {
            sb.append("    jobject ret = ").append(getJniCall(rType)).append(m.name).append("ID").append(paramsSb).append(");\n")
                    .append("    pushJavaValue(env, L, ret);\n")
                    .append("    return 1;\n");
        }

        return sb.toString();
    }

    private static String getLuaParam(Type t, int index) {
        if (t.isPrimitive) {
            switch (t.primitiveType) {
                case Boolean:
                    return "lua_toboolean(L, " + index + ");";
                case Number:
                    return "luaL_checknumber(L, " + index + ");";
                default:
                    return "luaL_checkinteger(L, " + index + ");";
            }
        }
        if (t.isString) {
            return String.format("lua_isnil(L, %d) ? NULL : newJString(env, lua_tostring(L, %d));", index, index);
        }
        return String.format("lua_isnil(L, %d) ? NULL : toJavaValue(env, L, %d);", index, index);
    }

    private String getJniCall(Type rt) {
        if (useStatic) {
            if (rt.isPrimitive || rt.isVoid) {
                return "(*env)->CallStatic" + getFirstUpper(rt.name) + "Method(env, _globalClass, ";
            }
            return "(*env)->CallStaticObjectMethod(env, _globalClass, ";
        }
        if (rt.isPrimitive || rt.isVoid) {
            return "(*env)->Call" + getFirstUpper(rt.name) + "Method(env, jobj, ";
        }
        return "(*env)->CallObjectMethod(env, jobj, ";
    }

    private static String getFirstUpper(String str) {
        char[] cs = str.toCharArray();
        cs[0] += 'A' - 'a';
        return new String(cs);
    }

    @Override
    public String toString() {
        return code;
    }
}