package org.csg.group.task.toolkit;

import org.csg.Data;
import org.csg.group.Lobby;
import org.csg.group.task.csgtask.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Stack;

public class TaskCompiler {

        public void destroy(){
            functions.clear();
            listeners.clear();
            last_one = null;
            key_com = null;
        }
    Set<FunctionTask> functions = new HashSet<>();
    Set<ListenerTask> listeners = new HashSet<>();

        public void getFunctions(Set<FunctionTask> func){
            func.addAll(functions);
            functions.clear();
        }
    public void getListeners(Set<ListenerTask> func){
        func.addAll(listeners);
        listeners.clear();
    }

        int tab_counter = 0;
        int space_counter = 0;
        boolean line_begin = true;
        String field = "";
        Set<String> target = new HashSet<>();
        Task begin = new NothingTask();
        public Task compile(Lobby lb, File f) {
            field = f.getName().split("\\.")[0];
            Data.ConsoleInfo("正在加载CustomGo脚本 "+f.getName());
            String path = f.getPath();
            last_one = begin;
            key_com.push(begin);

            try {

                FileInputStream input = new FileInputStream(path);
                InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
                BufferedReader r = new BufferedReader(isr);

                boolean pass = true;
                String s = r.readLine();
                while(r.ready()){
                    if(s.contains("###")){
                        break;
                    }
                    if(!s.contains(" ")){
                        s = r.readLine();
                        continue;
                    }
                    String[] cm = s.split(" ");
                    switch(cm[0]){
                        case "macro":
                            if(!lb.requireMacro(cm[1],cm.length>2?cm[2]:null)){
                                pass = false;
                            }
                            break;
                        case "restrict":
                            target.add(cm[1]);
                            break;
                        case "depend":
                            if(!Data.fmain.getServer().getPluginManager().isPluginEnabled(cm[1])){
                                Data.ConsoleInfo("该大厅并未满足脚本需求的插件依赖"+cm[1]+"！");
                                Data.ConsoleInfo("请添加所需的前置插件，并重启服务器。在此之前，相关脚本将无法使用！");
                                pass = false;
                            }
                            break;
                        case "import":
                            break;
                    }
                    s = r.readLine();
                }

                if(!pass){
                    r.close();
                    isr.close();
                    input.close();
                    return null;
                }
                String bu = "";
                String[] parts = new String[]{null,null,null,null};
                int index = 0;

                boolean annotation = false;
                boolean fxg = false;
                char c;
                while( (c=(char)r.read()) !=65535){
                    if(fxg){
                        line_begin = false;
                        space_counter=0;
                        bu = bu.concat(String.valueOf(c));
                        fxg = false;
                        continue;
                    }
                    switch(c){
                        case '\\':
                            fxg = true;
                            break;
                        case '\n':
                            if(annotation){
                                annotation = false;
                            }else{
                                parts[index] = bu.trim();
                                analysisCommand(parts,tab_counter+1);
                            }
                            parts = new String[]{null,null,null,null};
                            bu = "";
                            index = 0;
                            tab_counter = 0;
                            line_begin = true;
                            break;
                        case ' ':
                            if(line_begin){
                                space_counter++;
                                if(space_counter==4){
                                    tab_counter++;
                                    space_counter=0;
                                }
                            }else{
                                bu = bu.concat(" ");
                            }

                            break;
                        case '\t':
                            tab_counter++;
                            break;
                        case '#':
                            line_begin = false;
                            annotation = true;
                            break;
                        case '\r':
                            break;
                        case '(':
                        case ')':
                            parts[index] = bu.trim();
                            bu = "";
                            index++;
                            break;
                        default:
                            line_begin = false;
                            space_counter=0;
                            bu = bu.concat(String.valueOf(c));
                            break;
                    }
                }
                parts[index] = bu.trim();
                analysisCommand(parts,tab_counter+1);

                r.close();
                input.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return begin;
        }

    Task last_one = new NothingTask();
    Stack<Task> key_com = new Stack<Task>();
        private void analysisCommand(String[] parts,int level){

            String label = parts[0];
            String arg = parts[1];
            String rest = parts[2];
            if(label==null || label.trim().equals("") || label.startsWith("#")){
                return;
            }
            Task task;
            try{
                if(label.startsWith("if")){
                    task = new IfTask(arg);
                }else if(label.startsWith("repeat_reset")){
                    task = new RepeatResetTask();
                }else if(label.startsWith("repeat")){
                    analysisCommand(new String[]{"repeat_reset","",""},level);
                    task = new RepeatTask(arg);
                }else if(label.startsWith("while")){
                    task = new WhileTask(arg);
                }else if(label.startsWith("foreach")){
                    task = new ForEachTask(arg,rest);
                }else if(label.startsWith("function")){
                    task = new FunctionTask(label,arg);
                    functions.add((FunctionTask)task);
                }else if(label.startsWith("listener")){
                    task = new ListenerTask(label,arg,field,target);
                    listeners.add((ListenerTask)task);
                }else{
                    task = new CommandTask(label,arg,rest);
                    //task = new TestTask(label+" "+arg);
                }
            }catch(TaskSyntaxError e){
                Data.ConsoleInfo("无法解析任务 "+label+" ：出现语法错误！");
                return;
            }


            if(level==key_com.size()){

                //层级相等
                Task last = key_com.pop();

                if(task instanceof FunctionTask){
                    last_one = last;
                }else if(last instanceof FunctionTask){
                    last.set_next(null);
                    last_one.set_next(task);
                }else{
                    last.set_next(task);
                }
                key_com.push(task);

            }else if(level>key_com.size()) {
                //层级深入
                Task last = key_com.peek();
                if (last instanceof ChooseTask) {
                    ((ChooseTask) last).set_next_Yes(task);
                } else if (last instanceof FunctionTask){
                    last.set_next(task);
                }else{
                    last.set_next(task);
                }
                key_com.push(task);

            }else{
                while(level<key_com.size()){
                    Task last = key_com.pop();
                    Task prev = key_com.pop();
                    Task end = new NothingTask();
                    key_com.push(end);

                    if(prev instanceof WhileTask || prev instanceof RepeatTask || prev instanceof ForEachTask){
                        last.set_next(prev);
                        prev.set_next(end);
                    }else if(prev instanceof IfTask){
                        last.set_next(end);
                        prev.set_next(end);
                    }else if(prev instanceof FunctionTask){
                        last.set_next(null);
                        if(last_one!=null){
                            last_one.set_next(end);
                            last_one = null;
                        }

                    }else{
                        last.set_next(end);
                    }

                }
                Task end = key_com.pop();
                end.set_next(task);
                key_com.push(task);
            }

        }
}


