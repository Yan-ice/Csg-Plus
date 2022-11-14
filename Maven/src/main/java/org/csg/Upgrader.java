package org.csg;

import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Upgrader {
    static CommandSender sender;
    public static void upgrade(CommandSender p){
        sender = p;
        p.sendMessage("正在备份原数据文件...");
        File root = Data.lobbyDir;
        File back = new File(Data.fmain.getDataFolder(), "lobby_backup");
        FileMng.copyDir(root,back);
        p.sendMessage("正在更新数据...");
        upgRecurse(root);
    }

    private static void upgRecurse(File f){
        if(f.isDirectory()){
            for(File fi : f.listFiles()){
                upgRecurse(fi);
            }
        }else{
            if(f.getName().endsWith(".csgtask")){
                try {
                    upgradeCsg(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void upgradeCsg(File f) throws IOException {
        StringBuilder new_l = new StringBuilder();

        //读取文件
        FileInputStream input = new FileInputStream(f.getPath());
        InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
        BufferedReader r = new BufferedReader(isr);

        Pattern member_pattern = Pattern.compile("\\$[A-Za-z0-9_]+\\$\\.[A-Za-z0-9._\\$]+");
        Pattern single_pattern = Pattern.compile("\\$[A-Za-z0-9_]+\\$");

        int line_counter = 0;
        while(r.ready()){
            int replace_counter = 0;
            line_counter++;
            String line_origin = r.readLine();
            String line = line_origin+"";
            Matcher mch = member_pattern.matcher(line);
            if(line_origin.contains("$$")){
                replace_counter+=2;
            }

            while(mch.find()){
                String origin = mch.group();
                String target = origin+"";
                target = target.replaceFirst("\\$","{");
                target = target.replaceFirst("\\$","").trim()+"}";

                line = line.replace(origin,target);
                replace_counter+=2;
            }

            mch = single_pattern.matcher(line);
            while(mch.find()){
                String origin = mch.group();
                String target = origin+"";
                target = target.replaceFirst("\\$","{");
                target = target.replace("$","}");

                line = line.replace(origin,target);
                replace_counter+=1;
            }

            if(line.contains("$")){
                mch = single_pattern.matcher(line);
                while(mch.find()){
                    String origin = mch.group();
                    String target = origin+"";
                    target = target.replaceFirst("\\$","{");
                    target = target.replace("$","}");

                    line = line.replace(origin,target);
                    replace_counter+=1;
                }
            }

            new_l.append(line).append("\n");

            if(replace_counter>=3){
                sender.sendMessage("=========================" +
                        "\nCsg猜测应将文件"+f.getPath()+" "+"第 "+line_counter+" 行的语句: \n"
                        +line_origin.trim()+" \n替换为 "+ line.trim()+" 。" +
                        "\n由于语句较为复杂，如果猜测更新有误，请手动更正！");
            }
        }
        r.close();
        isr.close();
        input.close();

        f.delete();
        f.createNewFile();

        //写回文件
        FileOutputStream out = new FileOutputStream(f.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(osr);
        bw.write(new_l.toString());
        bw.flush();
        bw.close();
        osr.close();
        out.close();
    }

}
