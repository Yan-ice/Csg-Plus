package org.csg.group.task.toolkit;

import lombok.Cleanup;
import lombok.var;
import org.csg.Data;
import org.csg.FileMng;
import org.csg.Fwmain;
import org.csg.SplitCharUtils;
import org.csg.group.Group;
import org.csg.group.Lobby;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class JavaTaskCompiler extends ClassLoader {
    private final Lobby lobby;
    private boolean hasContent = false;
    private final String path;
    private byte[] bt = new byte[1000000];
    private Set<String> imports = new HashSet<String>();
    private String codes = "";

    public JavaTaskCompiler(Lobby lobby) {
        super(Group.class.getClassLoader());
        this.lobby = lobby;
        path = lobby.getTempFolder().getAbsolutePath();

        imports.add("customgo.*");
        imports.add("org.bukkit.entity.Player");
        imports.add("org.bukkit.plugin.java.JavaPlugin");

    }


    public void addDepend(File file) {
        if(true)
            return;
        String s = file.getName().substring(0, file.getName().length() - 6);

        if (s.contains(".")) {
            Data.ConsoleInfo("正在添加java脚本依赖" + s);
            String[] paths = s.split("\\.");
            File current = new File(path);

            if (!current.exists()) {
                current.mkdir();
            }
            for (int a = 0; a < paths.length - 1; a++) {
                boolean ch = false;
                for (File f : current.listFiles()) {
                    if (f.getName().equals(paths[a])) {
                        current = f;
                        ch = true;
                        break;
                    }
                }
                if (!ch) {
                    current = new File(current.getPath() + "/" + paths[a]);
                    current.mkdir();
                }

            }
            File target = new File(current + "/" + paths[paths.length - 1] + ".class");
            if (!target.exists()) {
                FileMng.deleteDir(target);
            }
            FileMng.copyDir(file, target);
        }
    }

    public void read(File f) throws IOException {
        hasContent = true;
        Data.ConsoleInfo("正在加载java脚本 " + f.getName());

        String path = f.getPath();
        FileInputStream input = new FileInputStream(path);
        InputStreamReader r = new InputStreamReader(input, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(r);

        String s = reader.readLine();

        boolean pass = true;
        while (reader.ready()) {
            if (s.contains("###")) {
                break;
            }
            if (!s.contains(" ")) {
                s = reader.readLine();
                continue;
            }
            String[] cm = s.split(" ");

            switch (cm[0]) {
                case "macro":
                    if (!lobby.requireMacro(cm[1], cm.length > 2 ? cm[2] : null)) {
                        pass = false;
                    }
                    break;
                case "depend":
                    if (!Data.fmain.getServer().getPluginManager().isPluginEnabled(cm[1])) {
                        Data.ConsoleInfo("该大厅并未满足脚本需求的插件依赖" + cm[1] + "！");
                        Data.ConsoleInfo("请添加所需的前置插件，并重启服务器。在此之前，相关脚本将无法使用！");
                        pass = false;
                    }
                    break;
                case "import":
                    imports.add(cm[1]);
            }
            s = reader.readLine();
        }
        if (pass) {
            while (reader.ready()) {
                String st = reader.readLine() + "\n";
                if (st.contains("setOp") || st.contains("setGameMode") || st.contains("dispatchCommand")) {
                    Data.ConsoleInfo(String.format("脚本 %s 中存在可疑后门语句 %s ！如果不是，请忽略此消息。", f.getName(), st));
                }
                for (Map.Entry<String, Object> o : lobby.macros.macros.entrySet()) {
                    st = st.replace(String.format("$%s$", o.getKey()), o.getValue().toString());
                }
                codes = codes.concat(st);
            }
        }


        reader.close();
        r.close();
        input.close();
    }

    public Class<?> compile() throws IOException {
        if (!hasContent) {
            return null;
        }

        StringBuilder out = new StringBuilder();
        for (String s : imports) {
            out.append("import ").append(s).append(";\n");
        }
        out.append("public class Temp { \n" +
                "JavaPlugin plugin;\n" +
                " public void _setPlugin(JavaPlugin p){plugin = p;}\n" +
                "Lobby lobby = null;Group group = null;Player striker = null;Player player = null;\n" +
                "public void _setMember(Lobby lobby,Group gro,Player striker,Player player){\n" +
                "this.group = gro; this.lobby=lobby;this.striker = striker;this.player = player;}\n");
        out.append(codes);
        out.append("}");

        @Cleanup
        FileOutputStream output = new FileOutputStream(path + "/Temp.java");
        output.write(out.toString().getBytes(StandardCharsets.UTF_8));
        output.flush();


        JavaCompiler c = ToolProvider.getSystemJavaCompiler();

        StringBuilder builder = new StringBuilder(path);

        for (File core : Data.bukkit_core) {
            builder.append(SplitCharUtils.getSplitChar(Fwmain.getOsName())).append(core.getAbsolutePath());
        }
        File plugins = new File("./plugins");
        if(plugins.exists()){
            for(File plug : plugins.listFiles()){
                if(plug.getName().endsWith(".jar")){
                    builder.append(SplitCharUtils.getSplitChar(Fwmain.getOsName())).append(plug.getAbsolutePath());
                }
            }
        }

        //将配置文件中依赖路径写入编译路
        //Fwmain.getOptionDepends().forEach(depend -> builder.append(SplitCharUtils.getSplitChar(Fwmain.getOsName())).append(depend));

        if (c == null) {
            Data.ConsoleInfo("当前环境为jre，无法使用javatask！如果需要使用，请安装jdk。");
            return null;
        }

        @Cleanup
        OutputStream streamOut = new FileOutputStream(path + "/TempOut.log");
        @Cleanup
        OutputStream streamError = new FileOutputStream(path + "/TempError.log");
        if (c.run(null, streamOut, streamError, "-encoding", "utf-8", "-classpath",
                builder.toString(),
                String.format("%s/Temp.java", path)
        ) == 0) {
            File temp = null;
            for (File f : lobby.getTempFolder().listFiles()) {
                if (f.getName().endsWith(".class")) {
                    if (f.getName().contains("$")) {
                        String classname = f.getName().substring(0, f.getName().length() - 6);
                        var byteArray = new ByteArrayOutputStream();
                        @Cleanup
                        FileInputStream input = new FileInputStream(f.getPath());
                        int streamInt;
                        while((streamInt = input.read()) != -1) {
                            byteArray.write(streamInt);
                        }
                        int length = byteArray.size();
                        defineClass(classname, byteArray.toByteArray(), 0, length);
                    } else if (f.getName().equals("Temp.class")) {
                        temp = f;
                    }
                }
            }
            if (temp != null) {
                var byteArray = new ByteArrayOutputStream();
                @Cleanup
                FileInputStream input = new FileInputStream(temp.getPath());
                int streamInt;
                while((streamInt = input.read()) != -1) {
                    byteArray.write(streamInt);
                }
                int length = byteArray.size();
                //FileMng.deleteDir(dir);
                return this.defineClass("Temp", byteArray.toByteArray(), 0, length);
            }
        }

        Data.Debug("加载javatask脚本出现了错误。");

        //FileMng.deleteDir(dir);
        return null;
    }

}